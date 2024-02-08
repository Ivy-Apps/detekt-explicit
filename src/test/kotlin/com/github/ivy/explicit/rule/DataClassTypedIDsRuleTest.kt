package com.github.ivy.explicit.rule

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.rules.KotlinCoreEnvironmentTest
import io.gitlab.arturbosch.detekt.test.compileAndLintWithContext
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@KotlinCoreEnvironmentTest
internal class DataClassTypedIDsRuleTest(private val env: KotlinCoreEnvironment) {

    private lateinit var rule: DataClassTypedIDsRule

    @BeforeEach
    fun setup() {
        rule = DataClassTypedIDsRule(Config.empty)
    }


    @Test
    fun `reports data class having UUID as id`() {
        // given
        val code = """
        data class A(
            val id: UUID,
            val name: String,
        )
        """

        // when
        val findings = rule.compileAndLintWithContext(env, code)

        // then
        findings shouldHaveSize 1
        val message = findings.first().message
        message shouldBe """
            Data class 'A' should use type-safe IDs instead of UUID for property 'id'. Typed-IDs like `value class SomeId(val id: UUID)` provide compile-time safety and prevent mixing IDs of different entities.
        """.trimIndent()
    }

    @Test
    fun `reports data class having String as id`() {
        // given
        val code = """
        data class A(
            val id: String,
        )
        """

        // when
        val findings = rule.compileAndLintWithContext(env, code)

        // then
        findings shouldHaveSize 1
    }

    @Test
    fun `reports data class having transactionId Int`() {
        // given
        val code = """
        data class A(
            val name: String,
            val transactionId: Int
        )
        """

        // when
        val findings = rule.compileAndLintWithContext(env, code)

        // then
        findings shouldHaveSize 1
    }

    @Test
    fun `doesn't report data class without ids`() {
        // given
        val code = """
        data class Person(
            val firstName: String,
            val lastName: String,
        )
        """

        // when
        val findings = rule.compileAndLintWithContext(env, code)

        // then
        findings shouldHaveSize 0
    }

    @Test
    fun `doesn't report Dto classes`() {
        // given
        val code = """
        data class SomeDto(
            val id: UUID
        )
        """

        // when
        val findings = rule.compileAndLintWithContext(env, code)

        // then
        findings shouldHaveSize 0
    }

    @Test
    fun `doesn't report Entity classes`() {
        // given
        val code = """
        data class SomeEntity(
            val id: UUID
        )
        """

        // when
        val findings = rule.compileAndLintWithContext(env, code)

        // then
        findings shouldHaveSize 0
    }

    @Test
    fun `doesn't report classes annotated with @Entity`() {
        // given
        val code = """
        @Entity(tableName = "budgets")
        data class A(
            val id: UUID
        )
        """

        // when
        val findings = rule.compileAndLintWithContext(env, code)

        // then
        findings shouldHaveSize 0
    }

    @Test
    fun `doesn't report classes annotated with @Serializable`() {
        // given
        val code = """
        @Serializable
        data class A(
            val id: UUID
        )
        """

        // when
        val findings = rule.compileAndLintWithContext(env, code)

        // then
        findings shouldHaveSize 0
    }
}
