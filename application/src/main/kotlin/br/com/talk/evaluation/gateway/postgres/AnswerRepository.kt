package br.com.talk.evaluation.gateway.postgres

import br.com.talk.evaluation.gateway.postgres.entity.AnswerEntity
import org.springframework.data.jpa.repository.JpaRepository

interface AnswerRepository : JpaRepository<AnswerEntity, Long>
