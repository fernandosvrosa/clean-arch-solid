# All-or-Nothing Save of a Student's Evaluation and Answers

*   **Author:** Fernando da Silva Rosa
*   **Status:** Approved
*   **Date:** 2026-10-31

--------------------------------------------------------------------------------

## Overview

This is a teaching example for a talk on SOLID and Clean Architecture. A new use case records a student's evaluation and then the student's answers, through two separate gateways. Both steps write to one Postgres database and run inside one local transaction, so either both are saved or neither is.

The domain module expresses that rule through its own interface, without depending on Spring. The application module implements the interface with Spring's transaction support. Tests against a real Postgres container prove the rollback.

The example sits next to the current assessment flow and does not change it. It is not a distributed transaction: only one database takes part.

Two terms are used throughout:

*   **Assessment:** the exam definition, as in the current `Assessement` model.
*   **Evaluation:** one student's recorded attempt at an exam.

## The Problem

The talk needs a concrete case for one question: how can a use case make two gateway calls succeed or fail together when the domain cannot use Spring's `@Transactional`?

The repository cannot show this today:

*   `CreateAssessmentUseCase` performs one read and one write, so it has no second write to protect.
*   The domain build has no Spring dependency, so the usual annotation is not available in a use case.
*   No code has run against a database. There is no driver or datasource, and the only test code is the abstract `BaseTest`.

Without a shared transaction, each gateway call commits on its own. If saving the answers fails, the evaluation stays in the database with no answers, and later code reading it has no way to know the attempt is incomplete.

The strongest objection is that one gateway saving both records with cascade would solve this with less code, and the current assessment flow already works that way. The example keeps two gateways because the boundary between them is what the talk teaches, not because this data requires it. The talk should say so directly.

## Goals

*   A new use case saves one evaluation and its answers through two gateways, as one all-or-nothing operation.
*   No file in the domain module imports Spring, and `domain/build.gradle.kts` gains no Spring dependency.
*   A test confirms the rows exist in both tables after a successful save.
*   A test causes a real Postgres error in the second step and confirms that neither table has rows afterward.
*   All tests that need a database run against Postgres in Testcontainers, and they fail rather than skip when Docker is missing.
*   The application can start locally against a Postgres whose connection settings come from environment variables.
*   No existing assessment source file changes.

## Non-Goals

*   Distributed transactions, more than one database, or calls to other services inside the transaction.
*   An HTTP endpoint or request and response types for the new use case. Starting the application locally shows only that it starts.
*   Repairs to the assessment flow, the calculators, or `GlobalExceptionHandler`.
*   Upgrades to Spring Boot, Kotlin, or Gradle, and web stack cleanup, unless the first phase shows that the smallest possible fix is required. Each such fix is its own commit.
*   Migration tooling, docker-compose, metrics, tracing, and coverage threshold changes.

## Open Questions

*   **Unproven base:** does the project compile and start, including the legacy assessment mapping once a datasource exists? If the legacy mapping blocks startup, work stops and asks the user.

## Critical User Journeys (CUJs)

### CUJ 1: Successful save

*   **Given**: A running Postgres container with empty `evaluation` and `answer` tables.
*   **When**: The test calls the new use case with valid student and exam data and answers with different question numbers.
*   **Then**: The `evaluation` table has one row, and the `answer` table has one row per answer, each pointing to that evaluation.

### CUJ 2: Failure in the second step

*   **Given**: A running Postgres container with empty tables and a unique constraint on evaluation id and question number.
*   **When**: The test calls the use case with two answers that share a question number.
*   **Then**: The call throws, the failure comes from that constraint, and both tables have zero rows when read outside the transaction.

### CUJ 3: Presenter runs the demo

*   **Given**: A machine with JDK 11 and Docker running.
*   **When**: The presenter runs the test command.
*   **Then**: The tests run against the container, and the logs show the transaction starting, the inserts, the failure, and the rollback.

### CUJ 4: Local startup

*   **Given**: A local Postgres and its connection settings in environment variables.
*   **When**: The developer starts the application.
*   **Then**: The application starts. The new use case is not reachable by hand, because there is no endpoint.

## Technical Plan (Architecture)

Nothing here has been built or run. Statements about framework behavior marked **[unverified]** must be confirmed in the first implementation phase.

### Decisions made by the owner

