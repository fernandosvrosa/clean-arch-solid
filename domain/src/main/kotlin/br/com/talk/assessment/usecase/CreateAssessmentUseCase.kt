package br.com.talk.assessment.usecase

import br.com.talk.assessment.gateway.GetQuestionaryGateway
import br.com.talk.assessment.gateway.SaveAssessmentGateway
import br.com.talk.assessment.model.Assessement
import javax.inject.Named

@Named
class CreateAssessmentUseCase (
    private val getQuestionaryGateway : GetQuestionaryGateway,
    private val saveAssessmentGateway: SaveAssessmentGateway) {

    fun execute(questionaryCode : String): Assessement {
        val questionary =  getQuestionaryGateway.execute(questionaryCode)
        val assessement = saveAssessmentGateway.execute(questionary)
        return assessement;
    }

}