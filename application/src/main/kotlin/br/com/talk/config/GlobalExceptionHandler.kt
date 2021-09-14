package br.com.talk.config

import br.com.talk.exception.GenericCodeException
import org.slf4j.LoggerFactory
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus

@ControllerAdvice
class GlobalExceptionHandler(
        private val messageSource: MessageSource
) {

    companion object {
        private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
    }

    @ExceptionHandler(value = [(Exception::class), (java.lang.Exception::class)])
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun exception(e: GenericCodeException): ResponseEntity<ErrorMessage> {
        log.error("Exception", e)
        return ResponseEntity(resolveExceptionMessage(e), HttpStatus.INTERNAL_SERVER_ERROR)
    }

    private fun resolveExceptionMessage(e: GenericCodeException) =
            if (e.code != null) {
                ErrorMessage(e.code!!, messageSource.getMessage(e.code!!, null, LocaleContextHolder.getLocale()))
            } else {
                ErrorMessage("", "")
            }

}