| Decision | Choice |
| --- | --- |
| Transaction adapter | `TransactionTemplate` |
| Control test without a transaction | Included |
| Test schema | SQL init script |
| Test context | `@SpringBootTest(webEnvironment = NONE)` |

### Structure

The feature lives in a new package, `br.com.talk.evaluation`, in both modules. `Application` already scans `br.com.talk`, so no assessment file changes.

```text
domain (no Spring)                      application (Spring)
--------------------------------        ----------------------------------------
RecordEvaluationUseCase
  |-- TransactionGateway      <-------- TransactionProvider
  |                                       `-- TransactionTemplate
  |-- SaveEvaluationGateway   <-------- SaveEvaluationProvider
  |                                       `-- EvaluationRepository -> evaluation
  `-- SaveAnswersGateway      <-------- SaveAnswersProvider
                                          `-- AnswerRepository     -> answer
```

### Domain

*   **Models:** `Evaluation(studentName, studentRegistration, examCode, examTitle)` and `Answer(questionNumber, chosenOption)`. Student and exam data repeat on each evaluation on purpose, to keep the example to two tables.
*   **`SaveEvaluationGateway`:** `execute(evaluation: Evaluation): Long` returns the new evaluation's id, which is never null.
*   **`SaveAnswersGateway`:** `execute(evaluationId: Long, answers: List<Answer>)` saves the answers.
*   **`TransactionGateway`:** a plain interface with one method, `fun <T> execute(block: () -> T): T`. It offers no propagation, isolation, or timeout options.
*   **`RecordEvaluationUseCase`:** annotated `@Named`, like the current use cases. `execute(evaluation, answers): Long` opens the transaction boundary and makes both gateway calls inside it:

```text
transactionGateway.execute {
    id = saveEvaluationGateway.execute(evaluation)
    saveAnswersGateway.execute(id, answers)
    id
}
```

An empty answer list is allowed and saves only the evaluation. Nothing in the design needs to reject it.

### The contract of `TransactionGateway`

The method signature cannot express these rules, so they go in the interface's KDoc and the package README:

*   If the block returns normally, the work commits. If the block throws, the work rolls back and the error reaches the caller.
*   The block runs synchronously, on the caller's thread, against one database.
*   The block must not catch errors from the gateways, and must not make network calls.
*   Using a second datasource needs a new design, not a new implementation of this interface.

The word "atomically" is not used alone, because in the talk it can be read as covering more than one database.

### Application

*   **`TransactionProvider`:** a `@Component` implementing `TransactionGateway` by running the block inside `TransactionTemplate.execute`. Under strict JSR-305, the template's result is nullable in Kotlin, so the adapter casts it to `T` with a narrowly scoped `@Suppress("UNCHECKED_CAST")`. Using `!!` would break blocks that return null **[unverified]**.
*   **How the second gateway joins:** the transaction is bound to the current thread, and both repositories use the default `REQUIRED` propagation, so they join it **[unverified]**.
*   **Entities:** `EvaluationEntity` and `AnswerEntity` use `@GeneratedValue(strategy = IDENTITY)`, so each insert runs right away and in order, and the evaluation's id is available to the second step **[unverified]**. `AnswerEntity` stores a plain `evaluationId`, with no JPA relation and no cascade, because a cascade would remove the two-gateway boundary. The existing assessment entities are not a template: they have null ids with no generator.
*   **Providers:** `SaveEvaluationProvider` calls `save` and returns the generated id. `SaveAnswersProvider` calls `saveAll`.
*   **Main configuration:** `application.yml` gets datasource settings read from environment variables. Boot's default creates no tables on a non-embedded database **[unverified]**, so a locally started application has no `evaluation` or `answer` tables. Nothing calls them, because there is no endpoint.

### Database schema

The test init script creates:

```text
evaluation(id identity primary key, student_name not null, student_registration not null,
           exam_code not null, exam_title not null)
answer(id identity primary key, evaluation_id not null references evaluation(id),
       question_number not null, chosen_option not null,
       constraint uq_answer_evaluation_question unique (evaluation_id, question_number))
```

The script does not create the legacy `assessment` and `question` tables. `ddl-auto` stays `none` in tests, so Hibernate does not create or check any tables.

### Tests

One test class, `RecordEvaluationIntegrationTest`, uses `@SpringBootTest(webEnvironment = NONE)` with a Postgres Testcontainers image at a pinned version. `@DynamicPropertySource` supplies the connection settings. It does not extend `BaseTest`, which needs MockMvc and starts WireMock on a fixed port.

