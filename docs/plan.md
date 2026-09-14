# ZoonZoon — план для агента

**Обновлено:** 2026-09-15  
**Стек:** Kotlin Multiplatform, Compose Multiplatform, Android + iOS  
**applicationId:** `com.ZoonZoon`

## Источник правды

Единственный дизайн-файл:

https://www.figma.com/design/z5m2GpAVF3qE2sDwV2MADs/ZoonZoon-2026--Copy-

`fileKey`: `z5m2GpAVF3qE2sDwV2MADs`

Перед версткой любого экрана вызвать Figma MCP `get_design_context` на указанный `node-id`. Скриншот и HTML-референс — ориентир, не копипаста в React/Tailwind. Верстать в существующих компонентах проекта (`Fig*`, `Figma*`, `ui/glass`).

Хост разработки — Windows. Android проверять на эмуляторе (`Pixel_10_Pro_XL`, правило `.cursor/rules/android-run-verify.mdc`). iOS на этой машине не собирается: не писать «проверено 1 в 1» для iOS.

## Как работать

- Одна задача за раз, в порядке ниже. Не смешивать стеклянный фундамент с пейволами.
- Не добавлять темы (Light/Dark/Coral/Aqua/Peach). В Figma Settings блок Theme есть — **пропустить**.
- Не возвращать вкладки Multiplayer / AI / Browser. Нижний бар — **3 кнопки**.
- Не трогать фоновую вибрацию iOS.
- После UI-задачи: собрать `:composeApp:installDebug`, открыть экран, снять скриншот, сравнить с Figma (привести 1344×2992 к логическим ~375×812).
- Стекло: iOS 26+ = нативный Liquid Glass; Android и iOS < 26 = плагин v2 (fallback). Не рисовать стекло картинкой.

## Сейчас в коде

| Что | Где | Состояние |
|-----|-----|-----------|
| 5 вкладок | `ui/shell/AppTab.kt`, `ZoonZoonBottomBar.kt` | Multi / AI / Home / Patterns / Settings |
| Стекло Android | `ui/glass/LiquidGlass.android.kt` | Kyant Backdrop `io.github.kyant0:backdrop:1.0.3` |
| Стекло iOS | `LiquidGlass.ios.kt`, `NativeLiquidGlass.ios.kt` | no-op, `isLiquidGlassEnabled() = false` |
| Haze | `FigmaKit.kt`, `ZoonZoonAppShell.kt` | blur-fallback, не то же самое что liquid glass |
| Вибрация | `gamepad/engine/VibrationEngine.kt` | цикл ~16 ms в `rememberCoroutineScope()` (Main) |
| Settings | `ui/settings/SettingsScreen.kt` | список, есть theme — **убрать** |
| Patterns | `ui/patterns/PatternsScreen.kt` | пресеты есть; плейлисты = PNG-карточки; recorder есть, но ломается |
| Home волны | `ui/home/Player.kt` | статичные `home_eq_hi` / `home_eq_lo` |
| Языки | `i18n/AppLanguage.kt` | только `en-US`, `ja` |
| Подписки / review / lock | — | нет |

---

## Задачи

### 1. Liquid Glass plugin v2 — Android + iOS < 26

**Зачем:** стекло должно работать везде, кроме нативного iOS 26 (это задача 2).

**Сейчас:** Backdrop только в `androidMain`. iOS actual пустой. В gradle `backdrop = "1.0.3"`.

**Сделать:**

1. Поднять Kyant Backdrop до **v2** (актуальный артефакт/changelog библиотеки — проверить перед bump).
2. Реализовать `iosMain` actuals: `ProvideGlassBackdrop`, `glassSource()`, `liquidGlass()`, `isLiquidGlassEnabled()`.
3. На iOS 26+ этот путь **не** использовать — там задача 2. На iOS < 26 и Android — plugin v2.
4. Общий API не ломать: UI уже зовёт `isLiquidGlassEnabled()` / `Modifier.liquidGlass`.
5. Haze оставить только если без него ломается blur; не дублировать два стекла на одном элементе.

**Готово когда:** кнопки/чипы с `liquidGlass` дают живой backdrop на Android. iOS < 26 — тот же API, без заглушки. Компиляция обоих source sets проходит.

---

### 2. Native Liquid Glass на все кнопки и элементы — iOS 26+

**Сейчас:** `supportsNativeLiquidGlass() = false`, `NativeLiquidGlass` пустой.

**Сделать:**

- `iosMain`: определять iOS 26+ и рендерить системный Liquid Glass (`UIVisualEffect` / Compose interop — как принято в v2 плагина и Apple API).
- Покрыть **все** интерактивные элементы: кнопки, чипы, слайдер-thumb, bottom bar, cards, dialogs, lock, paywall.
- Ниже iOS 26 — только plugin v2 из задачи 1.
- Android — plugin v2, не эмулировать UIGlass.

