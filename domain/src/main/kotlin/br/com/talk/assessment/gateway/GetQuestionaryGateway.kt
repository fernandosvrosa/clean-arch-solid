package br.com.talk.assessment.gateway

import br.com.talk.assessment.model.Questionary

interface GetQuestionaryGateway {

    fun execute(questionaryCode : String) : Questionary

}