package br.com.talk.assessment.gateway.postgres.entity

import br.com.talk.assessment.model.Assessement
import br.com.talk.assessment.model.Questionary
import javax.persistence.*

@Entity(name = "assessment")
data class AssessmentEntity(
    @Id val id : Long? = null,
    val title : String,
    @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "question_id", referencedColumnName = "id", nullable = false, insertable = false, updatable = true)
    val listQuestions : List<QuestionsEntity> = listOf(),
){
    fun toDomain(): Assessement {
      return Assessement(
          id = id,
          title = title,
          listQuestions = listQuestions.map { it.content }
      )
    }

    companion object {
        fun fromDomain(questionary: Questionary): AssessmentEntity {
            return AssessmentEntity(
                title = questionary.title,
                listQuestions = questionary.listQuestions.map { QuestionsEntity.fromDomain(it) }
            )
        }
    }
}



@Entity(name = "question")
data class QuestionsEntity (
    @Id
    val id: Long? = null,
    val content: String,
    ){
    companion object{
        fun fromDomain(question : String): QuestionsEntity {
            return QuestionsEntity(content = question)
        }
    }
}