<div align="center">

# EternalExchange

**Equivalent-exchange alchemy for Fabric.** Learn items, burn them into EMC, and transmute the world back out of it — on Minecraft 1.21.1.

[![Build](https://github.com/whoisaldo/EternalExchange/actions/workflows/build.yml/badge.svg)](https://github.com/whoisaldo/EternalExchange/actions/workflows/build.yml)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-brightgreen)](https://fabricmc.net/use/installer/)
[![Loader](https://img.shields.io/badge/Loader-Fabric-blue)](https://fabricmc.net)
[![License](https://img.shields.io/badge/License-MIT-yellow)](LICENSE)

</div>

---

EternalExchange is a standalone Fabric mod built around one idea: **everything has a value.** Every item in the game gets an EMC (Energy-Matter Covalence) value computed from its recipes at world load. Anything you've learned can be broken down into EMC and reshaped into anything else you know — if you can afford it.

## Features

- **The EMC engine** — item values solved from the full recipe graph at world load (~1,500 values on a vanilla instance; mods that add recipes are priced automatically)
- **Transmutation Table & Tablet** — learn items permanently, burn matter into EMC, withdraw anything you know
- **Automation chain** — Energy Collectors (MK1–3) generate EMC from light, Antimatter Relays (MK1–3) buffer and boost it, Energy Condensers duplicate any learned item from raw EMC
- **Dark & Red Matter progression** — tools, weapons, and armor with charge levels, modes, and area abilities; Dark and Red Matter Furnaces; item-holding Pedestals with ambient powers
- **The Gem armor set** — flight, damage absorption, offensive abilities, all toggleable with keybinds
- **Rings, amulets, and stones** — creative-flight rings, infinite water/lava amulets, a world-transmuting Philosopher's Stone with live preview overlay, healing/feeding stones, and more
- **Alchemical Bags & Chests** — 104-slot color-coded bags that live on your player, with item-suction and auto-repair companions
- **Klein Stars & alchemical fuels** — portable EMC batteries and a three-tier fuel line that burns in any furnace
- **Fully data-driven** — override any value with `custom_emc.json` or datapack conversion files; tweak everything in `config/ProjectE/*.toml`

## Installation

**Requirements:** Minecraft **1.21.1**, [Fabric Loader](https://fabricmc.net/use/installer/), [Fabric API](https://modrinth.com/mod/fabric-api) for 1.21.1.

1. Install Fabric Loader for 1.21.1.
2. Drop **`EternalExchange-<version>.jar`** (from [Releases](https://github.com/whoisaldo/EternalExchange/releases)) and **Fabric API** into `mods/`.
3. For servers, place the same two jars in the server's `mods/` directory.

## Roadmap

The core loop is complete and playable. Planned work:

- Recipe viewer integration (EMI/REI) with transmutation support
- Trinkets support for rings and amulets
- Fabric transfer-API bridges (machines as pipe targets, amulets filling tanks)
- Server→client config sync for multiplayer consistency
- Original content beyond the classic equivalent-exchange set

## Building from source

```bash
git clone https://github.com/whoisaldo/EternalExchange.git
cd EternalExchange
./gradlew build        # requires JDK 21
```

The jar lands in `build/libs/`. `./gradlew runClient` / `runServer` launch a dev instance.

## Contributing

Bug reports and pull requests are welcome. See [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines — clear reproduction steps make reports actionable.

## Origins & License

EternalExchange began as an independent Fabric port of the MIT-licensed [ProjectE](https://github.com/sinkillerj/ProjectE) (itself a rewrite of Equivalent Exchange 2), and it retains compatibility with that mod's data formats — worlds, configs, and datapacks carry over. Full credit to those projects' authors for the original design; see [LICENSE](LICENSE) for notices.

**EternalExchange is not affiliated with, maintained by, or endorsed by the ProjectE team.** It is developed independently and follows its own direction — report issues [here](https://github.com/whoisaldo/EternalExchange/issues), never upstream.

Licensed under the [MIT License](LICENSE). Not affiliated with Mojang or Microsoft.
