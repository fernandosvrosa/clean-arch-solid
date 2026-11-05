# response

## Overview
HTTP response bodies for the assessment endpoints. Names are spelled as in the code, including `CreateAssessement` and its `Id` property.

## Interface
- `data class Calculator(val average: Float)`
- `data class CreateAssessement(val Id: Long)`

## Invariants
- None

## Side Effects
- None

## Verification
- Run: `./gradlew :application:test`
