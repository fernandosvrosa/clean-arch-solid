# model (evaluation)

## Overview
Data for one student's attempt at an exam. Student and exam data repeat on each evaluation on purpose, to keep the example to two tables.

## Interface
- `data class Evaluation(val studentName: String, val studentRegistration: String, val examCode: String, val examTitle: String)`
- `data class Answer(val questionNumber: Int, val chosenOption: String)`

## Invariants
- All properties are read-only (`val`) and non-nullable.

## Side Effects
- None

## Verification
- Run: `./gradlew :domain:test`
