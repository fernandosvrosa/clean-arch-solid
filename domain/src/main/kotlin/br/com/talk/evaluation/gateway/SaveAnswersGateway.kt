package br.com.talk.evaluation.gateway

import br.com.talk.evaluation.model.Answer

interface SaveAnswersGateway {
    fun execute(evaluationId: Long, answers: List<Answer>)
}
