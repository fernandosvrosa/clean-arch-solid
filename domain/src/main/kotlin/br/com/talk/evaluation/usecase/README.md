# usecase (evaluation)

## Overview
Saves an evaluation and its answers through two separate gateways, inside one transaction boundary that the use case opens itself. The class is annotated `@Named` (`javax.inject`), so the domain keeps no Spring dependency.

## Interface
- `class RecordEvaluationUseCase(transactionGateway: TransactionGateway, saveEvaluationGateway: SaveEvaluationGateway, saveAnswersGateway: SaveAnswersGateway)`: `fun execute(evaluation: Evaluation, answers: List<Answer>): Long`

## Invariants
- Both gateway calls run inside `transactionGateway.execute`: first `saveEvaluationGateway.execute`, then `saveAnswersGateway.execute` with the returned id.
- `execute` returns the evaluation id.
- An empty answer list is allowed and saves only the evaluation.

## Side Effects
- Triggers the effects of its gateway implementations. With the `application` adapters, a failure in either step leaves no rows in either table.

## Verification
- Run: `./gradlew :domain:test` (call order only; the rollback is proven by `./gradlew :application:test`)