**Готово когда:** ветка `supportsNativeLiquidGlass()` истинна только на iOS 26+; элементы не падают в solid fill.

---

### 3. Команда вибрации в отдельный поток

**Сейчас:** `App.kt` кормит `VibrationEngine` через `rememberCoroutineScope()` (Main). Цикл 16 ms шлёт HID/haptics на том же скоупе — дёргает UI.

**Сделать:**

- Выделенный dispatcher/поток для `sendVibrationCommand` + phone vibrate (не Main).
- Старт/стоп/смена intensity/pattern остаются мгновенными с UI; в движок уходят потокобезопасно.
- Не пропустить stop: при disable/disconnect/отмене Job моторы гасятся.
- Тесты в `composeApp/src/commonTest/.../gamepad/engine/` не сломать; при необходимости запускать engine с тестовым dispatcher.

**Готово когда:** UI-поток не считает кадры паттерна; stop по-прежнему глушит моторы.

---

### 4. Нижний бар на 3 кнопки

Figma: [node 4075:6825](https://www.figma.com/design/z5m2GpAVF3qE2sDwV2MADs/ZoonZoon-2026--Copy-?node-id=4075-6825)

Компонент `-bottom-bar`: **200×70**, три круга 60×60 (Home / Patterns / Settings — сверить иконки в Figma).

**Сделать:**

- `AppTab` оставить три значения. Удалить Multiplayer и AI из навигации и `PlaceholderScreens`.
- Бар по макету: ширина 200, отступы 5, gap 5. Не растягивать старый 330-wide `nav_bg` на три кнопки.
- Стекло: plugin v2 или native iOS 26, в зависимости от платформы.

**Готово когда:** в приложении три вкладки, визуал = Figma, заглушки Multi/AI не открываются.

---

### 5. Меню настроек

Figma: [node 4065:5409](https://www.figma.com/design/z5m2GpAVF3qE2sDwV2MADs/ZoonZoon-2026--Copy-?node-id=4065-5409)

**Верстать:**

- Vibration target: три горизонтальных чипа Phone / Phone + controller / Controller (иконки из макета, не emoji-радио).
- Language: строка 70 px, иконка translate, текущий язык, шеврон. Выбор языка — sheet/picker, не длинный список на экране.
- Версия (`Version …`) + sign-in (компонент `-sign-states`).
- Нижняя широкая кнопка из макета (restore / manage — текст взять из Figma).
- Bottom bar из задачи 4.

**Не верстать:** блок Theme (Coral / Aqua / Peach / Phone). Вычистить theme UI из `SettingsScreen` и строки i18n, связанные только с темой. `AppThemeMode` не развивать.

**Готово когда:** Settings = Figma минус Theme; vibration target и language работают.

---

### 6. Меню паттернов

Figma: [node 4075:6903](https://www.figma.com/design/z5m2GpAVF3qE2sDwV2MADs/ZoonZoon-2026--Copy-?node-id=4075-6903)

Переверстать `PatternsScreen.kt` 1:1: фильтры All / Rhytmic / Fast / Pulsating, сетка пресетов, Custom, Your playlists. Токены, радиусы, отступы — с Figma, не с текущих «почти».

Связанные задачи 7 и 8 — логика внутри этого экрана, не отдельные макеты «с нуля».

---

### 7. Плейлисты — интерактив, не картинки

Figma: [node 2106:984](https://www.figma.com/design/z5m2GpAVF3qE2sDwV2MADs/ZoonZoon-2026--Copy-?node-id=2106-984)

**Сейчас:** `PlaylistCard` рисует `playlist_card_stripes` / `playlist_card_ripple` и имя сверху. `GamepadViewModel` очередь уже умеет.

**Сделать:** карточку собрать из слоёв макета: фон-волны, название, стек превью-паттернов, `+N`. Tap открывает/запускает плейлист; `+` создаёт. Reorder / длительность / play all — живые, не декорация.

Убрать зависимость карточки от статичных PNG, если они не совпадают с интерактивной сборкой.

**Готово когда:** карточка — Compose-дерево; play/save/load/reorder работают от UI до `VibrationEngine`.

---

### 8. Починить создание кастомного паттерна

Figma: [node 2106:972](https://www.figma.com/design/z5m2GpAVF3qE2sDwV2MADs/ZoonZoon-2026--Copy-?node-id=2106-972)

**Контракт (уже заложен):** Start Recording → pad → X = speed/freq, Y = strength → отпускание → save dialog → в «мои паттерны». Preview вибрации во время жеста.

Починить recorder (`PatternRecorderPad.kt` + VM), чтобы жест писался, сохранялся, выбирался и игрался. UI pad = макет.

**Готово когда:** полный цикл record → save → select → vibrate на Android.

---

### 9. Интерактивные волны — Home и Lock

**Сейчас:** PNG в `Player.kt`.

Заменить на живую анимацию (Canvas / shader / mesh), синхронизированную с интенсивностью и паттерном. Одинаковая система на Home и экране блокировки. Не зацикленный видеофайл.

**Готово когда:** волны движутся, реагируют на intensity/on-off, не статичный фон.

---

### 10. Экран блокировки

Figma: [node 4078:8848](https://www.figma.com/design/z5m2GpAVF3qE2sDwV2MADs/ZoonZoon-2026--Copy-?node-id=4078-8848)

Фрейм **Home lock**: полноэкранные волны, кнопка lock сверху слева, паттерн(ы) снизу, **без** нижнего бара.

**Сделать:** отдельный режим поверх Home. Lock скрывает chrome; unlock возвращает. Вибрация не обязана останавливаться. Волны — из задачи 9.

**Готово когда:** вход/выход с lock совпадает с макетом.

---

### 11. Пейволы + обучалка

Figma: [node 4092:4024](https://www.figma.com/design/z5m2GpAVF3qE2sDwV2MADs/ZoonZoon-2026--Copy-?node-id=4092-4024)

Секция **Paywalls**: онбординг + paywall overlays. Перед кодом снять `get_design_context` и пройти все фреймы секции (имена в Figma повторяются — ориентир скрин и тексты).

**Сделать:**

- Онбординг при первом запуске (страницы как в макете).
- Paywall: продукты StoreKit 2 + Play Billing. Кнопка Restore.
- Гейты: что бесплатно / что pro — по макету (лок на паттернах и т.д.).
- Пока сторы не подключены в debug — явный debug-bypass, не вечный «всё открыто» в релизе.

Делать **после** Settings/Patterns, чтобы оверлеи садились на финальные экраны.

---

### 12. Бесплатная подписка на старый функционал (уже купившие)

Юзеры, которые **уже купили** платную версию приложения, получают entitlement на старый функционал без новой оплаты.

**Сделать:**

- Detect legacy purchase: App Store original transaction / Play one-time SKU (или установленный paid build).
- Выдать permanent entitlement `legacy` = старый набор фич (Home, пресеты, custom, playlists, vibration) без активной подписки.
- Новые юзеры без покупки идут в paywall (задача 11).
- Restore purchases поднимает и legacy, и подписку.
- Не забирать доступ, если стор временно недоступен, а entitlement уже кэширован.

Зафиксировать в коде список «старого функционала», чтобы новые pro-фичи не утекли в legacy по ошибке.

---

### 13. Автозапрос ревью и оценки App Store / Play

После ценности (несколько успешных сессий вибрации / N дней), не на первом кадре.

- iOS: `SKStoreReviewController` / StoreKit `requestReview` — только прод, не TestFlight-заглушка без API.
- Android: Play In-App Review API.
- Лимиты Apple/Google: не спамить, хранить last-prompt в settings.
- Settings: ручной «Leave a review» оставить/починить deep link, это не замена автозапроса.

---

### 14. Локализации под рынки консолей

Сейчас: `en-US`, `ja`.

Добавить языки аудиторий, где живут PS/Xbox:

| Код | Рынок |
|-----|--------|
| `en` | US / UK / CA / AU (база) |
| `ja` | Япония |
| `ko` | Корея |
| `zh-Hant` | Тайвань / HK |
| `de` | Германия |
| `fr` | Франция / CA |
| `es` | Испания / LATAM |
| `pt-BR` | Бразилия |

Все пользовательские строки через `AppStrings` / compose resources. Без хардкода в UI. Системный язык при первом запуске, иначе English. Переключатель в Settings (задача 5) показывает полный список.

---

## Порядок

```
1 glass v2 (Android + iOS<26)
2 native glass iOS 26+          — можно параллельно с 3
3 vibration off Main
4 bottom bar 3 tabs
5 Settings (без тем)
6 Patterns layout
7 playlists interactive
8 custom pattern fix
9 interactive waves
10 lock screen
11 paywalls + onboarding
12 legacy entitlement
13 review prompt
14 locales
```

4 зависит от стекла. 5–10 зависят от бара. 11–12 после экранов, на которые садятся оверлеи. 14 можно параллельно с 5–10, но строки экранов лучше после их верстки.

## Вне скоупа

- Темы оформления
- Multiplayer backend, AI logic, in-app Browser
- Фоновая вибрация iOS без реального аудио
- Music mode
- iOS visual QA на этой Windows-машине
