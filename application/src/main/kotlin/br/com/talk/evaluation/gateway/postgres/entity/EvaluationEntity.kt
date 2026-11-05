package br.com.talk.evaluation.gateway.postgres.entity

import br.com.talk.evaluation.model.Evaluation
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "evaluation")
data class EvaluationEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(name = "student_name")
    val studentName: String,
    @Column(name = "student_registration")
    val studentRegistration: String,
    @Column(name = "exam_code")
    val examCode: String,
    @Column(name = "exam_title")
    val examTitle: String
) {
    companion object {
        fun fromDomain(evaluation: Evaluation) = EvaluationEntity(
            studentName = evaluation.studentName,
            studentRegistration = evaluation.studentRegistration,
            examCode = evaluation.examCode,
            examTitle = evaluation.examTitle
        )
    }
}
