# ZoonZoon — Design References

> **Обновлено:** 2026-06-29  
> Product spec: [app-product-spec.md](./app-product-spec.md)

---

## Figma (source of truth)

**Файл:** [ZoonZoon — Figma](https://www.figma.com/design/32ZPq34pLYgDXNrAPHgCo2/ZoonZoon?node-id=0-1)

| Поле | Значение |
|------|----------|
| `fileKey` | `32ZPq34pLYgDXNrAPHgCo2` |
| Страница | `🌌 Cover` (node `2:1327`) |
| Showcase frame | `2:1358` |

### Что уже есть в Figma

Сейчас в файле **только Cover / Showcase** (лого + ripples). Экранов Home / Patterns / nav **ещё нет** — их делаем по product spec + [v1 mockups](../design/v1-onboarding-mockups.png). Когда нарисуешь Home во Figma — дай node-id, подтянем точные размеры.

---

## Design tokens (из Figma Showcase)

Для [`Color.kt`](../../composeApp/src/commonMain/kotlin/com/seashore/zoonzoon/gamepad/theme/Color.kt):

| Token | Hex / value | Использование |
|-------|-------------|---------------|
| `backgroundGradientStart` | `#F7E7FF` rgb(247, 231, 255) | фон Home |
| `backgroundGradientEnd` | `#ECF0F3` rgb(236, 240, 243) | фон Home |
| `backgroundFallback` | `#F0F0F3` | solid fallback |
| `accentPurpleStart` | `#CA5AFF` | gradient UI, ON state |
| `accentPurpleEnd` | `#7E6BF3` | gradient UI, ON state |
| `rippleRing` | `#E3E6EC` @ 50% opacity | concentric circles |
| `brandTextOnDark` | `#FFFFFF` | ZOONZOON на тёмной плашке |
| `brandBar` | `#000000` | logo bar (cover only) |

**Typography (Figma):** Gilroy Bold, 18sp, **letter-spacing 7px**, UPPERCASE для бренда.

**Gradient background (Compose):**
```kotlin
// linear ~ horizontal wash: #F7E7FF → #ECF0F3
Brush.linearGradient(listOf(Color(0xFFF7E7FF), Color(0xFFECF0F3)))
```

**Accent gradient (кнопка ON, акценты):**
```kotlin
Brush.verticalGradient(listOf(Color(0xFFCA5AFF), Color(0xFF7E6BF3)))
```

**Ripple rings:** stroke `#E3E6EC`, opacity 0.5, circular borders (neomorphic ripples).

### Assets (Figma MCP, ~7 days URL)

| Asset | Node | Note |
|-------|------|------|
| Wave icon | `2:1435` | vibration logo mark |
| Logo subtract shape | `2:1439` | brand bar mask |

_При импорте в проект — экспорт SVG/PNG в `composeApp/.../resources`._

---

## Референс v1 (онбординг PNG)

Файл: [v1-onboarding-mockups.png](../design/v1-onboarding-mockups.png)

Дополняет Figma: **экраны телефона** (OFF/ON button, gamepads, hands).

| Элемент | Описание |
|---------|----------|
| **Кнопка** | Neomorphic circle, OFF white / ON purple glow |
| **Ripples** | Concentric waves (совпадает с Figma Cover) |
| **Онбординг** | CHECK VIBRATION, UNLIMITED VIBRATION, FOR EVERYONE, FOR ENJOY |

### Home v2 (нет в Figma — из product spec)

- Bottom nav, Home tab **крупнее по центру**
- Slider силы
- Иконка геймпада top-right
- Gilroy или fallback: system sans с wide tracking

---

## Конкуренты

| # | App | Что взять |
|---|-----|-----------|
| 1 | Vibrator Massage Controller | big trigger UX |
| 2 | ZoonZoon v1 / Figma Cover | purple ripples, neomorphic |

---

## Changelog

| Дата | Изменение |
|------|-----------|
| 2026-06-29 | v1 PNG mockups |
| 2026-06-29 | **Figma linked** — tokens из Showcase frame |
