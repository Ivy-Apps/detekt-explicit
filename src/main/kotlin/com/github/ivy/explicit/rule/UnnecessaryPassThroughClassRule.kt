package com.github.ivy.explicit.rule

import io.gitlab.arturbosch.detekt.api.*
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.isPrivate

class UnnecessaryPassThroughClassRule(config: Config) : Rule(config) {

    override val issue = Issue(
        id = "UnnecessaryPassThroughClass",
        severity = Severity.Maintainability,
        description = "Unnecessary pass-through classes increase complexity " +
                "and boilerplate code without adding value.",
        debt = Debt.TEN_MINS
    )

    override fun visitClass(klass: KtClass) {
        super.visitClass(klass)
        if (klass.isInterface() ||
            klass.isEnum() ||
            klass.isAnnotation() ||
            klass.isData()
        ) return

        val functions = klass.body?.functions?.filterNot { it.isPrivate() } ?: return
        if (functions.isEmpty()) return

        val passThroughClass = functions.all(::isPassThroughFunction)
        if (passThroughClass) {
            report(CodeSmell(issue, Entity.from(klass), failureMessage(klass)))
        }
    }

    private fun isPassThroughFunction(function: KtNamedFunction): Boolean {
        val callExpression = when (val body = function.bodyExpression) {
            is KtBlockExpression -> {
                if (body.statements.size == 1) {
                    extractCallExpression(body.statements.first())
                } else {
                    null
                }
            }

            is KtCallExpression,
            is KtReturnExpression,
            is KtDotQualifiedExpression -> extractCallExpression(body)

            else -> null
        }

        return callExpression?.let { callExp ->
            val callArguments = callExp.valueArguments.mapNotNull { it.getArgumentExpression()?.text }
            val functionParameters = function.valueParameters.mapNotNull { it.name }
            callArguments == functionParameters
        } ?: false
    }

    private fun extractCallExpression(expression: KtExpression): KtCallExpression? = when (expression) {
        is KtCallExpression -> expression
        is KtReturnExpression -> expression.returnedExpression?.let(::extractCallExpression)
        is KtDotQualifiedExpression -> expression.selectorExpression?.let(::extractCallExpression)
        else -> null
    }


    private fun failureMessage(klass: KtClass): String = buildString {
        append("The class '${klass.name}' appears to be an unnecessary pass-through class. ")
        append("It only increase complexity and boilerplate code without adding any value. ")
        append("Consider removing the class or adding meaningful logic.")
    }
}