Each test deletes both tables' rows first and reads counts through `JdbcTemplate`, outside any transaction. The test class does not use `@Transactional`, and Docker being absent fails the run.

| Test | Setup | Failure caused | Expected result |
| --- | --- | --- | --- |
| Success | Real beans | None | One evaluation row, and one answer row per answer pointing to it |
| Failure | Real beans | Two answers with the same question number | The call throws from `uq_answer_evaluation_question` with SQLState 23505, and both tables have zero rows |
| Control | Real gateways, a hand-built use case, and a pass-through `TransactionGateway` that only calls `block()` | The same duplicate answers | The call throws from the same constraint, one evaluation row survives, and the answer table has zero rows |

In the control test, `saveAll` runs its own transaction when no outer one exists, so its answers roll back together while the evaluation stays committed **[unverified]**. The pass-through fake is not a bean, because a second implementation would break injection.

A domain unit test, with a hand-written fake for each gateway, checks only the call order and returned id. Its name must not claim to prove all-or-nothing behavior.

Test configuration sets `org.springframework.orm.jpa.JpaTransactionManager` and `org.hibernate.SQL` to `DEBUG`, so the presenter can show the transaction starting, the inserts, the failure, and the rollback **[unverified]**. Because `application/src/test/resources/application.yml` likely replaces the main file during tests **[unverified]**, every setting the tests need goes in the test file or `@DynamicPropertySource`.

### Risks

*   **Context startup:** `@SpringBootTest` also loads Feign, Swagger, the locale config, and the legacy mapping `nullable = false` with `insertable = false` (`AssessmentEntity.kt:12`). If the legacy mapping blocks startup, work stops and asks the user. Other startup failures are handled in the first phase under the Non-Goals' smallest-fix rule.
*   **Web stack:** the application declares only the WebFlux starter while its config uses servlet types, so the application type and whether the code compiles are unknown.
*   **Tooling age:** Testcontainers must work with the local Docker version even though the rest of the project dates from about 2020, so its version is chosen for Docker. Boot 2.3 does not manage it **[unverified]**.
*   **Broken legacy endpoint:** once a datasource exists, `POST /api/v1/assessment` can reach a database with no matching tables. This flow stays unrepaired, and the README must say so.

## Alternatives Considered

### Alternative A: One gateway saving both records with cascade

*   **Pros**: The simplest working design, with less code and no transaction interface. The current assessment flow already works this way.
*   **Cons**: Only one gateway remains, so there is no boundary to share a transaction across.
*   **Reason for Rejection**: Rejected only because it removes what the talk teaches. For this data alone, it would be the better choice, and the talk should say so.

### Alternative B: `@Transactional` on the domain use case

*   **Pros**: The shortest code and the form most developers know.
*   **Cons**: Adds Spring to the domain. The domain classes are also final, because the domain build has no `plugin.spring`.
*   **Reason for Rejection**: Breaks the goal of a domain with no Spring dependency.

### Alternative C: `@Transactional` wrapper around the use case in the application module

*   **Pros**: Keeps the domain free of Spring and needs no new domain interface.
*   **Cons**: The rule that both steps succeed together leaves the domain. The controller pattern injects concrete use cases, so a wrapper also needs a new interface.
*   **Reason for Rejection**: Contradicts the lesson that the domain states the rule. It remains a fair answer to an audience question.

### Alternative D: `@Transactional` on the adapter method

*   **Pros**: A shorter slide, using an annotation the audience knows.
*   **Cons**: Depends on a proxy that does not appear in the code, and needs `rollbackFor` to cover checked exceptions.
*   **Reason for Rejection**: The owner chose `TransactionTemplate`, which keeps the boundary visible in the code.

### Alternative E: Begin, commit, and rollback methods on a domain interface

*   **Pros**: Each step of the transaction is visible in the use case.
*   **Cons**: Puts infrastructure lifecycle in the domain, and a rollback can be forgotten.
*   **Reason for Rejection**: One method taking a block gives the same guarantee with no way to forget the rollback.

### Alternative F: Passing a transaction object through gateway methods

*   **Pros**: Removes the reliance on a transaction bound to the thread.
*   **Cons**: Changes every gateway signature, and adapters must downcast the object to use it.
*   **Reason for Rejection**: Spreads a persistence detail through every port.

### Alternative G: XA, JTA, saga, outbox, or two-phase commit

