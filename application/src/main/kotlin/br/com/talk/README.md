# talk (application)

## Overview
Root package of the `application` module, holding the Spring Boot entry point.

## Interface
- `class Application` (`@SpringBootApplication(scanBasePackages = ["br.com.talk"])`, `@EnableFeignClients`)
- `fun main(args: Array<String>)`
- Adapters: [./assessment/README.md](./assessment/README.md)
- Evaluation adapters: [./evaluation/README.md](./evaluation/README.md)
- Configuration: [./config/README.md](./config/README.md)

## Invariants
- Component scanning covers `br.com.talk`, which is also the root package of the `domain` module. That is how its `@Named` classes can be discovered.

## Side Effects
- `main` starts the Spring Boot application through `runApplication<Application>(*args)`.

## Verification
- Run: `./gradlew :application:test`
