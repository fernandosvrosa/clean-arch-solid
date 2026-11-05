package br.com.talk.evaluation.usecase

import br.com.talk.evaluation.gateway.SaveAnswersGateway
import br.com.talk.evaluation.gateway.SaveEvaluationGateway
import br.com.talk.evaluation.gateway.TransactionGateway
import br.com.talk.evaluation.model.Answer
import br.com.talk.evaluation.model.Evaluation
import javax.inject.Named

@Named
class RecordEvaluationUseCase(
    private val transactionGateway: TransactionGateway,
    private val saveEvaluationGateway: SaveEvaluationGateway,
    private val saveAnswersGateway: SaveAnswersGateway
) {

    fun execute(evaluation: Evaluation, answers: List<Answer>): Long =
        transactionGateway.execute {
            val evaluationId = saveEvaluationGateway.execute(evaluation)
            saveAnswersGateway.execute(evaluationId, answers)
            evaluationId
        }
}
