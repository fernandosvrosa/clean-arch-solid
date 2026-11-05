# postgres (evaluation)

## Overview
Spring Data repositories for evaluations and answers.

## Interface
- `interface EvaluationRepository : JpaRepository<EvaluationEntity, Long>`
- `interface AnswerRepository : JpaRepository<AnswerEntity, Long>`
- Entities: [./entity/README.md](./entity/README.md)

## Invariants
- None

## Side Effects
- Methods inherited from `JpaRepository` access the database.

## Verification
- Run: `./gradlew :application:test` (requires Docker)
