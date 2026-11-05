## Projeto para talk de SOLID and Clean Arch

# nome-app Blueprint

## System Overview
Example project for a talk on SOLID and Clean Architecture, built with Gradle as `rootProject.name = "nome-app"`. It has two modules: `domain` holds business rules, and `application` holds the Spring Boot adapters that expose and implement them.

```text
HTTP -> AssessmentController -> CreateAssessmentUseCase -> GetQuestionaryGateway <- GetQuestionaryProvider (hardcoded data)
                                                        -> SaveAssessmentGateway <- SaveAssessmentProvider -> AssessmentRepository
     -> AssessmentController -> CalculatorUseCase       -> Calculator <- CalculatorEnem, CalculatorProvinha

tests -> RecordEvaluationUseCase -> TransactionGateway    <- TransactionProvider (TransactionTemplate)
                                 -> SaveEvaluationGateway <- SaveEvaluationProvider -> EvaluationRepository -> evaluation
                                 -> SaveAnswersGateway    <- SaveAnswersProvider    -> AnswerRepository     -> answer
```

The evaluation flow shows how a use case can make two gateway calls succeed or fail together without Spring in the domain. It is a local transaction on one database, not a distributed transaction. The design is in [docs/designs/record_evaluation_transaction.md](./docs/designs/record_evaluation_transaction.md).

### Glossary
- **Assessment:** an exam definition, as in the `Assessement` model.
- **Evaluation:** one student's recorded attempt at an exam, with the student's answers.

## Subdirectories & System Boundaries
Only folders with logic or architecture contracts have a README. Data-only folders (models, entities, request and response bodies, exceptions, repository interfaces) are described inside the README of the folder that uses them.

- `domain`: business rules, with no Spring dependency. Its build declares only `javax.inject`, for `@Named`.
  - [assessment use cases](./domain/src/main/kotlin/br/com/talk/assessment/usecase/README.md) - `CreateAssessmentUseCase`, `CalculatorUseCase`, and the `Calculator` strategies.
  - [assessment ports](./domain/src/main/kotlin/br/com/talk/assessment/gateway/README.md) - `GetQuestionaryGateway` and `SaveAssessmentGateway`.
  - [evaluation use case](./domain/src/main/kotlin/br/com/talk/evaluation/usecase/README.md) - `RecordEvaluationUseCase`.
  - [evaluation ports](./domain/src/main/kotlin/br/com/talk/evaluation/gateway/README.md) - `SaveEvaluationGateway`, `SaveAnswersGateway`, and the `TransactionGateway` contract.
- `application`: the Spring Boot application, started by `Application.kt`.
  - [assessment HTTP entry point](./application/src/main/kotlin/br/com/talk/assessment/entrypoint/README.md) - `POST /api/v1/assessment` and `POST /api/v1/assessment/calculator`.
  - [assessment adapters](./application/src/main/kotlin/br/com/talk/assessment/gateway/README.md) - `GetQuestionaryProvider` and `SaveAssessmentProvider`.
  - [evaluation adapters](./application/src/main/kotlin/br/com/talk/evaluation/gateway/README.md) - `TransactionProvider`, `SaveEvaluationProvider`, and `SaveAnswersProvider`.
  - [configuration](./application/src/main/kotlin/br/com/talk/config/README.md) - error handling, internationalization, `RestTemplate`, and Swagger.

## Global Invariants
- Dependencies point inward: `application` declares `implementation(project(":domain"))`, and `domain` declares no project dependencies.
- Use cases depend on the gateway interfaces in `domain`. The implementations live in `application`.
- Domain beans are annotated `@Named` and are found through `scanBasePackages = ["br.com.talk"]` on `Application`.
- `RecordEvaluationUseCase` saves an evaluation and its answers inside `TransactionGateway.execute`, so a failure in either step leaves no rows in either table.
- The code `assessement-type.invalid-type`, thrown by `AssessmentType.toAssessmentType`, has no entry in `application/src/main/resources/messages.properties`.
- `CalculatorEnem.execute` and `CalculatorProvinha.execute` are unimplemented `TODO(...)` stubs, so a calculator request with a valid type throws `kotlin.NotImplementedError`.

## Global Side Effects
- The application connects to Postgres through `DATABASE_URL` (default `jdbc:postgresql://localhost:5432/talk`), `DATABASE_USERNAME` (default `postgres`), and `DATABASE_PASSWORD` (default empty). Hibernate creates no tables (`ddl-auto: none`), so a locally started application has no `evaluation` or `answer` tables, and the evaluation feature has no endpoint.
- The server port comes from `SERVER_PORT` (default `9011`) and the profile from `ACTIVE_PROFILE` (default `dev`). The build declares both the web and WebFlux starters; with both present, Spring Boot runs the servlet stack, which the servlet config needs.
- The assessment flow is unrepaired: its entities have no tables in any schema, so `POST /api/v1/assessment` fails once it reaches the database.
- `application.yml` exposes all actuator web endpoints (`management.endpoints.web.exposure.include: "*"`). Do not point the application at a shared database.
- Swagger is active under the `dev` and `staging` profiles, and `dev` is the default for `ACTIVE_PROFILE`.

## System Verification
- Run: `./gradlew test` on JDK 11 with Docker running. The evaluation tests start Postgres with Testcontainers and fail if Docker is missing.
