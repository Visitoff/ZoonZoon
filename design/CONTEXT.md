# CONTEXT.md — Jetpack Compose target

## Strict constraints (must follow)
- Never hardcode dp values if a token exists within ±2dp (use the token).
- Never flatten layout hierarchy.
- Prefer Column/Row over Box unless `kind` is `overlay` or `absolute`.
- Text must use typography tokens resolved from the token definitions — no inline TextStyle literals.
- Text content must come from `stringRef` (key as resource id, value as fallback literal).

## v0.4 scope notes (read before applying rules)
- `tokens.json.typography` is intentionally empty in v0.4 — read the inline `typography` field on each text leaf for family/size/weight/lineHeight/letterSpacing. Treat the leaf-level typography as authoritative; emit a Compose `TextStyle` from those values inline. Re-introduction of named typography tokens is tracked for v0.5.
- `token-distance-high` (see Warnings handling) fires only on `gap`, `padding`, and `cornerRadius` ValueTokens. **Sizing dimensions** (`sizing.w`, `sizing.h`) are raw pixel values with no `tokenDistance` — when emitting Compose, cross-check fixed dimensions against `tokens.json.spacing` yourself and prefer the token name if the value matches within ±2dp.
- Warnings tagged `(v0.2+)` below are **not emitted** by the v0.4 extractor; treat their absence as the default behavior described.
- `_meta.tokensSource` indicates where the entries in `tokens.json` came from:
    - `figma-variables` — the file has published Variables; names like `color.primary` / `spacing.md` are the designer's semantic intent, use them verbatim.
    - `inferred-from-frequency` — the file has NO Variables; the bundle synthesized value-derived names (`spacing.8`, `color.1c1c1c`) by counting actual usage in the IR. Names are stable + unique but NOT semantic. Still use them in Compose to keep the code tokenized rather than littering with literals; rename when the designer publishes real Variables.
    - `none` — the file has neither Variables nor enough repeated raw values to infer. Emit literals; do not invent token names.