*   **Pros**: These address work across more than one database or service.
*   **Cons**: Heavy machinery for a case with one database, and they would reinforce the "distributed" misreading.
*   **Reason for Rejection**: Only one database takes part, so one local transaction is enough.

### Alternative H: H2 instead of Testcontainers

*   **Pros**: Faster tests with no Docker.
*   **Cons**: Constraint behavior differs from Postgres, so the proof would not carry over.
*   **Reason for Rejection**: The test must fail on a real Postgres error.

### Other rejected options

| Option | Reason for rejection |
| --- | --- |
| One combined gateway with `@Transactional` inside | Hides a business rule in infrastructure |
| `@Transactional` on the controller | Puts the boundary in delivery code, and tests calling the use case bypass it |
| `@Transactional` on each provider | Each one commits separately, which is the bug being shown |
| Domain `@Atomic` annotation with an aspect | Invisible behavior, and needs AOP wiring and open classes |
| Raw `PlatformTransactionManager` | More code than `TransactionTemplate` with no gain |
| Transaction interface in the application module | The use case could not depend on it |
| Top-level `transaction` package | Premature with one consumer |
| Placing the feature under `assessment/` | Edits unrelated contracts and sits beside the broken entities |
| Extending the assessment flow | Its mapping needs repairs, and it would mix two concerns |
| `JdbcTemplate` gateways instead of JPA | Departs from the repository's Spring Data pattern. Remains a fallback if JPA cannot start. |
| One SQL statement or stored procedure | All-or-nothing, but bypasses the gateways and the lesson |
| R2DBC with `TransactionalOperator` | Nothing here is reactive |
| A mock as the failure source | Never reaches Postgres |
| Failing the first step as the main scenario | Nothing was written yet, so it proves nothing |
| NOT NULL as the provoked error | Hard to trigger through Kotlin types, and may be rejected before SQL **[unverified]** |
| `@DataJpaTest` with imports | The owner chose `@SpringBootTest`, which proves the real wiring. A default `@DataJpaTest` also has its own test transaction, which invalidates the proof **[unverified]**. |
| `ddl-auto=create-drop` in tests | The owner chose a SQL script, which shows the constraint and keeps the legacy tables out |
| Leaving out the control test | The owner kept it, because "zero rows" has no counter-proof without it |
| Extending `BaseTest` | Brings in MockMvc and WireMock on a fixed port |
| `disabledWithoutDocker` | A skipped test looks like a pass |
| Flyway or Liquibase | Nothing to migrate yet |
| Upgrading Boot, Kotlin, or Gradle first | High cost with no benefit to the lesson |
| Testcontainers 1.15, matching the project's era | Likely rejected by current Docker **[unverified]** |
| Separate `integrationTest` source set | Excess for one test class |
| ArchUnit | The Gradle module boundary already keeps Spring out of the domain |

## Non-Functional Requirements

*   **Security & Privacy:** tests use made-up student data. Database credentials come from environment variables and are never committed. `application.yml` exposes all actuator endpoints, which is accepted for a local demo, so this must not point at a shared database.
*   **Rollout & Rollback:** there is no deployment and no data to migrate. Reverting the commits removes the feature.

--------------------------------------------------------------------------------

## Detailed Implementation Plan

Work follows the phases below, in order. Every phase ends with `./gradlew build` passing.

### Rules for build fixes

*   A fix for a build failure that blocks this feature is made without asking, in its own commit, before the feature code.
*   Changing the major version of Spring Boot, Kotlin, or Gradle requires asking the owner first.
*   If the legacy assessment mapping stops the context from loading, work stops and asks the owner.
*   `domain/build.gradle.kts:12` (`junit-jupiter` on the production configuration) stays as it is, because it blocks nothing.
*   All work runs on JDK 11.

### Known build fixes

These failures can be predicted, so their fixes are decided here. Any other build failure falls under the rules above.

*   **Servlet types:** if `:application:compileKotlin` fails on `org.springframework.web.servlet` types, add `implementation("org.springframework.boot:spring-boot-starter-web")` and keep the WebFlux starter. With both present, Spring Boot runs the servlet stack, which matches the existing servlet config and `BaseTest`'s MockMvc.
*   **`jcenter()`:** if dependency resolution fails on `jcenter()`, remove `jcenter()` from `build.gradle.kts`, because `mavenCentral()` is already listed.
*   **Kotlin plugins:** if the mixed Kotlin plugin versions fail, align `plugin.spring` to 1.4.30.

