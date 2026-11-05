package br.com.talk.evaluation.gateway

import br.com.talk.evaluation.model.Evaluation

interface SaveEvaluationGateway {
    fun execute(evaluation: Evaluation): Long
}
