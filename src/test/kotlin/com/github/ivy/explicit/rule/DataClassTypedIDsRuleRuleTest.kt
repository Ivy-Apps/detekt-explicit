package com.github.ivy.explicit.rule

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.rules.KotlinCoreEnvironmentTest
import io.gitlab.arturbosch.detekt.test.compileAndLintWithContext
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.junit.jupiter.api.Test

@KotlinCoreEnvironmentTest
internal class DataClassTypedIDsRuleRuleTest(private val env: KotlinCoreEnvironment) {

    @Test
    fun `reports data class having UUID as id`() {
        val code = """
        data class A(
            val id: UUID,
            val name: String,
        )
        """
        val findings = DataClassTypedIDsRule(Config.empty).compileAndLintWithContext(env, code)
        findings shouldHaveSize 1
        val message = findings.first().message
        message shouldBe """
            Data class 'A' should use type-safe IDs instead of UUID for property 'id'. Typed-IDs like `value class SomeId(val id: UUID)` provide compile-time safety and prevent mixing IDs of different entities.
        """.trimIndent()
    }

    @Test
    fun `reports data class having String as id`() {
        val code = """
        data class A(
            val id: String,
        )
        """
        val findings = DataClassTypedIDsRule(Config.empty).compileAndLintWithContext(env, code)
        findings shouldHaveSize 1
    }

    @Test
    fun `reports data class having transactionId Int`() {
        val code = """
        data class A(
            val name: String,
            val transactionId: Int
        )
        """
        val findings = DataClassTypedIDsRule(Config.empty).compileAndLintWithContext(env, code)
        findings shouldHaveSize 1
    }

    @Test
    fun `doesn't report data class without ids`() {
        val code = """
        data class Person(
            val firstName: String,
            val lastName: String,
        )
        """
        val findings = DataClassTypedIDsRule(Config.empty).compileAndLintWithContext(env, code)
        findings shouldHaveSize 0
    }

    @Test
    fun `doesn't report Dto classes`() {
        val code = """
        data class SomeDto(
            val id: UUID
        )
        """
        val findings = DataClassTypedIDsRule(Config.empty).compileAndLintWithContext(env, code)
        findings shouldHaveSize 0
    }

    @Test
    fun `doesn't report Entity classes`() {
        val code = """
        data class SomeEntity(
            val id: UUID
        )
        """
        val findings = DataClassTypedIDsRule(Config.empty).compileAndLintWithContext(env, code)
        findings shouldHaveSize 0
    }

    @Test
    fun `doesn't report classes annotated with @Entity`() {
        val code = """
        @Entity(tableName = "budgets")
        data class A(
            val id: UUID
        )
        """
        val findings = DataClassTypedIDsRule(Config.empty).compileAndLintWithContext(env, code)
        findings shouldHaveSize 0
    }

    @Test
    fun `doesn't report classes annotated with @Serializable`() {
        val code = """
        @Serializable
        data class A(
            val id: UUID
        )
        """
        val findings = DataClassTypedIDsRule(Config.empty).compileAndLintWithContext(env, code)
        findings shouldHaveSize 0
    }
}
