package com.github.ivy.explicit.rule

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.rules.KotlinCoreEnvironmentTest
import io.gitlab.arturbosch.detekt.test.compileAndLintWithContext
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.junit.jupiter.api.Test

@KotlinCoreEnvironmentTest
internal class NoImplicitFunctionReturnTypeRuleTest(private val env: KotlinCoreEnvironment) {

    @Test
    fun `reports function with implicit return type`() {
        val code = """
        fun magicNumber() = 42
        """
        val findings = NoImplicitFunctionReturnTypeRule(Config.empty).compileAndLintWithContext(env, code)
        findings shouldHaveSize 1
        val message = findings.first().message
        message shouldBe """
        The function 'magicNumber()' should declare an explicit return type. Implicit (missing) return types make the code harder to read and reason about. Changing the implementation of such function is error-prone and can lead to regressions.
        """.trimIndent()
    }

    @Test
    fun `reports class method with implicit return type`() {
        val code = """
        class A {
            fun a() = "Hello, world!"
        }
        """
        val findings = NoImplicitFunctionReturnTypeRule(Config.empty).compileAndLintWithContext(env, code)
        findings shouldHaveSize 1
    }

    @Test
    fun `doesn't report class method with explicit return type`() {
        val code = """
        class A {
            fun a(): String = "Hello, world!"
        }
        """
        val findings = NoImplicitFunctionReturnTypeRule(Config.empty).compileAndLintWithContext(env, code)
        findings shouldHaveSize 0
    }

    @Test
    fun `doesn't report function with explicit return type`() {
        val code = """
        fun a(): String = "Hello, world!"
        """
        val findings = NoImplicitFunctionReturnTypeRule(Config.empty).compileAndLintWithContext(env, code)
        findings shouldHaveSize 0
    }
}