### Unverified Items

Each item marked **[unverified]** in the Technical Plan is confirmed in the phase below, with a fixed response if it turns out false.

| Item | Confirmed in | Response if false |
| --- | --- | --- |
| Servlet types compile | Phase 1, `compileKotlin` | Apply the servlet fix in Known build fixes |
| `jcenter()` resolves | Phase 1, `help` | Apply the `jcenter()` fix in Known build fixes |
| Testcontainers 1.21.4 works with the local Docker | Phase 1, context-load test | Try the latest 1.21.x; if it still fails, stop and ask |
| Boot's BOM does not break Testcontainers' dependencies | Phase 1, dependency report and context-load test | Stop and ask |
| The context loads with the legacy mapping | Phase 1, context-load test | Stop and ask |
| `TransactionTemplate.execute` returns `T?` in Kotlin | Phase 2, compile | If it is non-null, drop the cast and the `@Suppress` |
| The new entities start | Phase 3, success test | Stop and ask; switching to `JdbcTemplate` gateways is the owner's call |
| `IDENTITY` inserts run at once and in order | Phase 3, SQL debug log | Stop and ask |
| Both repositories join the outer transaction | Phase 4, failure test finds zero rows | Stop and ask |
| A duplicate raises `DataIntegrityViolationException` | Phase 4, failure test | Assert on the type actually raised, if it is a Spring `DataAccessException`; otherwise stop and ask |
| Without a transaction, the evaluation survives and the answers roll back together | Phase 4, control test | If the evaluation count is 0, stop and ask, because the failure test proves nothing until this passes |
| The debug logs show begin, inserts, and rollback | Phase 4, test output | Add `DEBUG` for `org.springframework.transaction` |

### Target Files to Create/Modify

All paths are relative to the repository root. `D` means `domain/src/main/kotlin/br/com/talk/evaluation`, and `A` means `application/src/main/kotlin/br/com/talk/evaluation`.

#### Build and configuration

##### 1. `application/build.gradle.kts` (Modify)

*   **Type / Scope**: Build configuration.
*   **Proposed Changes**: Add `runtimeOnly("org.postgresql:postgresql")`, with the version managed by Boot, so JPA can reach Postgres. Add `testImplementation("org.testcontainers:postgresql:1.21.4")` with an explicit version, because Boot 2.3 likely does not manage it **[unverified]**. Phase 1 checks this version against the Docker version found on the machine; the Unverified Items table gives the response if it fails.

##### 2. `application/src/main/resources/application.yml` (Modify)

*   **Type / Scope**: Configuration.
*   **Proposed Changes**: Add `spring.datasource.url` as `${DATABASE_URL:jdbc:postgresql://localhost:5432/talk}`, `username` as `${DATABASE_USERNAME:postgres}`, and `password` as `${DATABASE_PASSWORD:}`, so the application can start locally with no committed secret. Add `spring.jpa.hibernate.ddl-auto: none`, so the main configuration does not rely on Boot's default.

##### 3. `application/src/test/resources/application.yml` (Modify)

*   **Type / Scope**: Test configuration.
*   **Proposed Changes**: Keep the existing `server.port`, `management`, and `spring.mvc` settings. Add `spring.jpa.open-in-view: false`, `spring.jpa.hibernate.ddl-auto: none`, and `DEBUG` logging for `org.springframework.orm.jpa.JpaTransactionManager` and `org.hibernate.SQL`. This file may replace the main one during tests **[unverified]**, so it sets everything the tests need, and the datasource comes from `@DynamicPropertySource`. No profile is active in tests, so Swagger stays off.

##### 4. `application/src/test/resources/evaluation-schema.sql` (Create)

*   **Type / Scope**: Test schema.
*   **Proposed Changes**: Create the two tables with this exact script. The failure test depends on `uq_answer_evaluation_question`. There is no `ON DELETE` rule, so Postgres's default applies: an evaluation cannot be deleted while answers point to it, and tests delete answers first.

```sql
create table evaluation (
    id bigint generated by default as identity primary key,
    student_name varchar(255) not null,
    student_registration varchar(50) not null,
    exam_code varchar(50) not null,
    exam_title varchar(255) not null
);

create table answer (
    id bigint generated by default as identity primary key,
    evaluation_id bigint not null,
    question_number integer not null,
    chosen_option varchar(10) not null,
    constraint fk_answer_evaluation foreign key (evaluation_id) references evaluation (id),
    constraint uq_answer_evaluation_question unique (evaluation_id, question_number)
);
```