- `stringRef.key` is OMITTED on text leaves where the Figma layer name is unusable (numeric-only like `"5"` or `"217.24.165.183"`, pure punctuation, shorter than 3 chars) OR when the same key collides with multiple distinct values across screens (e.g. layer named `"label"` used for "Client", "Host", "5201" on different nodes — emitting `R.string.label` would resolve to only one value and render the wrong text everywhere else). In both cases the leaf still carries `stringRef.value` for the literal. Emit the value as a string literal (no `R.string.*` lookup); if that string repeats elsewhere in the export, lift it to a single `strings.xml` entry yourself with a name you choose. Colliding keys land in `_meta.warnings` as `strings-collision:<key>`.
- `semanticRole: "icon"` on a leaf node does NOT guarantee renderable vector data. Many Figma "icons" are decomposed into raw `Rectangle`/`Ellipse`/`Vector` children without SVG path data or `image.url`. Before generating `Icon()`, check the leaf has a `background` color (treat as a colored `Box` placeholder) OR an `image.url` (use `AsyncImage` with Coil). If neither, fall back to a same-sized `Spacer` with a TODO comment — the design intent is geometry-only and needs a real asset later.
- INSTANCE nodes (Figma component instances placed on a screen) carry `componentId` (a string like `"4828:148"`) and optionally `componentName` (the source component's display name). These fields link the screen IR node back to the corresponding entry in `components/inventory.json` — cross-reference to find the canonical component definition, variants, or shared token bindings. Non-instance nodes never carry these fields.

## Layout interpretation (IR kind → Compose)
- stack + axis=horizontal    → Row
- stack + axis=vertical      → Column
- overlay                    → Box (children stacked by array index: 0 = bottom)
- absolute                   → Box child with Modifier.offset(x.dp, y.dp)
- kind:stack/overlay/absolute may carry a `background` ColorToken — render via Modifier.background(color, shape)

## Sizing mapping
- sizing.width=fill          → Modifier.fillMaxWidth()
- sizing.width=hug           → Modifier.wrapContentWidth()
- sizing.width=fixed         → Modifier.width(<w>.dp)
- sizing.flex: N (N>0, stack child) → Modifier.weight(Nf)
- same mapping for height

## Alignment mapping
- justify → Arrangement on the main axis:
    start/center/end/space-between/space-around
    → Arrangement.Start / Center / End / SpaceBetween / SpaceAround
- align   → Alignment on the cross axis:
    Row:    start=Top, center=CenterVertically, end=Bottom, stretch=use Modifier.fillMaxHeight on children
    Column: start=Start, center=CenterHorizontally, end=End, stretch=use Modifier.fillMaxWidth on children

Note: when `justify` is a space-distribution mode (`space-between`, `space-around`, `space-evenly`),
the IR will not carry a `gap` field. Do not invent one.

## Semantic roles (prefer Material3 components)
- button      → Button / OutlinedButton / FilledTonalButton (read background+text color tokens)
- input       → MUST use Material3 `OutlinedTextField`. Do NOT substitute `BasicTextField + Box`. Children text nodes go in the `placeholder` slot (`placeholder = { Text(...) }`). Trust intrinsic sizing — padding/height stripped from IR per §13.1 #3.
- icon        → Icon (if vector/svg) or Image (for raster)
- image       → Image with ContentScale matching `image.fit`
- text        → Text

## Image rendering
Remote images are loaded via Coil (see Approved external dependencies below).
IR `image.fit` maps to Compose `ContentScale`:
- fit=cover   → ContentScale.Crop
- fit=contain → ContentScale.Fit
- fit=fill    → ContentScale.FillBounds

## Approved external dependencies
These are considered "standard" for the purposes of the no-external-deps rule:
- io.coil-kt:coil-compose            (for remote images via image.url)
- androidx.compose.material:material-icons-extended

## Material3 component fields
When a node's `semanticRole` implies a specific Material3 component (e.g. `input`),
fields that the component manages (padding, min-height, border width, ripple) may have been
stripped from the IR by normalization. Do not invent padding or height to compensate —
trust the component's defaults. If the design genuinely required non-standard sizing,
the IR will carry a `"material3-override-needed"` warning (v0.2+ feature).

## Warnings handling
When a node has `warnings[]`, act on them:
- "unnamed"                       → generate a descriptive name from parent context
- "name-inferred"                 → name is auto-generated; feel free to rename for readability
- "detached-instance"             → treat as a standalone component; don't assume a library component
- "spacing-no-token-match"        → emit the raw value with a `// WARNING:` comment noting the drift
- "token-distance-high"           → SUBSTITUTE the IR's raw `value` with the token's resolved value from §4. The dp/sp expression in code MUST be the token's value, never the raw. The raw value belongs in the comment ONLY. Worked example for `padding.vertical: {value: 12, token: "spacing.sm"}` (where §4 has `spacing.sm: 8`):
    WRONG:  vertical = 12.dp  // WARNING: token-distance-high; using token
       (the dp value 12 contradicts the "using token" comment — never do this)
    RIGHT:  vertical = 8.dp   // WARNING: token-distance-high — IR raw=12, token spacing.sm=8; using token
       (the dp value 8 matches the resolved token; raw 12 is mentioned only in the comment)
    Apply the same substitution to ALL fields (padding, gap, sizing, etc.) carrying this warning.
- "mixed-auto-layout-resolved"    → trust the normalized structure; do not second-guess
- "layout-mode-none-inferred"     → the original Figma frame had no auto-layout but its children are non-overlapping; the IR synthesized a vertical `kind:stack` with start/start alignment and gap:0. Emit Compose `Column { ... }` per the IR — DO NOT try to reconstruct absolute positions from `x/y`. This is the most common warning on real Figma files where the designer used manual positioning rather than auto-layout, and the IR's inferred Column is the correct rendering 95% of the time.
- "material3-override-needed"     → (v0.2+) wrap in BasicTextField / custom component to preserve IR sizing

## If uncertain
- Prefer vertical stacking (Column) over horizontal.
- Prefer start alignment.
- Prefer tokens over raw values.
- Prefer simpler layouts (Row/Column) over Box wherever the IR allows.