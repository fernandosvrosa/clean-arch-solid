package br.com.talk.assessment.gateway

import br.com.talk.assessment.model.Assessement
import br.com.talk.assessment.model.Questionary

interface SaveAssessmentGateway {
    fun execute(questionary : Questionary) : Assessement
}