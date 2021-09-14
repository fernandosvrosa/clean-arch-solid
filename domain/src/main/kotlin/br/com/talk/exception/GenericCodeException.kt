package br.com.talk.exception

open class GenericCodeException(message: String? = "", val code: String? = null) : RuntimeException(message) {
}