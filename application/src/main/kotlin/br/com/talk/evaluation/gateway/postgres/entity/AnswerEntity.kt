package br.com.talk.evaluation.gateway.postgres.entity

import br.com.talk.evaluation.model.Answer
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

// Holds a plain evaluationId with no JPA relation, so saving answers stays a separate step.
@Entity
@Table(name = "answer")
data class AnswerEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(name = "evaluation_id")
    val evaluationId: Long,
    @Column(name = "question_number")
    val questionNumber: Int,
    @Column(name = "chosen_option")
    val chosenOption: String
) {
    companion object {
        fun fromDomain(evaluationId: Long, answer: Answer) = AnswerEntity(
            evaluationId = evaluationId,
            questionNumber = answer.questionNumber,
            chosenOption = answer.chosenOption
        )
    }
}
