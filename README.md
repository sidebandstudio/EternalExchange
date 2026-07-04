# EternalValue — ProjectE for Fabric

A faithful Fabric port of **[ProjectE](https://github.com/sinkillerj/ProjectE)** (`ProjectE-1.21.1-PE1.1.0`) for Minecraft **1.21.1**.

ProjectE is a complete rewrite of Equivalent Exchange 2 by SinKillerJ, MaPePeR, williewillus, Lilylicious, pupnewfster and contributors, released under the **MIT license**. This project ports that codebase (and its assets, data, and EMC values, imported unchanged from the official release jar) from NeoForge to the Fabric loader. All credit for the mod's design, content, art, and systems belongs to the upstream authors.

## Status

The full codebase (~450 source files: EMC engine, transmutation, all items/blocks/machines, GUIs, networking, config) compiles and builds against Fabric Loader 0.19.3 + Fabric API for 1.21.1. Runtime hardening is in progress — see `PORTING.md` for the porting patterns, intentional divergences, and remaining work.

**Not yet ported:** JEI/EMI recipe viewer integration, CraftTweaker, Curios (→ Trinkets), Jade/TOP/WTHIT overlays, config GUI, datagen source set. A few NeoForge-only hooks are stubbed with `//Fabric port:` comments where Fabric has no equivalent (details in `PORTING.md`).

## Compatibility notes

- **Mod id stays `projecte`**, so datapacks, resource packs, and custom EMC files targeting upstream ProjectE work unchanged. Do not install this alongside NeoForge ProjectE (they are the same mod id on different loaders — an instance only ever has one).
- **World migration:** player knowledge and alchemical bags saved by the NeoForge version are read back (the port reads `neoforge:attachments` player NBT as a fallback and uses the same item-handler NBT format).
- **Config:** same TOML format and option names as upstream, at `config/ProjectE/*.toml`. Server-type options are not synced to clients — keep them matched manually in multiplayer.
- Custom EMC conversions (`custom_emc.json`, datapack conversions) use the same formats as upstream. Recipes gated by upstream's NeoForge conditions were converted to Fabric resource conditions with the same ids.

## Building

Requires JDK 21.

```
./gradlew build
```

The jar lands in `build/libs/`. Run a dev client/server with `./gradlew runClient` / `./gradlew runServer`.

## License

MIT, same as upstream ProjectE. See `LICENSE`.
