package com.github.ivy.explicit

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.rules.KotlinCoreEnvironmentTest
import io.gitlab.arturbosch.detekt.test.compileAndLintWithContext
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.string.shouldNotBeBlank
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.junit.jupiter.api.Test

@KotlinCoreEnvironmentTest
internal class DataClassFunctionsRuleTest(private val env: KotlinCoreEnvironment) {

    @Test
    fun `reports data class having functions`() {
        val code = """
        data class A(
            val x: Int
        ) {
            fun a() = 42
        }
        """
        val findings = DataClassFunctionsRule(Config.empty).compileAndLintWithContext(env, code)
        findings shouldHaveSize 1
        val message = findings.first().message
        message.shouldNotBeBlank()
    }

    @Test
    fun `doesn't report data class without functions`() {
        val code = """
        data class A(
            val x: Int
        )
        """
        val findings = DataClassFunctionsRule(Config.empty).compileAndLintWithContext(env, code)
        findings shouldHaveSize 0
    }

    @Test
    fun `doesn't report data class with companion object`() {
        val code = """
        data class A(
            val x: Int
        ) {
            companion object {
                fun a() = 42
            }
        }
        """
        val findings = DataClassFunctionsRule(Config.empty).compileAndLintWithContext(env, code)
        findings shouldHaveSize 0
    }
}
