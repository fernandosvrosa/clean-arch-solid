# gateway (domain)

## Overview
Output ports of the assessment domain. Use cases depend on these interfaces, and the `application` module implements them in [its gateway package](../../../../../../../../../application/src/main/kotlin/br/com/talk/assessment/gateway/README.md).

## Interface
- `interface GetQuestionaryGateway`: `fun execute(questionaryCode: String): Questionary`
- `interface SaveAssessmentGateway`: `fun execute(questionary: Questionary): Assessement`

## Invariants
- None

## Side Effects
- None: these are interfaces only. Effects belong to the implementations.

## Verification
- Run: `./gradlew :domain:test`
