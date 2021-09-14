package br.com.talk.assessment.usecase

import br.com.talk.assessment.model.AssessmentType
import br.com.talk.assessment.model.Result
import br.com.talk.assessment.model.Tri

interface Calculator {

    fun isAssessementType(assessmentType: AssessmentType) : Boolean

    fun execute(tri : Tri, results : List<Result>) : Float
}