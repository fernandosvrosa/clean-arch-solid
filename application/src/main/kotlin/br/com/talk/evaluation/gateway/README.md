# gateway (evaluation, application)

## Overview
Implementations of the output ports declared in the [domain gateway package](../../../../../../../../../domain/src/main/kotlin/br/com/talk/evaluation/gateway/README.md).

## Interface
- `class TransactionProvider(transactionTemplate: TransactionTemplate) : TransactionGateway` (`@Component`)
- `class SaveEvaluationProvider(evaluationRepository: EvaluationRepository) : SaveEvaluationGateway` (`@Component`)
- `class SaveAnswersProvider(answerRepository: AnswerRepository) : SaveAnswersGateway` (`@Component`)
- Persistence: [./postgres/README.md](./postgres/README.md)

## Invariants
- `TransactionProvider.execute` runs the block inside `TransactionTemplate.execute`. It commits on normal return and rolls back on any throw.
- The transaction is bound to the current thread, and both repositories use the default `REQUIRED` propagation, so they join it.
- `TransactionProvider` must stay the only `TransactionGateway` bean.
- `SaveEvaluationProvider.execute` returns `checkNotNull(saved.id)`.

## Side Effects
- `SaveEvaluationProvider.execute` inserts one `evaluation` row.
- `SaveAnswersProvider.execute` inserts one `answer` row per answer through `saveAll`. Without an outer transaction, `saveAll` runs its own, so its answers roll back together.

## Verification
- Run: `./gradlew :application:test` (requires Docker)
