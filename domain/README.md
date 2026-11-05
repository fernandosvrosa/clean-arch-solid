# domain

## Overview
Gradle module holding business rules: models, use cases, output ports, and exceptions. `build.gradle.kts` declares no Spring dependency and no dependency on `:application`; production code uses `javax.inject` for `@Named`.

## Subdirectories & Boundaries
- src: [./src/README.md](./src/README.md) - Source sets of the module.
