package br.com.talk.assessment.gateway

import br.com.talk.assessment.gateway.postgres.AssessmentRepository
import br.com.talk.assessment.gateway.postgres.entity.AssessmentEntity
import br.com.talk.assessment.model.Assessement
import br.com.talk.assessment.model.Questionary
import org.springframework.stereotype.Component

@Component
class SaveAssessmentProvider(private val assessmentRepository : AssessmentRepository) : SaveAssessmentGateway {
    override fun execute(questionary: Questionary): Assessement {
      val assessemenEntity =  assessmentRepository.save(AssessmentEntity.fromDomain(questionary))
      return assessemenEntity.toDomain()
    }
}