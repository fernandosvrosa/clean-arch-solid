package br.com.talk.assessment.usecase

import br.com.talk.assessment.model.AssessmentType
import br.com.talk.assessment.model.Result
import br.com.talk.assessment.model.Tri
import javax.inject.Named

@Named
class CalculatorUseCase(private val calculators: List<Calculator>) {

    fun execute(assessementType: AssessmentType, assessementId: Long): Float {
        val tri = Tri(1, 1.5f)
        val results = listOf(Result(1, "Aluno teste", 23))
        return calculators.first { it.isAssessementType(assessementType) }.execute(tri, results)
    }

}