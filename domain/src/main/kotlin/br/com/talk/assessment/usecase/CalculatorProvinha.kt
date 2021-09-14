package br.com.talk.assessment.usecase

import br.com.talk.assessment.model.AssessmentType
import br.com.talk.assessment.model.Result
import br.com.talk.assessment.model.Tri
import javax.inject.Named

@Named
class CalculatorProvinha : Calculator {

    override fun isAssessementType(assessmentType: AssessmentType) = AssessmentType.PROVINHA == assessmentType

    override fun execute(tri: Tri, results: List<Result>): Float {
        TODO("Logica para calculo do provinha")
    }
}