package br.com.talk.assessment.gateway

import br.com.talk.assessment.model.Questionary
import org.springframework.stereotype.Component

@Component
class GetQuestionaryProvider : GetQuestionaryGateway {
    override fun execute(questionaryCode: String): Questionary {
        // pode criar um pakage de http para chamar outro microservico

        return Questionary(
            id = 1,
            title = "Clean arch",
            listQuestions = listOf("SOLID", "Clean Arch")
        )
    }
}