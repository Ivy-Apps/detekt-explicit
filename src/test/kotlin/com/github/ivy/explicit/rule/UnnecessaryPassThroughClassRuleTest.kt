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
internal class UnnecessaryPassThroughClassRuleTest(private val env: KotlinCoreEnvironment) {

    private lateinit var rule: UnnecessaryPassThroughClassRule

    @BeforeEach
    fun setup() {
        rule = UnnecessaryPassThroughClassRule(Config.empty)
    }


    @Test
    fun `reports an unnecessary pass-through class - case 1`() {
        // given
        val code = """
        class A(val b: B) {
            fun x() {
                return b.x()
            }

            fun y(p1: Int, p2: Double): Double {
                return b.y(p1,p2)
            }

            fun z(text: String) = b.z(text)
        }
        """

        // when
        val findings = rule.compileAndLintWithContext(env, code)

        // then
        findings shouldHaveSize 1
        val message = findings.first().message
        message shouldBe """
        The class 'A' appears to be an unnecessary pass-through class. It only increase complexity and boilerplate code without adding any value. Consider removing the class or adding meaningful logic.
        """.trimIndent()
    }

    @Test
    fun `reports a pass-through class that delegates to two classes`() {
        // given
        val code = """
        class A(val b: B, val c: C) {
            fun x() {
                return b.x()
            }

            fun y(p1: Int, p2: Double): String {
                return c.y(p1,p2)
            }

            fun z(text: String) = b.z(text)
        }
        """

        // when
        val findings = rule.compileAndLintWithContext(env, code)

        // then
        findings shouldHaveSize 1
    }

    @Test
    fun `reports a pass-through DataSource`() {
        // given
        val code = """import java.util.UUID

        class SomeDataSource(val dao: SomeDao): Int {
            fun save(value: Entity) {
                dao.save(value)
            }

            fun findById(id: UUID): Entity? = dao.findById(id)

            fun deleteBy(id: UUID) {
                dao.deleteBy(id)
            }
        }
        """

        // when
        val findings = rule.compileAndLintWithContext(env, code)

        // then
        findings shouldHaveSize 1
    }


    @Test
    fun `does not report interfaces`() {
        // given
        val code = """
        interface A(val b: B) {
            fun x() {
                return b.x()
            }

            fun y(p1: Int, p2: Double) {
                return b.y(p1,p2)
            }

            fun z(text: String) = b.z(text)
        }
        """

        // when
        val findings = rule.compileAndLintWithContext(env, code)

        // then
        findings shouldHaveSize 0
    }

    @Test
    fun `does not report classes that have at least one function with logic`() {
        // given
        val code = """
        class A(val b: B) {
            fun x() {
                return b.x()
            }

            fun y(text: String) = b.z(text.uppercase())
        }
        """

        // when
        val findings = rule.compileAndLintWithContext(env, code)

        // then
        findings shouldHaveSize 0
    }

    @Test
    fun `does not report a class - case 1`() {
        // given
        val code = """
        class A(val b: B): Int {
            fun x(): Boolean {
                return predicate(b.x())
            }
        }
        """

        // when
        val findings = rule.compileAndLintWithContext(env, code)

        // then
        findings shouldHaveSize 0
    }

    @Test
    fun `does not report a class - case 2`() {
        // given
        val code = """import java.util.UUID

        class SomeDataSource(val dao: SomeDao): Int {
            fun save(value: Entity) {
                return dao.save(value)
            }

            fun findById(id: UUID): Entity = dao.findById(id) ?: DEFAULT
        }
        """

        // when
        val findings = rule.compileAndLintWithContext(env, code)

        // then
        findings shouldHaveSize 0
    }
}
