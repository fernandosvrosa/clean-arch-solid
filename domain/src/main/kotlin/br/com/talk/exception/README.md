# exception

## Overview
Exception types raised by the domain. Each one carries a message code that the application layer resolves into a localized message.

## Interface
- `open class GenericCodeException(message: String? = "", val code: String? = null) : RuntimeException(message)`
- `class UnprocessableEntityException(message: String? = null, code: String = "") : GenericCodeException(message, code)`

## Invariants
- None

## Side Effects
- None

## Verification
- Run: `./gradlew :domain:test`
