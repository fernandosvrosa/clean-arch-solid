# usecase

## Overview
Application use cases for creating assessments and calculating averages. Calculators follow a strategy pattern: `CalculatorUseCase` receives every `Calculator` and picks one by assessment type. Classes are annotated `@Named` (`javax.inject`), so the domain has no Spring dependency.

## Interface
- `interface Calculator`
  - `fun isAssessementType(assessmentType: AssessmentType): Boolean`
  - `fun execute(tri: Tri, results: List<Result>): Float`
- `class CalculatorEnem : Calculator`
- `class CalculatorProvinha : Calculator`
- `class CalculatorUseCase(calculators: List<Calculator>)`: `fun execute(assessementType: AssessmentType, assessementId: Long): Float`
- `class CreateAssessmentUseCase(getQuestionaryGateway: GetQuestionaryGateway, saveAssessmentGateway: SaveAssessmentGateway)`: `fun execute(questionaryCode: String): Assessement`

## Invariants
- `CalculatorEnem` matches only `AssessmentType.ENEM`, and `CalculatorProvinha` matches only `AssessmentType.PROVINHA`.
- `CalculatorUseCase.execute` uses `calculators.first { ... }`: the first match wins, and `NoSuchElementException` is thrown if none matches.
- `CalculatorEnem.execute` and `CalculatorProvinha.execute` are `TODO(...)` stubs, so every call throws `kotlin.NotImplementedError`.
- `CalculatorUseCase.execute` never reads `assessementId`. It passes the hardcoded inputs `Tri(1, 1.5f)` and `listOf(Result(1, "Aluno teste", 23))`.
- `CreateAssessmentUseCase.execute` calls `getQuestionaryGateway.execute`, then `saveAssessmentGateway.execute`, and returns the saved result.

## Side Effects
- `CreateAssessmentUseCase.execute` triggers whatever effects its two gateway implementations have.
- The calculator classes perform no I/O.

## Verification
- Run: `./gradlew :domain:test`
