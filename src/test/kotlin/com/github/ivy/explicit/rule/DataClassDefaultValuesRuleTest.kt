package com.github.ivy.explicit.rule

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.rules.KotlinCoreEnvironmentTest
import io.gitlab.arturbosch.detekt.test.compileAndLintWithContext
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.junit.jupiter.api.Test

@KotlinCoreEnvironmentTest
internal class DataClassDefaultValuesRuleTest(private val env: KotlinCoreEnvironment) {

    @Test
    fun `reports data class with a default value`() {
        val code = """
        data class A(
            val x: Int = 42
        )
        """
        val findings = DataClassDefaultValuesRule(Config.empty).compileAndLintWithContext(env, code)
        findings shouldHaveSize 1
        val message = findings.first().message
        message shouldBe """
            Data class 'A' should not have default values for properties. Found default value for property 'x: Int = 42'. This can lead to implicit instance constructions and problems.
        """.trimIndent()
    }

    @Test
    fun `reports data class with a override default value`() {
        val code = """
        data class A(
            override val x: Int = 0,
            override val y: Int = 0,
        ): Point
        """
        val findings = DataClassDefaultValuesRule(Config.empty).compileAndLintWithContext(env, code)
        findings shouldHaveSize 2
    }

    @Test
    fun `doesn't report class with a default value`() {
        val code = """
        class A(
            val x: Int = 42
        )
        """
        val findings = DataClassDefaultValuesRule(Config.empty).compileAndLintWithContext(env, code)
        findings shouldHaveSize 0
    }

    @Test
    fun `doesn't report data class without default values`() {
        val code = """
        class Point(
            val x: Double,
            val y: Double,
        )
        """
        val findings = DataClassDefaultValuesRule(Config.empty).compileAndLintWithContext(env, code)
        findings shouldHaveSize 0
    }

}
