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
internal class DataClassFunctionsRuleTest(private val env: KotlinCoreEnvironment) {

    private lateinit var rule: DataClassFunctionsRule

    @BeforeEach
    fun setup() {
        rule = DataClassFunctionsRule(Config.empty)
    }

    @Test
    fun `reports data class having one function`() {
        // given
        val code = """
        data class Abc(
            val x: Int
        ) {
            fun a() = 42
        }
        """

        // when
        val findings = rule.compileAndLintWithContext(env, code)

        // then
        findings shouldHaveSize 1
        val message = findings.first().message
        message shouldBe """
            Data class 'Abc' should not contain functions. Found: function 'a()'. Data classes should only model data and should not be tied to any behavior.
        """.trimIndent()
    }

    @Test
    fun `doesn't report data class without functions`() {
        // given
        val code = """
        data class A(
            val x: Int
        )
        """

        // when
        val findings = rule.compileAndLintWithContext(env, code)

        // then
        findings shouldHaveSize 0
    }

    @Test
    fun `doesn't report data class with override functions`() {
        // given
        val code = """
        data class DisplayLoan(
            val loan: Loan,
            val amountPaid: Double,
            val currencyCode: String? = getDefaultFIATCurrency().currencyCode,
            val formattedDisplayText: String = "",
            val percentPaid: Double = 0.0
        ) : Reorderable {
            override fun getItemOrderNum(): Double {
                return loan.orderNum
            }
        
            override fun withNewOrderNum(newOrderNum: Double): Reorderable {
                return this.copy(
                    loan = loan.copy(
                        orderNum = newOrderNum
                    )
                )
            }
        }
        """

        // when
        val findings = rule.compileAndLintWithContext(env, code)

        // then
        findings shouldHaveSize 0
    }

    @Test
    fun `doesn't report data class with functions in companion object`() {
        // given
        val code = """
        data class A(
            val x: Int
        ) {
            companion object {
                fun a() = 42
            }
        }
        """

        // when
        val findings = rule.compileAndLintWithContext(env, code)

        // then
        findings shouldHaveSize 0
    }
}
