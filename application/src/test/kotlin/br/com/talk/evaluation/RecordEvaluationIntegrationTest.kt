package br.com.talk.evaluation

import br.com.talk.evaluation.gateway.SaveAnswersGateway
import br.com.talk.evaluation.gateway.SaveEvaluationGateway
import br.com.talk.evaluation.model.Answer
import br.com.talk.evaluation.model.Evaluation
import br.com.talk.evaluation.usecase.RecordEvaluationUseCase
import org.hibernate.exception.ConstraintViolationException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer

class EvaluationPostgresContainer : PostgreSQLContainer<EvaluationPostgresContainer>("postgres:16.4-alpine")

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class RecordEvaluationIntegrationTest {

    companion object {
        @JvmStatic
        val postgres: EvaluationPostgresContainer = EvaluationPostgresContainer()
            .withInitScript("evaluation-schema.sql")
            .apply { start() }

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
        }
    }

    @Autowired
    lateinit var recordEvaluationUseCase: RecordEvaluationUseCase

    @Autowired
    lateinit var saveEvaluationGateway: SaveEvaluationGateway

    @Autowired
    lateinit var saveAnswersGateway: SaveAnswersGateway

    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    private val evaluation = Evaluation("Ana Souza", "2026001", "SOLID-01", "SOLID and Clean Architecture")

    private val duplicatedAnswers = listOf(Answer(1, "A"), Answer(1, "B"))

    @BeforeEach
    fun cleanTables() {
        jdbcTemplate.execute("delete from answer")
        jdbcTemplate.execute("delete from evaluation")
    }

    @Test
    fun `context loads`() {
    }

    @Test
    fun `saves evaluation and answers when both steps succeed`() {
        val id = recordEvaluationUseCase.execute(evaluation, listOf(Answer(1, "A"), Answer(2, "C")))

        assertEquals(1, count("select count(*) from evaluation"))
        assertEquals(2, count("select count(*) from answer where evaluation_id = $id"))
    }

    @Test
    fun `rolls back evaluation when saving answers fails`() {
        val error = assertThrows<DataIntegrityViolationException> {
            recordEvaluationUseCase.execute(evaluation, duplicatedAnswers)
        }

        assertFailedOnAnswerUniqueConstraint(error)
        assertEquals(0, count("select count(*) from evaluation"))
        assertEquals(0, count("select count(*) from answer"))
    }

    @Test
    fun `keeps evaluation without a transaction when saving answers fails`() {
        val useCaseWithoutTransaction =
            RecordEvaluationUseCase(PassThroughTransactionGateway(), saveEvaluationGateway, saveAnswersGateway)

        val error = assertThrows<DataIntegrityViolationException> {
            useCaseWithoutTransaction.execute(evaluation, duplicatedAnswers)
        }

        assertFailedOnAnswerUniqueConstraint(error)
        assertEquals(1, count("select count(*) from evaluation"))
        assertEquals(0, count("select count(*) from answer"))
    }

    private fun assertFailedOnAnswerUniqueConstraint(error: Throwable) {
        val violation = generateSequence(error) { it.cause }
            .filterIsInstance<ConstraintViolationException>()
            .firstOrNull()

        assertNotNull(violation, "expected a ConstraintViolationException in the cause chain")
        assertEquals("uq_answer_evaluation_question", violation!!.constraintName)
        assertEquals("23505", violation.sqlException.sqlState)
    }

    private fun count(sql: String): Int = jdbcTemplate.queryForObject(sql, Int::class.java)!!
}
