# entity

## Overview
JPA entities for persisted assessments and their questions, with mappings to and from domain types.

## Interface
- `data class AssessmentEntity` (`@Entity(name = "assessment")`): `id: Long?`, `title: String`, `listQuestions: List<QuestionsEntity>`
  - `fun toDomain(): Assessement`
  - `AssessmentEntity.fromDomain(questionary: Questionary): AssessmentEntity`
- `data class QuestionsEntity` (`@Entity(name = "question")`): `id: Long?`, `content: String`
  - `QuestionsEntity.fromDomain(question: String): QuestionsEntity`

## Invariants
- Both `id` fields are annotated `@Id`, default to `null`, and have no `@GeneratedValue`. The `fromDomain` functions leave them `null`.
- `AssessmentEntity.fromDomain` does not copy `Questionary.id`.
- `listQuestions` joins on `question_id`, declared `nullable = false`, `insertable = false`, `updatable = true`, `referencedColumnName = "id"`.
- `title` and `content` are non-nullable Kotlin `String` values.

## Side Effects
- `listQuestions` declares `CascadeType.ALL`, so persistence operations cascade to its questions.
- `orphanRemoval = true` deletes questions removed from the association.
- `listQuestions` is `FetchType.LAZY`, so reading it in `toDomain` can trigger a load.

## Verification
- Run: `./gradlew :application:test`
