# gateway (evaluation, application)

## Overview
Implementations of the output ports declared in the [domain gateway package](../../../../../../../../../domain/src/main/kotlin/br/com/talk/evaluation/gateway/README.md).

## Interface
- `class TransactionProvider(transactionTemplate: TransactionTemplate) : TransactionGateway` (`@Component`)
- `class SaveEvaluationProvider(evaluationRepository: EvaluationRepository) : SaveEvaluationGateway` (`@Component`)
- `class SaveAnswersProvider(answerRepository: AnswerRepository) : SaveAnswersGateway` (`@Component`)
- Persistence in `postgres/`: `EvaluationRepository` and `AnswerRepository`, both `JpaRepository<..., Long>`, and in `postgres/entity/` the JPA entities `EvaluationEntity` (`@Table(name = "evaluation")`) and `AnswerEntity` (`@Table(name = "answer")`).

## Invariants
- `TransactionProvider.execute` runs the block inside `TransactionTemplate.execute`. It commits on normal return and rolls back on any throw.
- The transaction is bound to the current thread, and both repositories use the default `REQUIRED` propagation, so they join it.
- `TransactionProvider` must stay the only `TransactionGateway` bean.
- `SaveEvaluationProvider.execute` returns `checkNotNull(saved.id)`.
- Both entities use `@GeneratedValue(strategy = IDENTITY)`, so each insert runs as soon as the entity is saved, and the evaluation id is ready for the second step.
- `AnswerEntity` holds a plain `evaluationId` with no JPA relation and no cascade, so saving answers stays a separate step.
- Hibernate creates no tables (`ddl-auto: none`). Tests create them from `application/src/test/resources/evaluation-schema.sql`, which declares the `fk_answer_evaluation` foreign key and the `uq_answer_evaluation_question` unique constraint.

## Side Effects
- `SaveEvaluationProvider.execute` inserts one `evaluation` row.
- `SaveAnswersProvider.execute` inserts one `answer` row per answer through `saveAll`. Without an outer transaction, `saveAll` runs its own, so its answers roll back together.

## Verification
- Run: `./gradlew :application:test` (requires Docker)