#### Domain

##### 5. `D/model/Evaluation.kt` (Create)

*   **Type / Scope**: Domain model.
*   **Proposed Changes**: `data class Evaluation(val studentName: String, val studentRegistration: String, val examCode: String, val examTitle: String)`, holding the data for the first step.

##### 6. `D/model/Answer.kt` (Create)

*   **Type / Scope**: Domain model.
*   **Proposed Changes**: `data class Answer(val questionNumber: Int, val chosenOption: String)`, holding the data for the second step.

##### 7. `D/gateway/SaveEvaluationGateway.kt` (Create)

*   **Type / Scope**: Domain port.
*   **Proposed Changes**: `fun execute(evaluation: Evaluation): Long`, returning the id the second step needs.

##### 8. `D/gateway/SaveAnswersGateway.kt` (Create)

*   **Type / Scope**: Domain port.
*   **Proposed Changes**: `fun execute(evaluationId: Long, answers: List<Answer>)`, as the second step.

##### 9. `D/gateway/TransactionGateway.kt` (Create)

*   **Type / Scope**: Domain port.
*   **Proposed Changes**: `fun <T> execute(block: () -> T): T`, as a plain interface, with KDoc stating the contract from the Technical Plan. This is how the domain states the rule without Spring.

##### 10. `D/usecase/RecordEvaluationUseCase.kt` (Create)

*   **Type / Scope**: Use case.
*   **Proposed Changes**: A `@Named` class with the constructor `(transactionGateway: TransactionGateway, saveEvaluationGateway: SaveEvaluationGateway, saveAnswersGateway: SaveAnswersGateway)` and `fun execute(evaluation: Evaluation, answers: List<Answer>): Long`, following the pseudocode in the Technical Plan.

##### 11. `domain/src/test/kotlin/br/com/talk/evaluation/usecase/RecordEvaluationUseCaseTest.kt` (Create)

*   **Type / Scope**: Unit test.
*   **Proposed Changes**: Hand-written fakes append to a shared list:
    *   The transaction fake adds `begin` before running the block and `end` after it.
    *   The evaluation fake adds `evaluation` and returns 42.
    *   The answers fake adds `answers:<id>:<count>`.

    Test `saves evaluation then answers inside the transaction` passes two answers and expects `[begin, evaluation, answers:42:2, end]` and a return value of 42. Test `saves evaluation with no answers` expects `[begin, evaluation, answers:42:0, end]`. Neither test claims to prove all-or-nothing behavior. The domain build already declares the JUnit test dependencies.

#### Application

##### 12. `A/gateway/TransactionProvider.kt` (Create)

*   **Type / Scope**: Adapter.
*   **Proposed Changes**: A `@Component` taking `TransactionTemplate` and implementing `TransactionGateway`, with the cast described in the Technical Plan.

##### 13. `A/gateway/SaveEvaluationProvider.kt` (Create)

*   **Type / Scope**: Adapter.
*   **Proposed Changes**: A `@Component` calling `EvaluationRepository.save(EvaluationEntity.fromDomain(evaluation))` and returning `checkNotNull(saved.id) { "evaluation id was not generated" }`.

##### 14. `A/gateway/SaveAnswersProvider.kt` (Create)

*   **Type / Scope**: Adapter.
*   **Proposed Changes**: A `@Component` calling `answerRepository.saveAll(answers.map { AnswerEntity.fromDomain(evaluationId, it) })`.

##### 15. `A/gateway/postgres/EvaluationRepository.kt` (Create)

*   **Type / Scope**: Spring Data repository.
*   **Proposed Changes**: `interface EvaluationRepository : JpaRepository<EvaluationEntity, Long>`.

##### 16. `A/gateway/postgres/AnswerRepository.kt` (Create)

*   **Type / Scope**: Spring Data repository.
*   **Proposed Changes**: `interface AnswerRepository : JpaRepository<AnswerEntity, Long>`.

##### 17. `A/gateway/postgres/entity/EvaluationEntity.kt` (Create)

