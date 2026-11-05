package br.com.talk.evaluation.gateway

/**
 * Runs [block] so that all of its database writes succeed or fail together,
 * as one local transaction on one database.
 *
 * - If [block] returns normally, the work commits and its result is returned.
 * - If [block] throws, the work rolls back and the error reaches the caller.
 * - [block] runs synchronously, on the caller's thread.
 * - [block] must not catch errors from the gateways, and must not make network calls.
 *
 * This does not cover a second datasource or a remote service. Those need a new design,
 * not a new implementation of this interface.
 */
interface TransactionGateway {
    fun <T> execute(block: () -> T): T
}
