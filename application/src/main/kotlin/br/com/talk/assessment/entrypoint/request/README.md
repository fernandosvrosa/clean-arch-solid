# request

## Overview
HTTP request bodies for the assessment endpoints.

## Interface
- `data class Calculator(val type: String, val assessmentId: Long)`
- `data class CreateAssessment(val questionaryCode: String)`

## Invariants
- None: no validation annotations are declared.

## Side Effects
- None

## Verification
- Run: `./gradlew :application:test`