*   **Type / Scope**: JPA entity.
*   **Proposed Changes**: `@Entity @Table(name = "evaluation") data class EvaluationEntity`, with these members:
    *   `@Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long? = null`.
    *   `val studentName: String`, `val studentRegistration: String`, `val examCode: String`, and `val examTitle: String`, each with `@Column(name = ...)` set to `student_name`, `student_registration`, `exam_code`, and `exam_title`.
    *   `fun fromDomain(evaluation: Evaluation): EvaluationEntity` in its `companion object`.

    The `kotlin-jpa` plugin already applied in `application/build.gradle.kts` generates the no-argument constructor JPA needs.

##### 18. `A/gateway/postgres/entity/AnswerEntity.kt` (Create)

*   **Type / Scope**: JPA entity.
*   **Proposed Changes**: `@Entity @Table(name = "answer") data class AnswerEntity`, with these members:
    *   `@Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long? = null`.
    *   `val evaluationId: Long`, `val questionNumber: Int`, and `val chosenOption: String`, each with `@Column(name = ...)` set to `evaluation_id`, `question_number`, and `chosen_option`.
    *   `fun fromDomain(evaluationId: Long, answer: Answer): AnswerEntity` in its `companion object`.

    It has no JPA relation, to keep the two steps separate.

##### 19. `application/src/test/kotlin/br/com/talk/evaluation/RecordEvaluationIntegrationTest.kt` (Create)

*   **Type / Scope**: Integration test.
*   **Proposed Changes**: A `@SpringBootTest(webEnvironment = NONE)` class with no `@Transactional`.
    *   **Container:** a `companion object` holds `@JvmStatic val postgres = EvaluationPostgresContainer().withInitScript("evaluation-schema.sql").apply { start() }`, where `class EvaluationPostgresContainer : PostgreSQLContainer<EvaluationPostgresContainer>("postgres:16.4-alpine")`. `PostgreSQLContainer<Nothing>` does not work in Kotlin: its builder methods return the type parameter, so Kotlin treats every call after them as unreachable. Starting it in the companion makes a missing Docker fail the class instead of skipping it. The init script runs once, when the container starts. Spring's own schema initialization is not used.
    *   **Datasource:** a `@JvmStatic @DynamicPropertySource` function registers `spring.datasource.url`, `spring.datasource.username`, and `spring.datasource.password` from the container.
    *   **Shared setup:** `@Autowired` `RecordEvaluationUseCase`, `SaveEvaluationGateway`, `SaveAnswersGateway`, and `JdbcTemplate`. `@BeforeEach` runs `delete from answer`, then `delete from evaluation`. Assertions use JUnit 5 (`assertThrows`, `assertEquals`). Row counts are read with `JdbcTemplate`, outside any transaction. Every test uses the input `Evaluation("Ana Souza", "2026001", "SOLID-01", "SOLID and Clean Architecture")`.
    *   **Constraint check:** walk the cause chain of the thrown exception to `org.hibernate.exception.ConstraintViolationException`. Assert that its `constraintName` is `uq_answer_evaluation_question` and that its `sqlException.sqlState` is `23505`. This avoids importing the driver, which stays `runtimeOnly`.

    | Test | Answers | Expected |
    | --- | --- | --- |
    | `context loads` | None | The context starts |
    | `saves evaluation and answers when both steps succeed` | `Answer(1, "A")`, `Answer(2, "C")` | One `evaluation` row, and two `answer` rows with the returned id |
    | `rolls back evaluation when saving answers fails` | `Answer(1, "A")`, `Answer(1, "B")` | `DataIntegrityViolationException`, the constraint check, and zero rows in both tables |
    | `keeps evaluation without a transaction when saving answers fails` | The same duplicate answers, through `RecordEvaluationUseCase(PassThroughTransactionGateway(), saveEvaluationGateway, saveAnswersGateway)` | The same exception and constraint check, one `evaluation` row, and zero `answer` rows |

##### 20. `application/src/test/kotlin/br/com/talk/evaluation/PassThroughTransactionGateway.kt` (Create)

*   **Type / Scope**: Test double.
*   **Proposed Changes**: Implements `TransactionGateway` by calling `block()` only. It is not a bean, so Spring sees exactly one `TransactionGateway` bean and injection stays unambiguous.

#### Documentation

> **Changed after implementation:** READMEs were later kept only in folders with logic or architecture contracts: use cases, adapters, configuration, and domain gateway ports, plus the root README. The READMEs for models, entities, request and response bodies, exceptions, repository interfaces, modules, and intermediate folders were removed, and their key facts moved into the README of the folder that uses them. Items 21 to 26 describe the original plan.

##### 21. New package contracts (Create)

