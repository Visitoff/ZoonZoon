# Фоновая вибрация геймпада на iOS

> **Статус:** исследование завершено, нужен **прототип на железе**  
> **Обновлено:** 2026-06-29  
> **Связано:** [app-work-backlog.md](./app-work-backlog.md) → Фаза 1

---

## Цель

Юзер включает вибрацию в ZoonZoon, сворачивает приложение (браузер, видео), геймпад **продолжает вибрировать**.

---

## Краткий вывод

| Подход | Работает? | App Store |
|--------|-----------|-----------|
| Просто свернуть приложение | **Нет** | — |
| Длинный haptic-паттерн (5 мин) | **Нет** — движок умирает при suspend | — |
| Live Activity / push «дослать» | **Нет** — не исполняет код | — |
| Silent audio (без реального звука) | Теоретически да | **Реджект** (guideline 2.5.4) |
| **Реальное аудио + background mode `audio`** | **Возможно** — нужен прототип | Легально, если звук — настоящая фича |
| Android foreground service | **Да** | Легально |

**На iOS «чистый фон» без аудио — невозможен.** Единственный реалистичный путь — сделать **аудио-фичу** (эмбиент, саундскейпы), которая держит процесс живым, а вибрация идёт параллельно.

---

## Как устроена гаптика на iOS

```
App process → CHHapticEngine → GCController.haptics → моторы контроллера
```

- Вибрация **не firmware-rumble** — это поток команд из приложения (~60 fps).
- Нет процесса → нет команд → контроллер молчит (но не отключается).

### Что убивает движок

| Причина | Когда |
|---------|-------|
| `applicationSuspended` | приложение ушло в фон (стандартное поведение iOS) |
| `audioSessionInterrupt` | звонок, **или** конфликт аудио-сессий |
| `gameControllerDisconnect` | контроллер отключился |
| `idleTimeout` | движок простаил без активности |

Документация Apple: [CHHapticEngine.StoppedReason](https://developer.apple.com/documentation/corehaptics/chhapticengine/stoppedreason)

---

## Что НЕ работает (проверено по докам и индустрии)

### 1. Сворачивание приложения

iOS suspend'ит процесс через несколько секунд. Движок получает `applicationSuspended` → вибрация стоп.

### 2. Длинная duration / infinite pattern

Можно создать continuous-событие с `GCHapticDurationInfinite` или duration до ~30 сек. Но **играет движок в процессе** — при suspend паттерн обрывается. Это не «отдал команду мотору на 5 минут и ушёл».

### 3. Live Activity (ActivityKit)

- Рисует статус на локскрине / Dynamic Island.
- **Не исполняет код** приложения — только отрисовка по push/foreground.
- Не может слать haptic-команды 60 раз/сек.
- Не может «раз в 5 минут перезапустить» вибрацию.

### 4. Silent push / BGTaskScheduler

- Silent push будит app на ~30 сек, но **не по расписанию** (Apple троттлит).
- Даже проснувшись в фоне — haptic engine снова suspend'ится.
- Не подходит для непрерывной вибрации.

### 5. `GCController.shouldMonitorBackgroundEvents`

- Про **чтение ввода** (кнопки) в фоне, не про **вывод** вибрации.
- На практике часто ломается между версиями iOS.

### 6. Silent audio hack

- `UIBackgroundModes: audio` + бесшумный луп держит процесс.
- **App Store реджектит**, если нет реального слышимого контента (guideline 2.5.4).
- Жрёт батарею.

### 7. Индустриальный преcedent

[Provenance emulator](https://github.com/Provenance-Emu/Provenance/issues/2723) явно останавливает haptic engines при backgrounding — индустрия принимает foreground-only как норму.

---

## Что МОЖЕТ сработать

### Вариант A: Аудио + вибрация (iOS, основной кандидат)

**Идея:** ZoonZoon проигрывает **реальный звук** (эмбиент, ASMR, «звук вибрации», саундскейпы) + вибрация контроллера синхронно.

| Шаг | Детали |
|-----|--------|
| Background mode | `UIBackgroundModes: audio` — легально, т.к. есть реальный аудио-контент |
| Audio session | `.playback` + `.mixWithOthers` — видео/музыка юзера **не прерывают** сессию |
| Haptics | `CHHapticEngine` на контроллере, continuous/infinite pattern |
| Продукт | «Immersive vibration sessions» — звук + тактильность, не «фон ради фона» |

**Почему видео может не убить:** при `.mixWithOthers` чужой звук не вызывает `audioSessionInterrupt` — микшируется.

**Риск:** не задокументировано Apple, что controller haptics **точно** живут при audio background mode. Нужен **прототип на железе**.

### Вариант B: Foreground-only + keep awake (fallback)

- `UIApplication.shared.isIdleTimerDisabled = true` — экран не гаснет.
- Вибрация работает, пока app на экране.
- Легально, просто, без риска ревью.
- Не решает сценарий «смотрю ютуб в другом приложении».

### Вариант C: Live Activity — только UI

- Показать на локскрине: «ZoonZoon: Wave 70%, активна» + кнопка Stop.
- **Не гонит вибрацию** — только статус для foreground-сессии.

### Вариант D: Android foreground service

- На Android фон **реализуем** через foreground service + notification.
- Честный cross-platform: iOS = audio workaround или foreground-only; Android = полный фон.

---

## План прототипа (следующий шаг)

Минимальный test harness в `iosApp` (~30 мин работы + тест на устройстве с геймпадом):

### 1. Info.plist

```xml
<key>UIBackgroundModes</key>
<array>
    <string>audio</string>
</array>
```

### 2. Audio session

```swift
try AVAudioSession.sharedInstance().setCategory(
    .playback,
    mode: .default,
    options: [.mixWithOthers]
)
try AVAudioSession.sharedInstance().setActive(true)
```

### 3. Loop

- `AVAudioPlayer` с коротким ambient-файлом (реальный слышимый звук, loop).
- `CHHapticEngine` + `GCHapticDurationInfinite` continuous event на контроллере.

### 4. Тест-кейсы

| # | Действие | Ожидание |
|---|----------|----------|
| 1 | Запустить вибрацию + звук, свернуть app | Вибрация продолжается? |
| 2 | Свернуть + включить YouTube со звуком | Вибрация + звук ZoonZoon + видео одновременно? |
| 3 | Заблокировать экран | Вибрация продолжается? |
| 4 | 10+ минут в фоне | Стабильность, батарея |
| 5 | Force quit app | Вибрация стоп (ожидаемо) |

### 5. Логирование

Логировать все `stoppedHandler` reasons — понять, что именно убивает движок.

---

## Решение после прототипа

```
Прототип на железе
       │
       ├─► Работает → продуктовая фича «Audio + Haptic sessions»
       │              + background mode audio в релизе
       │              + описание в App Store как audio/haptic app
       │
       └─► Не работает → закрыть тему на iOS
                         → foreground-only + keep awake
                         → фон только на Android
```

---

## Задачи в бэклоге

- [ ] **Прототип** audio + haptics background (test harness)
- [ ] Зафиксировать результат тестов в этом документе
- [ ] Если OK — продуктовый дизайн «soundscapes + vibration»
- [ ] Если FAIL — пометить `[-]` в app-work-backlog, реализовать keep-awake

---

## Changelog

| Дата | Изменение |
|------|-----------|
| 2026-06-29 | Первичное исследование: все подходы, план прототипа |
