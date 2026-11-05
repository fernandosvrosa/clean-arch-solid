# evaluation (application)

## Overview
Spring adapters for the evaluation feature: the transaction adapter and the two save gateways. There is no HTTP endpoint, so the feature is exercised only by tests.

## Subdirectories & Boundaries
- gateway: [./gateway/README.md](./gateway/README.md) - `TransactionProvider`, `SaveEvaluationProvider`, `SaveAnswersProvider`, and JPA persistence.
