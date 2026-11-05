# config

## Overview
Spring configuration for error handling, internationalization, the `RestTemplate` bean, and Swagger.

## Interface
- `data class ErrorMessage(val code: String, val message: String)`
- `class GlobalExceptionHandler(messageSource: MessageSource)` (`@ControllerAdvice`): `fun exception(e: GenericCodeException): ResponseEntity<ErrorMessage>`
- `class InternationalizationMessagesConfig : WebMvcConfigurer`: beans `localeResolver` and `localeInterceptor`, plus `addInterceptors`
- `class RestTemplateConfiguration`: bean `restTemplate`
- `class SwaggerConfiguration`: bean `swaggerSpringPlugin`

## Invariants
- `GlobalExceptionHandler.exception` declares `@ExceptionHandler` for `Exception`, but its parameter is typed `GenericCodeException`.
- `GlobalExceptionHandler.exception` always responds with HTTP 500; no other status is mapped here.
- A non-null `e.code` is resolved with `messageSource.getMessage`. A null code yields `ErrorMessage("", "")`.
- The default locale is `en-US`, and the locale-change request parameter is `lang`.
- `SwaggerConfiguration` is active only under the `dev` and `staging` profiles and documents paths matching `/api/.*`.

## Side Effects
- `GlobalExceptionHandler.exception` logs each handled exception with `log.error`.
- `GlobalExceptionHandler` reads the thread-bound locale from `LocaleContextHolder`.
- `localeResolver` is a `SessionLocaleResolver`, which stores the locale in the HTTP session.
- `addInterceptors` registers `LocaleChangeInterceptor` with no path patterns.

## Verification
- Run: `./gradlew :application:test`
