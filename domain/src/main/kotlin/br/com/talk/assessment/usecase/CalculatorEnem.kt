package br.com.talk.assessment.usecase

import br.com.talk.assessment.model.AssessmentType
import br.com.talk.assessment.model.Result
import br.com.talk.assessment.model.Tri
import javax.inject.Named

@Named
class CalculatorEnem : Calculator {
    override fun isAssessementType(assessmentType: AssessmentType) = AssessmentType.ENEM == assessmentType

    override fun execute(tri: Tri, results: List<Result>): Float {
        TODO("Logica para calculo do Enem")
    }
}