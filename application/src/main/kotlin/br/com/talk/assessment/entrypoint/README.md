# entrypoint

## Overview
REST adapter for assessments. It converts HTTP payloads and delegates to the domain use cases.

## Interface
- `class AssessmentController(createAssessmentUseCase: CreateAssessmentUseCase, calculatorUseCase: CalculatorUseCase)`, under `/api/v1/assessment`, producing `application/json`
  - `POST /api/v1/assessment`: `fun save(createAssessment: CreateAssessment): ResponseEntity<CreateAssessement>`
  - `POST /api/v1/assessment/calculator`: `fun calculator(calculator: CalculatorRequest): ResponseEntity<CalculatorResponse>`
- Request bodies in `request/`: `data class Calculator(val type: String, val assessmentId: Long)` and `data class CreateAssessment(val questionaryCode: String)`. No validation annotations are declared.
- Response bodies in `response/`: `data class Calculator(val average: Float)` and `data class CreateAssessement(val Id: Long)`. The names are spelled as in the code.

## Invariants
- Both endpoints return HTTP 201 (`HttpStatus.CREATED`) on success, including the calculator endpoint.
- `save` applies `!!` to `assessemnt.id`, so a null id throws `NullPointerException`.
- `calculator` converts `calculator.type` with `AssessmentType.toAssessmentType` before calling the use case.

## Side Effects
- None directly. Each endpoint triggers the effects of its use case.

## Verification
- Run: `./gradlew :application:test`
