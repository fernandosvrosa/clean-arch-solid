# application

## Overview
Gradle module holding the Spring Boot application: REST entry points, gateway adapters, and configuration. `build.gradle.kts` depends on `:domain`.

`build.gradle.kts` declares both `spring-boot-starter-web` and `spring-boot-starter-webflux`. With both present, Spring Boot runs the servlet stack, which the existing servlet config needs.

`application.yml` sets the server port from `SERVER_PORT` (default `9011`), the active profile from `ACTIVE_PROFILE` (default `dev`), and exposes all actuator web endpoints. It reads the datasource from `DATABASE_URL` (default `jdbc:postgresql://localhost:5432/talk`), `DATABASE_USERNAME` (default `postgres`), and `DATABASE_PASSWORD` (default empty), with `ddl-auto: none`.

Local startup limits: Hibernate creates no tables, so a locally started application has no `evaluation` or `answer` tables, and the evaluation feature has no endpoint. Starting the application shows only that it starts. The evaluation flow is exercised by `./gradlew :application:test`, which needs Docker.

## Subdirectories & Boundaries
- src: [./src/README.md](./src/README.md) - Source sets of the module.
