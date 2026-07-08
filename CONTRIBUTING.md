# Contributing to EternalExchange

Thank you for taking the time to contribute.

## Reporting bugs

Use the [bug report template](https://github.com/whoisaldo/EternalExchange/issues/new/choose). A report is actionable when it includes:

1. **Exact steps to reproduce** — what you clicked/placed/crafted, in order.
2. **Expected vs. actual behavior** — including exact numbers for EMC issues.
3. **Logs** — `logs/latest.log`, and `crash-reports/` if it crashed. Attach files, don't screenshot text.

Please confirm the bug happens with only EternalExchange + Fabric API installed before reporting, or name the other mods involved.

## Pull requests

- Target `main`. Keep PRs focused — one fix or feature each.
- Match the existing code style (tabs, upstream-style formatting). Run `./gradlew build` before pushing; CI must pass.
- Mark any intentional behavior divergence from classic equivalent-exchange behavior with a `//Fabric port:` comment and explain it in the PR description.
- New code should follow the existing patterns: Fabric API lookups for capabilities, the `moze_intel.projecte.compat` shims for cross-loader concepts, and the established mixin conventions.

## Development setup

JDK 21 required. `./gradlew build` builds, `./gradlew runClient` / `runServer` launch dev instances. The repo uses Loom with Mojang mappings.

## Scope

This project follows its own direction. Feature requests are welcome, but parity bugs (things that behaved differently in the classic mods) get priority while the port hardens.
