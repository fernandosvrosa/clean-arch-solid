# gateway (evaluation, domain)

## Overview
Output ports of the evaluation feature. The `application` module implements them in [its gateway package](../../../../../../../../../application/src/main/kotlin/br/com/talk/evaluation/gateway/README.md).

## Interface
- `interface SaveEvaluationGateway`: `fun execute(evaluation: Evaluation): Long`
- `interface SaveAnswersGateway`: `fun execute(evaluationId: Long, answers: List<Answer>)`
- `interface TransactionGateway`: `fun <T> execute(block: () -> T): T`

## Invariants
- `SaveEvaluationGateway.execute` returns the new evaluation's id, which is never null.
- `TransactionGateway.execute` makes all database writes in `block` succeed or fail together, on one database:
  - If `block` returns normally, the work commits. If it throws, the work rolls back and the error reaches the caller.
  - `block` runs synchronously, on the caller's thread.
  - `block` must not catch errors from the gateways, and must not make network calls.
  - A second datasource or a remote service needs a new design, not a new implementation.

## Side Effects
- None: these are interfaces only. Effects belong to the implementations.

## Verification
- Run: `./gradlew :domain:test`
