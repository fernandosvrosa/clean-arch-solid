# model

## Overview
Domain data types for assessments, questionaries, and calculation inputs. Names are spelled as in the code, including `Assessement`.

## Interface
- `data class Assessement(val id: Long? = null, val title: String, val listQuestions: List<String>)`
- `enum class AssessmentType(val value: String)`: `ENEM("enem")`, `PROVINHA("provinha")`
- `AssessmentType.toAssessmentType(value: String)` (companion function)
- `data class Questionary(val id: Long, val title: String, val listQuestions: List<String>)`
- `data class Result(val id: Long, val name: String, val hits: Int)`
- `data class Tri(val id: Long, val averageCalc: Float)`

## Invariants
- `toAssessmentType` compares `value.toLowerCase()` with `AssessmentType.value`, so matching is case-insensitive.
- `toAssessmentType` throws `UnprocessableEntityException` with code `assessement-type.invalid-type` when nothing matches.
- `Assessement.id` is nullable and defaults to `null`. `Questionary.id`, `Result.id`, and `Tri.id` are non-nullable with no default.
- All data class properties are read-only (`val`).

## Side Effects
- None

## Verification
- Run: `./gradlew :domain:test`
