package br.com.talk.evaluation.usecase

import br.com.talk.evaluation.gateway.SaveAnswersGateway
import br.com.talk.evaluation.gateway.SaveEvaluationGateway
import br.com.talk.evaluation.gateway.TransactionGateway
import br.com.talk.evaluation.model.Answer
import br.com.talk.evaluation.model.Evaluation
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RecordEvaluationUseCaseTest {

    private val calls = mutableListOf<String>()

    private val transactionGateway = object : TransactionGateway {
        override fun <T> execute(block: () -> T): T {
            calls.add("begin")
            val result = block()
            calls.add("end")
            return result
        }
    }

    private val saveEvaluationGateway = object : SaveEvaluationGateway {
        override fun execute(evaluation: Evaluation): Long {
            calls.add("evaluation")
            return 42
        }
    }

    private val saveAnswersGateway = object : SaveAnswersGateway {
        override fun execute(evaluationId: Long, answers: List<Answer>) {
            calls.add("answers:$evaluationId:${answers.size}")
        }
    }

    private val useCase = RecordEvaluationUseCase(transactionGateway, saveEvaluationGateway, saveAnswersGateway)

    private val evaluation = Evaluation("Ana Souza", "2026001", "SOLID-01", "SOLID and Clean Architecture")

    @Test
    fun `saves evaluation then answers inside the transaction`() {
        val id = useCase.execute(evaluation, listOf(Answer(1, "A"), Answer(2, "C")))

        assertEquals(42L, id)
        assertEquals(listOf("begin", "evaluation", "answers:42:2", "end"), calls)
    }

    @Test
    fun `saves evaluation with no answers`() {
        useCase.execute(evaluation, emptyList())

        assertEquals(listOf("begin", "evaluation", "answers:42:0", "end"), calls)
    }
}
