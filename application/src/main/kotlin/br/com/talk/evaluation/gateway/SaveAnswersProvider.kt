package br.com.talk.evaluation.gateway

import br.com.talk.evaluation.gateway.postgres.AnswerRepository
import br.com.talk.evaluation.gateway.postgres.entity.AnswerEntity
import br.com.talk.evaluation.model.Answer
import org.springframework.stereotype.Component

@Component
class SaveAnswersProvider(private val answerRepository: AnswerRepository) : SaveAnswersGateway {
    override fun execute(evaluationId: Long, answers: List<Answer>) {
        answerRepository.saveAll(answers.map { AnswerEntity.fromDomain(evaluationId, it) })
    }
}
