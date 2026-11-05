# gateway (application)

## Overview
Adapters implementing the output ports declared in the [domain gateway package](../../../../../../../../../domain/src/main/kotlin/br/com/talk/assessment/gateway/README.md).

## Interface
- `class GetQuestionaryProvider : GetQuestionaryGateway` (`@Component`): `fun execute(questionaryCode: String): Questionary`
- `class SaveAssessmentProvider(assessmentRepository: AssessmentRepository) : SaveAssessmentGateway` (`@Component`): `fun execute(questionary: Questionary): Assessement`
- Persistence: [./postgres/README.md](./postgres/README.md)

## Invariants
- `GetQuestionaryProvider.execute` ignores `questionaryCode` and returns a hardcoded `Questionary(id = 1, title = "Clean arch", listQuestions = listOf("SOLID", "Clean Arch"))`.

## Side Effects
- `SaveAssessmentProvider.execute` persists through `assessmentRepository.save(AssessmentEntity.fromDomain(questionary))`.
- `GetQuestionaryProvider.execute` makes no network or database call.

## Verification
- Run: `./gradlew :application:test`
