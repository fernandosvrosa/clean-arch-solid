package br.com.talk.assessment.gateway.postgres

import br.com.talk.assessment.gateway.postgres.entity.AssessmentEntity
import org.springframework.data.jpa.repository.JpaRepository

interface AssessmentRepository : JpaRepository<AssessmentEntity, Long> {
}