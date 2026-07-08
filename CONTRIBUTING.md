# Contributing to EternalValuePE

Thanks for helping make the mod better. Here's how to be effective.

## Reporting bugs

Use the [bug report template](https://github.com/whoisaldo/EternalValuePE/issues/new/choose). The three things that make a report actionable:

1. **Exact steps to reproduce** — what you clicked/placed/crafted, in order.
2. **Expected vs. actual behavior** — including exact numbers for EMC issues.
3. **Logs** — `logs/latest.log`, and `crash-reports/` if it crashed. Attach files, don't screenshot text.

Please confirm the bug happens with only EternalValuePE + Fabric API installed before reporting, or name the other mods involved.

## Pull requests

- Target `main`. Keep PRs focused — one fix or feature each.
- Match the existing code style (tabs, upstream-style formatting). Run `./gradlew build` before pushing; CI must pass.
- Mark any intentional behavior divergence from classic equivalent-exchange behavior with a `//Fabric port:` comment and explain it in the PR description.
- Read [`PORTING.md`](PORTING.md) first — it maps every NeoForge-era API to this codebase's replacement. New code should use the same patterns (Fabric API lookups, the `compat` shims, mixin conventions).

## Development setup

JDK 21 required. `./gradlew build` builds, `./gradlew runClient` / `runServer` launch dev instances. The repo uses Loom with Mojang mappings.

## Scope

This project follows its own direction. Feature requests are welcome, but parity bugs (things that behaved differently in the classic mods) get priority while the port hardens.
