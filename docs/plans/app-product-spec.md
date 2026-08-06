# ZoonZoon 2 — Product Spec (UI + навигация)

> **Дизайн:** [design-references.md](./design-references.md) — [Figma](https://www.figma.com/design/32ZPq34pLYgDXNrAPHgCo2/ZoonZoon)  
> **Обновлено:** 2026-06-29 (v1.3 — дизайн, паттерны, playlist, i18n)

---

## Prompt для разработки (copy-paste)

```
ZoonZoon 2 — Kotlin Multiplatform (Compose Multiplatform), iOS + Android.

НАВИГАЦИЯ: bottom bar, 5 вкладок слева направо:
  [1 Multiplayer] [2 AI] [3 Home ★] [4 Patterns] [5 Settings]

ЭКРАН 3 — HOME (главный, центральный):
  - Большая круглая кнопка вибрации по центру. Tap = start/stop вибрации.
  - Без геймпада: только телефон. С геймпадом: **выбор юзера** (только геймпад / геймпад + телефон) — спросить при первом запуске или в Settings.
  - Bottom nav: **Home по центру выделен** (больше / ярче остальных).
  - Tap главной кнопки БЕЗ блокирующего modal — вибрация телефона сразу.
  - Top-right: иконка геймпада.
      • Не подключён → tap = modal «Как подключить геймпад».
      • Подключён → disabled, alpha ~0.4, tap = no-op.
  - Под кнопкой: slider силы (0–100%).

ЭКРАН 1 — MULTIPLAYER:
  - P2P: host + ровно 1 guest (не больше).
  - Приватные комнаты с паролем + публичные комнаты (без пароля / open lobby).
  - Сценарий: host (например, девушка с геймпадом) создаёт комнату → даёт пароль guest (парню) → guest управляет вибрацией на устройстве host (геймпад и/или телефон host).
  - В комнате: текстовый чат между участниками.
  - Real-time sync вибрации (pattern, intensity, on/off) от guest к host.

ЭКРАН 2 — AI:
  - Отдельный режим in-app AI. UI-заглушка / экран-заготовка. Логику AI пока НЕ реализовывать.

ЭКРАН 4 — PATTERNS:
  - 12 preset-паттернов (Volcano, Heartbeat, … — см. список в spec).
  - Custom: запись жестом на pad (не frame editor как сейчас).
  - Playlist: очередь паттернов, play all.

ЭКРАН 5 — SETTINGS:
  - Языки v1: English (US) + 日本語 (JP).
  - Прочее: версия, privacy, restore purchases.

ТЕКУЩИЙ КОД: один экран GamepadVibratorScreen — заменить на multi-screen + bottom nav.
ДИЗАЙН: v1 mockups — светлый neomorphic, lavender/purple ON, ripples. См. design-references.md. Figma link — TBD.
```

---

## Архитектура навигации

```
┌─────────────────────────────────────────────────────────────┐
│                        App Shell                           │
│  ┌─────────────────────────────────────────────────────┐   │
│  │              Content (current tab)                   │   │
│  └─────────────────────────────────────────────────────┘   │
│  ┌──────┬──────┬──────────┬──────────┬──────────┐          │
│  │  👥  │  🤖  │   ( ● )  │    〰️    │    ⚙️    │          │
│  │Multi │  AI  │  HOME ★  │ Patterns │ Settings │  ← HOME bigger
│  └──────┴──────┴──────────┴──────────┴──────────┘          │
└─────────────────────────────────────────────────────────────┘
         Tab 1    Tab 2    Tab 3 ★     Tab 4      Tab 5
```

| # | Tab | Route | Приоритет v1 |
|---|-----|-------|--------------|
| 1 | Browser (ex-Multiplayer) | `browser` | v1.1 — WebView + vibrate; Multiplayer отложен |
| 2 | AI | `ai` | UI-заглушка в v1 |
| 3 | **Home** | `home` | **v1 MUST** |
| 4 | Patterns | `patterns` | v1 MUST |
| 5 | Settings | `settings` | v1 MUST |

**Default tab при запуске:** Home (центр).

**Bottom nav:** вкладка **Home по центру визуально выделена** — иконка/кнопка **крупнее и ярче**, остальные 4 одинаковые.

---

## Экран 3 — Home (главный)

### Layout (сверху вниз)

```
┌──────────────────────────────────────┐
│                            [🎮 icon] │  ← top-right
│                                      │
│                                      │
│            ┌────────────┐            │
│            │            │            │
│            │  VIBRATE   │            │  ← главная кнопка (toggle)
│            │            │            │
│            └────────────┘            │
│                                      │
│   ───────────●────────────────       │  ← slider силы
│   0%                    100%         │
│                                      │
├──────────────────────────────────────┤
│  👥    🤖    ●     〰️     ⚙️         │  ← bottom nav
└──────────────────────────────────────┘
```

### Главная кнопка вибрации

| Состояние | Поведение |
|-----------|-----------|
| Off | Tap → включить вибрацию (текущий паттерн + intensity со slider) |
| On | Tap → выключить (stop) |
| **Нет геймпада** | Tap → **сразу вибрирует телефон** (Taptic). Modal про геймпад **не блокирует** — только через иконку 🎮 |
| **Геймпад подключён** | Tap → вибрация на геймпад (+ опционально телефон) |

**Целевое устройство:**
- **Без геймпада:** только телефон.
- **С геймпадом:** по выбору юзера — **только геймпад** или **геймпад + телефон** (настройка в Settings, при первом подключении — onboarding prompt).

### Onboarding: выбор цели вибрации

При **первом подключении геймпада** (или первом запуске с геймпадом) — sheet/dialog:

| Опция | Поведение |
|-------|-----------|
| **Gamepad only** | Вибрация только на контроллере |
| **Gamepad + Phone** | Оба одновременно |

Сохраняется в prefs, меняется в **Settings → Vibration target**.

### Иконка геймпада (top-right)

| Состояние | Вид | Tap |
|-----------|-----|-----|
| **Не подключён** | Активная иконка (полная непрозрачность) | Modal / bottom sheet: **подробная инструкция** подключения (Bluetooth, совместимые модели PS/Xbox, MFi на iOS) |
| **Подключён** | Disabled, **alpha ~0.4**, некликабельная | **Ничего** — no-op |

### Slider силы

- Диапазон: 0–100% (маппинг на 0.0–1.0 в engine).
- Применяется в реальном времени к активной вибрации.
- Значение сохраняется между сессиями (local prefs).

### Modal «Как подключить геймпад»

Показывается **только** при tap на иконку 🎮, когда геймпад не подключён.

_(Раньше планировали modal и при tap главной кнопки — отменено: без геймпада телефон вибрирует сразу.)_

Содержимое (черновик):
1. Включи Bluetooth на телефоне.
2. Включи геймпад (PS: Share+PS, Xbox: pairing mode).
3. Подключи в Settings → Bluetooth.
4. Вернись в ZoonZoon — иконка станет полупрозрачной.
5. Поддерживаемые: DualShock 4, DualSense, Xbox Wireless…

---

## Экран 1 — Multiplayer

### Концепция

Remote control: **host + ровно 1 guest** (не больше).

Пример: девушка (host) с геймпадом создаёт комнату → парень (guest) по паролю заходит → включает/выключает вибрацию, меняет intensity/pattern на **её** телефоне/геймпаде.

### Типы комнат

| Тип | Создание | Вход |
|-----|----------|------|
| **Приватная** | Host задаёт пароль | Guest вводит room ID + пароль |
| **Публичная** | Host создаёт open room | Guest выбирает из списка / по ID без пароля |

### Функции комнаты

- [ ] Создать комнату (private / public)
- [ ] Присоединиться по ID + пароль (private) или из списка (public)
- [ ] Host + **1 guest** max (не больше одного)
- [ ] Host: статус геймпада/телефона, guest connected
- [ ] Guest: UI как Home — кнопка вибрации + slider → команды уходят на host
- [ ] **Чат** внутри комнаты (текст, real-time)
- [ ] Sync: on/off, intensity, pattern (минимум для v1 multiplayer)
- [ ] Host может kick guest / закрыть комнату

### Технические заметки (не для v1 day-1)

- Transport: WebRTC data channel / WebSocket через relay server (P2P через NAT сложно без TURN).
- Host = authoritative: только host шлёт команды на `PlatformGamepadController`.
- Security: пароль комнаты, rate limit, модерация чата — позже.

---

## Экран 2 — AI

| | |
|---|---|
| **v1** | Экран-заглушка: заголовок «AI Mode», описание «Coming soon», placeholder UI |
| **Позже** | In-app AI для генерации/управления паттернами, сценариями, голосом и т.д. |
| **Сейчас** | **Не программировать** логику AI |

---

## Экран 4 — Patterns

### Preset-паттерны (12 штук, v1)

Grid/list, tap = active pattern для Home.

| # | ID | Название (EN) | JP (черновик) | Характер |
|---|-----|---------------|---------------|----------|
| 1 | `steady` | Steady | 一定 | постоянная |
| 2 | `pulse` | Pulse | パルス | ритмичные импульсы |
| 3 | `wave` | Wave | ウェーブ | плавная синусоида |
| 4 | `volcano` | Volcano | 火山 | нарастание → всплеск → затухание |
| 5 | `heartbeat` | Heartbeat | 心拍 | dub-dub пауза |
| 6 | `earthquake` | Earthquake | 地震 | хаотичные всплески |
| 7 | `ripple` | Ripple | 波紋 | затухающие кольца |
| 8 | `storm` | Storm | 嵐 | быстрые bursts + паузы |
| 9 | `breeze` | Breeze | そよ風 | лёгкая, частая |
| 10 | `hammer` | Hammer | ハンマー | редкие сильные удары |
| 11 | `cascade` | Cascade | 滝 | ступеньки вниз по силе |
| 12 | `spark` | Spark | スパーク | короткие вспышки |

_Алгоритмы — реализация поверх `VibrationPattern`; имена и иконки в UI обязательны._

### Custom pattern — запись жестом (NEW, заменяет текущий frame editor)

**Не** построчный список frames как сейчас.

```
[ Start Recording ]  →  появляется прямоугольный pad
```

| Ось pad | Параметр |
|---------|----------|
| **X** (влево–вправо) | частота / скорость вибрации |
| **Y** (вниз–вверх) | сила (intensity) |

**Flow:**
1. Юзер жмёт **«Начать запись»**.
2. Появляется **прямоугольник** (touch pad).
3. Водит пальцем — в реальном времени вибрация (preview на телефоне/геймпаде).
4. **Отпускает палец** → запись завершена, паттерн = sampled curve `(t → frequency, intensity)`.
5. Dialog: **«Сохранить паттерн?»** → имя → в «Мои паттерны».

Техника: sample points каждые ~16–32 ms while finger down → resample to loopable pattern → store as custom.

### Playlist вибраций

Отдельная секция на экране Patterns (или sub-tab):

- Юзер **добавляет** сохранённые presets + custom в очередь.
- Drag to reorder.
- **Play** — проигрывает паттерны **подряд** (auto-advance).
- Save/load named playlists локально.

_Похоже на текущую идею «плейлист», но явный UX: очередь + play all._

Active pattern (single) vs playlist (sequence) — на Home кнопка вибрации использует **либо** выбранный preset, **либо** активный playlist mode.

---

## Экран 5 — Settings

| Пункт | v1 |
|-------|-----|
| **Vibration target** | Gamepad only / Gamepad + Phone (если геймпад подключён) |
| **Язык** | **English (US)** + **日本語** |
| **Тема** | Light / Dark / System |
| **Оставить отзыв** | App Store / Play Store deep link |
| **Продлить подписку** | placeholder |
| **Restore purchases** | placeholder |
| **Версия** | build number |
| **Privacy / Terms** | ссылки |

**i18n:** `compose-resources`; default `en`, locales `en`, `ja`.

---

## Приоритет реализации

### Phase UI-1 — Shell + Home + Design v1

- [ ] Bottom navigation (5 tabs)
- [ ] Home: neomorphic button, ripples, purple ON (см. design-references.md)
- [ ] Home: gamepad icon, intensity slider, phone vibration
- [ ] Modal «Как подключить геймпад»
- [ ] i18n scaffold (en + ja)

### Phase UI-2 — Patterns + Settings

- [ ] **12 preset patterns** (алгоритмы + иконки)
- [ ] **Custom pad recorder** (жест, save dialog) — заменить CustomPatternEditor
- [ ] **Playlist** (очередь, reorder, play all, save)
- [ ] Settings: theme, language en/ja, review link

### Phase UI-3 — Stubs

- [ ] AI screen placeholder
- [ ] Multiplayer screen placeholder (UI без backend)

### Phase UI-4 — Multiplayer backend

- [ ] Room server / signaling
- [ ] Chat
- [ ] Remote vibration sync

### Phase UI-5 — AI logic

- [ ] TBD

---

## Связь с текущим кодом

| Сейчас | Нужно |
|--------|-------|
| `GamepadVibratorScreen` — один scroll | Разбить на 5 экранов + `Scaffold` + `NavigationBar` |
| `GamepadViewModel` — один VM | Shared VM или per-screen; gamepad state — singleton/service |
| Только gamepad vibration | + phone vibration platform API |
| Один controller | Multiplayer позже на host-side |

---

## Решения продукта (зафиксировано)

| # | Вопрос | Решение |
|---|--------|---------|
| 1 | Home без геймпада | **Только телефон.** Modal про геймпад — с иконки 🎮 |
| 2 | Multiplayer | **1 guest** на комнату |
| 3 | Music mode | **[-] отменено** |
| 4 | Home в bottom nav | **Выделен по центру** (крупнее, ярче) |
| 5 | Геймпад + телефон | **Выбор юзера** (onboarding + Settings) |
| 6 | Языки v1 | **EN (US) + JA** |
| 7 | Presets | **12 штук** (Volcano, Heartbeat, …) |
| 8 | Custom pattern | **Pad recorder** (X=freq, Y=intensity), save dialog |
| 9 | Playlist | очередь паттернов, play all |
| 10 | Дизайн | **v1 mockups** — light neomorphic purple; Figma TBD |

---

## Out of scope (отменено)

- **Music mode** — вибрация под Spotify/музыку (2026-06-29)

---

## Music Mode — архив _(не делаем)_

<details>
<summary>Исследование (закрыто)</summary>

Spotify напрямую на iOS нельзя — sandbox. Рассматривали in-app player и mic mode — **решение: не делаем.**

</details>

---

## Changelog

| Дата | Изменение |
|------|-----------|
| 2026-06-29 | Первая версия product spec от создателя продукта |
| 2026-06-29 | v1.4: vibration target choice; Home nav highlighted |
