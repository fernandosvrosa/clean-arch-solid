package br.com.talk.evaluation.gateway

import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionTemplate

@Component
class TransactionProvider(private val transactionTemplate: TransactionTemplate) : TransactionGateway {

    // TransactionTemplate.execute is annotated @Nullable, so Kotlin sees T?; the block's own type is T.
    @Suppress("UNCHECKED_CAST")
    override fun <T> execute(block: () -> T): T =
        transactionTemplate.execute { block() } as T
}
