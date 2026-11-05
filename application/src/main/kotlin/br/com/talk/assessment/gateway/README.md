# gateway (application)

## Overview
Adapters implementing the output ports declared in the [domain gateway package](../../../../../../../../../domain/src/main/kotlin/br/com/talk/assessment/gateway/README.md).

## Interface
- `class GetQuestionaryProvider : GetQuestionaryGateway` (`@Component`): `fun execute(questionaryCode: String): Questionary`
- `class SaveAssessmentProvider(assessmentRepository: AssessmentRepository) : SaveAssessmentGateway` (`@Component`): `fun execute(questionary: Questionary): Assessement`
- Persistence in `postgres/`: `interface AssessmentRepository : JpaRepository<AssessmentEntity, Long>`, and in `postgres/entity/` the JPA entities `AssessmentEntity` (`@Entity(name = "assessment")`) and `QuestionsEntity` (`@Entity(name = "question")`), with `toDomain` and `fromDomain` mappings.

## Invariants
- `GetQuestionaryProvider.execute` ignores `questionaryCode` and returns a hardcoded `Questionary(id = 1, title = "Clean arch", listQuestions = listOf("SOLID", "Clean Arch"))`.
- The assessment entities declare `@Id` ids that default to `null` with no `@GeneratedValue`, and the `question_id` join column is declared both `nullable = false` and `insertable = false`.
- No schema creates the `assessment` and `question` tables, so `SaveAssessmentProvider` fails once it reaches the database.

## Side Effects
- `SaveAssessmentProvider.execute` persists through `assessmentRepository.save(AssessmentEntity.fromDomain(questionary))`.
- `GetQuestionaryProvider.execute` makes no network or database call.

## Verification
- Run: `./gradlew :application:test`
