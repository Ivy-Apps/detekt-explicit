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
internal class DataClassDefaultValuesRuleTest(private val env: KotlinCoreEnvironment) {

    private lateinit var rule: DataClassDefaultValuesRule

    @BeforeEach
    fun setup() {
        rule = DataClassDefaultValuesRule(Config.empty)
    }

    @Test
    fun `reports data class with a default value`() {
        // given
        val code = """
        data class A(
            val x: Int = 42
        )
        """

        // when
        val findings = rule.compileAndLintWithContext(env, code)

        // then
        findings shouldHaveSize 1
        val message = findings.first().message
        message shouldBe """
            Data class 'A' should not have default values. Found default value for property 'x: Int = 42'. This allows for instances of 'A' to be created without explicitly specifying all properties, potentially leading to unintended or inconsistent states.
        """.trimIndent()
    }

    @Test
    fun `reports data class with a override default value`() {
        // given
        val code = """
        data class A(
            override val x: Int = 0,
            override val y: Int = 0,
        ): Point
        """

        // when
        val findings = rule.compileAndLintWithContext(env, code)

        // then
        findings shouldHaveSize 2
    }

    @Test
    fun `doesn't report class with a default value`() {
        // given
        val code = """
        class A(
            val x: Int = 42
        )
        """

        // when
        val findings = DataClassDefaultValuesRule(Config.empty).compileAndLintWithContext(env, code)

        // then
        findings shouldHaveSize 0
    }

    @Test
    fun `doesn't report data class without default values`() {
        // given
        val code = """
        class Point(
            val x: Double,
            val y: Double,
        )
        """

        // when
        val findings = rule.compileAndLintWithContext(env, code)

        // then
        findings shouldHaveSize 0
    }

}
