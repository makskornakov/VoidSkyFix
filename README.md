# Void Sky Fix

Client-side Forge mod for Minecraft 1.7.10 that fixes the sky in void / skyblock worlds.

In vanilla, when the player is low in the world, the sky turns black: a black box is drawn under the horizon (below Y=63), the lower sky disc shows up as a blue square beneath you, and void fog fades the sky to black below about Y=32. This mod removes each of those, and each fix has its own toggle.

## Settings

Open **Mods → Void Sky Fix → Config** in game. Changes apply immediately. They are saved to `config/voidskyfix.cfg`.

| Option | Default | What it does |
|---|---|---|
| `removeBlackBox` | `true` | Removes the black box drawn under the horizon when you are below Y=63. |
| `removeLowerSkyDisc` | `true` | Removes the lower sky disc (the blue square below you). |
| `removeVoidDarkening` | `true` | Turns off vanilla void fog darkening (sky fading to black below ~Y=32). |
| `voidDarkeningDepth` | `16` | Used only with `removeVoidDarkening`: the sky fades to black smoothly from Y=0 down to Y=-depth. `0` = hard cut at Y=0. |

## Installation

Put the jar in `mods/`. Only the client needs it. It doesn't need to be installed on the server, and servers don't require it from players.

## Building

```sh
VERSION=1.0.4 ./gradlew build   # or tag the commit (git tag 1.0.4) and run ./gradlew build
```

The jar is written to `build/libs/voidskyfix-<version>.jar`.

Built on the [GTNH ExampleMod 1.7.10](https://github.com/GTNewHorizons/ExampleMod1.7.10) template.
