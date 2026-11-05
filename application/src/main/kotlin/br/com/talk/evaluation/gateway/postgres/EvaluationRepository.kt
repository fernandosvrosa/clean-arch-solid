package br.com.talk.evaluation.gateway.postgres

import br.com.talk.evaluation.gateway.postgres.entity.EvaluationEntity
import org.springframework.data.jpa.repository.JpaRepository

interface EvaluationRepository : JpaRepository<EvaluationEntity, Long>
