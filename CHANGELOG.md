# Changelog

All settings are in **Mods → Void Sky Fix → Config** and apply immediately.

## [1.0.9]

- New setting **Fix void darkening in other mods** (`globalVoidFogFix`). Some mods copy the vanilla void darkening and bypass this mod's fix. For example, Biomes O' Plenty biome fog (Frost Forest and others) turned almost black below Y=32. With this setting on, those mods darken exactly like vanilla does with this mod's fix.
- Only **Remove black box** and **Remove lower sky disc** are on by default. Everything else is opt-in. An existing `config/voidskyfix.cfg` keeps its values; delete it to get the new defaults.

## [1.0.8]

- New setting **Directional sunset** (`directionalSunset`). The sunrise and sunset glow is computed for each direction of the sky, replacing the vanilla glow fan. The horizon no longer recolors when you turn the camera.
- New setting **High detail sky** (`highDetailSky`). A denser sky mesh near the horizon, which removes faint lines in the gradient at sunrise and sunset.

## [1.0.7]

- Gradient sky: smoother transition at the horizon, with no visible line where the upper and lower halves meet.

## [1.0.6]

- New setting **Gradient sky** (`gradientSky`). The sky is drawn as one smooth sphere: sky color above and below, fog color at the horizon. There's no seam between the upper and lower sky, and it doesn't depend on fog settings, including Angelica's.
- New setting **Void darkens whole sky** (`voidDarkenWholeSky`). Falling into the void darkens the whole sky, not only the horizon.
- New setting **Fix fog brightness** (`fixFogBrightness`). Fixes a vanilla bug at render distance over 16 chunks: in the dark, fog got up to 2x brighter, causing a turquoise sky below Y=0 and a flash when flying up.
- New setting **Debug: fog info on F3** (`debugFog`) for diagnosing sky colors with other mods.

## [1.0.5]

- New setting **Uniform sky color** (`uniformSkyColor`). The fog color matches the sky color, so the sky is one color with no pale horizon band or white area below you.
- Settings have readable names and tooltips (English and Russian). Hover over a setting's name to see what it does.

## [1.0.4]

- New setting **Void darkening depth** (`voidDarkeningDepth`). The sky fades to black smoothly from Y=0 down to Y=-depth instead of cutting off within one block.

## [1.0.3]

- Every fix has its own toggle: **Remove black box**, **Remove lower sky disc**, **Remove void darkening**.
- In-game config screen in the mod list. Changes apply without restarting.

## [1.0.2]

- First version: removes the black box under the horizon, the blue lower sky disc and the vanilla void darkening in void and skyblock worlds.
