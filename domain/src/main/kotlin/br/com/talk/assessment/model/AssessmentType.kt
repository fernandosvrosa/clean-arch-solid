package br.com.talk.assessment.model

import br.com.talk.exception.UnprocessableEntityException

enum class AssessmentType(val value: String)  {
    ENEM("enem"),PROVINHA("provinha");


    companion object {
        fun toAssessmentType(value: String) =
            values().firstOrNull { it.value == value.toLowerCase() }
                ?: throw UnprocessableEntityException (code = "assessement-type.invalid-type")
    }
}
