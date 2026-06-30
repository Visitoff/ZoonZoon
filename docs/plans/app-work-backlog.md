# ZoonZoon 2 — Work Backlog

> **Product spec:** [app-product-spec.md](./app-product-spec.md)  
> **Design:** [design-references.md](./design-references.md)  
> Индекс: [docs/README.md](../README.md)

**Обновлено:** 2026-06-29

---

## Легенда

| Маркер | Значение |
|--------|----------|
| `[ ]` | не начато |
| `[~]` | в работе |
| `[x]` | готово |
| `[-]` | отложено |
| `[?]` | нужно уточнение |

---

## Фаза UI-1 — Shell + Home (ПЕРВЫМ)

- [x] Bottom navigation — **Home center tab enlarged/highlighted**
- [x] Onboarding: **gamepad only vs gamepad+phone** choice
- [x] Settings: **Vibration target** toggle
- [x] Home: **neomorphic button + ripples** ([design-references.md](./design-references.md))
- [x] Home: gamepad icon, slider, **phone vibration**
- [x] Modal «Как подключить геймпад»
- [x] i18n scaffold (**en + ja**)

---

## Фаза UI-2 — Patterns + Settings

- [x] **12 presets:** Steady, Pulse, Wave, Volcano, Heartbeat, Earthquake, Ripple, Storm, Breeze, Hammer, Cascade, Spark
- [x] **Custom pad recorder** — X=freq, Y=intensity, save dialog (**заменить** frame editor)
- [x] **Playlist** — очередь, reorder, play all, save/load
- [x] Settings: theme, language en/ja, review link

---

## Фаза UI-3 — Stubs

- [x] AI screen placeholder
- [x] Multiplayer screen placeholder (UI без backend)

---

## Фаза UI-4 — Multiplayer backend

- [ ] Host + 1 guest, private/public rooms, chat, remote vibration sync

---

## Фаза 1 — Gamepad core

- [ ] Сильнее / быстрее iOS, стабильность, PS/Xbox
- [?] Фоновая вибрация → [background-vibration-ios.md](./background-vibration-ios.md)

---

## iOS / платформа

- [ ] TestFlight: Apple agreement в ASC

---

## Out of scope

- Music mode, paywall (placeholder only in Settings)

---

## Changelog

| Дата | Изменение |
|------|-----------|
| 2026-06-29 | v1.3: 12 patterns, pad recorder, playlist, en+ja, design v1 |
