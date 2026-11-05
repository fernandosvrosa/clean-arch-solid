package br.com.talk.evaluation

import br.com.talk.evaluation.gateway.TransactionGateway

// Not a bean: Spring sees exactly one TransactionGateway bean, so injection stays unambiguous.
class PassThroughTransactionGateway : TransactionGateway {
    override fun <T> execute(block: () -> T): T = block()
}
