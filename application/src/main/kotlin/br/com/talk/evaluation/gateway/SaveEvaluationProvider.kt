package br.com.talk.evaluation.gateway

import br.com.talk.evaluation.gateway.postgres.EvaluationRepository
import br.com.talk.evaluation.gateway.postgres.entity.EvaluationEntity
import br.com.talk.evaluation.model.Evaluation
import org.springframework.stereotype.Component

@Component
class SaveEvaluationProvider(private val evaluationRepository: EvaluationRepository) : SaveEvaluationGateway {
    override fun execute(evaluation: Evaluation): Long {
        val saved = evaluationRepository.save(EvaluationEntity.fromDomain(evaluation))
        return checkNotNull(saved.id) { "evaluation id was not generated" }
    }
}
