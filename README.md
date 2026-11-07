# Void Sky Fix

Client-side Forge mod for Minecraft 1.7.10 that fixes the sky in void / skyblock worlds.

In vanilla, when the player is low in the world, the sky turns black: a black box is drawn under the horizon (below Y=63), the lower sky disc shows up as a blue square beneath you, and void fog fades the sky to black below about Y=32. This mod removes each of those, and each fix has its own toggle.

## Settings

Open **Mods → Void Sky Fix → Config** in game. Changes apply immediately. By default only the black box and the lower sky disc are removed; everything else is opt-in. They are saved to `config/voidskyfix.cfg`.

| Option | Default | What it does |
|---|---|---|
| `removeBlackBox` | `true` | Removes the black box drawn under the horizon when you are below Y=63. |
| `removeLowerSkyDisc` | `true` | Removes the lower sky disc (the blue square below you). |
| `removeVoidDarkening` | `false` | Turns off vanilla void fog darkening (sky fading to black below ~Y=32). |
| `voidDarkeningDepth` | `16` | Used only with `removeVoidDarkening`: the sky fades to black smoothly from Y=0 down to Y=-depth. `0` = hard cut at Y=0. |
| `globalVoidFogFix` | `false` | Used only with `removeVoidDarkening`: also applies it to mods that copy the vanilla void darkening (for example, Biomes O' Plenty biome fog turning black below Y=32). |
| `uniformSkyColor` | `false` | Makes the fog color match the sky color, so the sky is one color everywhere with no pale horizon band. Distant terrain also fades into the sky color. |
| `fixFogBrightness` | `false` | Fixes a vanilla bug at render distance over 16 chunks: in the dark, fog gets up to 2x brighter (turquoise sky below Y=0, a flash when flying up). |
| `gradientSky` | `false` | Draws the sky as a smooth sphere: sky color above and below, fog color at the horizon. No seam between the upper and lower sky, and it doesn't depend on fog settings. |
| `voidDarkenWholeSky` | `false` | Used only with `gradientSky`: void darkening darkens the whole sky. Off = only the horizon and below. |
| `directionalSunset` | `false` | Used only with `gradientSky`: the sunrise/sunset glow is computed for each direction of the sky, replacing the vanilla glow fan and the fog that recolors when you turn the camera. |
| `highDetailSky` | `false` | Used only with `gradientSky`: a denser sky mesh near the horizon, which removes faint lines in the gradient at sunrise and sunset. |
| `debugFog` | `false` | Diagnostics: shows the fog color at each rendering stage on the F3 screen. |

If a resource pack has a custom sky (for example OptiFine Custom Sky), it already covers the sky above and below you, so you can turn the fixes off if anything looks wrong.

## Installation

Put the jar in `mods/`. Only the client needs it. It doesn't need to be installed on the server, and servers don't require it from players.

## Building

```sh
VERSION=1.0.4 ./gradlew build   # or tag the commit (git tag 1.0.4) and run ./gradlew build
```

The jar is written to `build/libs/voidskyfix-<version>.jar`.

Built on the [GTNH ExampleMod 1.7.10](https://github.com/GTNewHorizons/ExampleMod1.7.10) template.

## License

[MIT](LICENSE)
