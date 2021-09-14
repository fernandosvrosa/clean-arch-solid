package br.com.talk.assessment.entrypoint

import br.com.talk.assessment.entrypoint.request.CreateAssessment
import br.com.talk.assessment.entrypoint.response.CreateAssessement
import br.com.talk.assessment.usecase.CreateAssessmentUseCase
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping(value = ["/api/v1/lesson-plans"], produces = [MediaType.APPLICATION_JSON_VALUE])
class AssessmentController (private val createAssessmentUseCase: CreateAssessmentUseCase){


    @PostMapping
    fun save(@RequestBody createAssessment: CreateAssessment): ResponseEntity<CreateAssessement> {
        val assessemnt = createAssessmentUseCase.execute(createAssessment.questionaryCode)
        return ResponseEntity(CreateAssessement(assessemnt.id!!), HttpStatus.CREATED)
    }

}