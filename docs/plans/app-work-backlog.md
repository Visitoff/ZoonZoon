# ZoonZoon 2 — Work Backlog

> **Product spec:** [app-product-spec.md](./app-product-spec.md)  
> **Design:** [design-references.md](./design-references.md)  
> Индекс: [docs/README.md](../README.md)

**Обновлено:** 2026-08-07

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
- [x] Multiplayer screen placeholder (UI без backend) — **позже заменим на Browser**

---

## Фаза UI-4 — In-app Browser (вместо Multiplayer)

Заменяет вкладку Multiplayer. Цель: смотреть веб + параллельно вибрировать (геймпад/телефон).

- [ ] Экран Browser вместо Multiplayer tab (placeholder → WebView)
- [ ] Старт: Google Search (`https://www.google.com`)
- [ ] URL bar + назад / вперёд / reload
- [ ] Sticky native vibrate controls на экране (on/off, intensity; без JS-bridge к сайтам — App Store 4.7.2)
- [ ] iOS: `WKWebView` / Android: WebView
- [ ] Age rating / Review Notes: companion browser + native haptics (не thin Safari wrapper)

---

## Фаза UI-5 — Multiplayer backend

- [-] Host + 1 guest, private/public rooms, chat, remote vibration sync — **отложено** (Browser приоритетнее)

---

## Фаза 1 — Gamepad core

- [ ] Сильнее / быстрее iOS, стабильность, PS/Xbox
- [?] Фоновая вибрация → [background-vibration-ios.md](./background-vibration-ios.md)

---

## iOS / платформа

- [ ] TestFlight: Apple agreement в ASC
- [x] Fix Leave a review (iOS 18+ `openURL:options:completionHandler:`)

---

## Out of scope

- Music mode, paywall (placeholder only in Settings)

---

## Changelog

| Дата | Изменение |
|------|-----------|
| 2026-08-07 | Multiplayer backend отложен; вместо него In-app Browser (Google + vibrate) |
| 2026-06-29 | v1.3: 12 patterns, pad recorder, playlist, en+ja, design v1 |