*   **Type / Scope**: Documentation.
*   **Files**: `D/README.md`, `D/gateway/README.md`, `D/model/README.md`, `D/usecase/README.md`, `A/README.md`, `A/gateway/README.md`, `A/gateway/postgres/README.md`, and `A/gateway/postgres/entity/README.md`.
*   **Proposed Changes**: Contracts for the new packages, in the repository's existing format. `D/gateway/README.md` repeats the `TransactionGateway` contract.

##### 22. `README.md` (Modify)

*   **Type / Scope**: Documentation.
*   **Proposed Changes**: Add the new flow and a short glossary for assessment and evaluation. Replace the statement that no driver or datasource exists. Say that `./gradlew test` needs Docker and JDK 11, and that the assessment flow remains unrepaired. Use "local transaction, one database" throughout.

##### 23. `application/README.md` (Modify)

*   **Type / Scope**: Documentation.
*   **Proposed Changes**: Add the datasource environment variables and the limits of local startup: the new tables do not exist and there is no endpoint.

##### 24. `application/src/main/kotlin/br/com/talk/README.md` and `domain/src/main/kotlin/br/com/talk/README.md` (Modify)

*   **Type / Scope**: Documentation.
*   **Proposed Changes**: Add links to the new `evaluation` contracts.

##### 25. `application/src/main/kotlin/br/com/talk/assessment/gateway/postgres/README.md` (Modify)

*   **Type / Scope**: Documentation.
*   **Proposed Changes**: Replace the statement that no driver or datasource exists, since this feature adds them.

##### 26. `domain/src/README.md` and the domain READMEs saying "no test sources" (Modify)

*   **Type / Scope**: Documentation.
*   **Files**: `domain/src/README.md`, and under `domain/src/main/kotlin/br/com/talk/`: `assessment/gateway/README.md`, `assessment/model/README.md`, `assessment/usecase/README.md`, and `exception/README.md`.
*   **Proposed Changes**: Item 11 makes the current wording false. In `domain/src/README.md`, replace "Only `main` exists." with "`test/` holds only test code, so it has no contract." In the other four files, change the Verification line to "Run: `./gradlew :domain:test`".

No existing assessment Kotlin file changes. `2026-10-31_DRIFT_REPORT.md` is a dated record and is not edited.

### Phase Order

1.  **Prove the base:**
    *   Record the starting commit as `BASE`.
    *   Run `./gradlew help`, `./gradlew :application:compileKotlin`, `./gradlew :application:compileTestKotlin`, `./gradlew :application:dependencies --configuration testRuntimeClasspath`, and `docker version`.
    *   Apply the Known build fixes as needed.
    *   Add items 1 to 4, and item 19 with only the context-load test.
2.  **Domain and adapters:** items 5 to 18. The use case and its three gateway implementations arrive together, so the context-load test keeps passing.
3.  **Success proof:** the success test in item 19.
4.  **Rollback proof:** the failure and control tests in item 19, and item 20.
5.  **Finish:** items 21 to 26.

### Verification Plan

*   **Compilation/Build**: `./gradlew build`.
*   **Unit Tests**: `./gradlew :domain:test`.
*   **Static Analysis & Linting**: SonarQube is configured but is not run as part of verification. No linter is configured, and this feature adds none.
*   **Test Coverage Requirement**: None. JaCoCo's minimum stays at `0.0`, because the proof comes from the three named integration tests.
*   **E2E/Integration**: `./gradlew :application:test` with Docker running. No test is skipped.
*   **Proof of the rollback**: the failure test shows zero rows in both tables, and the control test shows one evaluation row with the same input. If the control test finds zero evaluation rows, the failure test proves nothing, and the work is not done.
*   **Domain purity**: `grep -rn "org.springframework" domain/src --include=*.kt domain/build.gradle.kts` finds nothing.
*   **Assessment unchanged**: `git diff --stat $BASE..HEAD -- application/src/main/kotlin/br/com/talk/assessment domain/src/main/kotlin/br/com/talk/assessment` prints nothing. `BASE` is the commit recorded at the start of Phase 1.
*   **Local startup**: with `DATABASE_URL`, `DATABASE_USERNAME`, and `DATABASE_PASSWORD` pointing at a local Postgres, `./gradlew :application:bootRun` starts, and `GET http://localhost:9011/actuator/health` returns status `UP`.

--------------------------------------------------------------------------------

*Drafted with EGM*
