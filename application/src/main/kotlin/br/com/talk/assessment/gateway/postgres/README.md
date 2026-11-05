# postgres

## Overview
Spring Data repository for assessments. The Postgres driver and datasource now exist, but no schema creates the `assessment` and `question` tables, so this repository fails once it reaches the database.

## Interface
- `interface AssessmentRepository : JpaRepository<AssessmentEntity, Long>` (declares no members of its own)
- Entities: [./entity/README.md](./entity/README.md)

## Invariants
- None

## Side Effects
- None declared here. Methods inherited from `JpaRepository` access the database at runtime.

## Verification
- Run: `./gradlew :application:test`
