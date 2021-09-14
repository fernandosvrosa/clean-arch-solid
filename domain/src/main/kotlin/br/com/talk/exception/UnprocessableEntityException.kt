package br.com.talk.exception

class UnprocessableEntityException(message: String? = null, code: String = "") : GenericCodeException(message, code) {
}
