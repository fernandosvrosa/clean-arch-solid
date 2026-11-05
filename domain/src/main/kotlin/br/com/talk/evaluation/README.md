# evaluation (domain)

## Overview
Records one student's attempt at an exam (an evaluation) and the student's answers, as one all-or-nothing operation on one database. It is a teaching example: the domain states that the two steps succeed or fail together, and the `application` module decides how to enforce it. This is a local transaction, not a distributed one.

## Subdirectories & Boundaries
- gateway: [./gateway/README.md](./gateway/README.md) - `SaveEvaluationGateway`, `SaveAnswersGateway`, and `TransactionGateway`.
- model: [./model/README.md](./model/README.md) - `Evaluation` and `Answer`.
- usecase: [./usecase/README.md](./usecase/README.md) - `RecordEvaluationUseCase`.
