package com.github.ivy.explicit.rule

import io.gitlab.arturbosch.detekt.api.*
import org.jetbrains.kotlin.psi.KtNamedFunction

class NoImplicitFunctionReturnTypeRule(config: Config) : Rule(config) {

    override val issue = Issue(
        "NoImplicitFunctionReturnType",
        Severity.Warning,
        "Functions and class methods should declare their return types explicitly to improve code readability and maintainability.",
        Debt.FIVE_MINS
    )

    override fun visitNamedFunction(function: KtNamedFunction) {
        super.visitNamedFunction(function)
        // Check if the function has an explicit return type
        if (function.typeReference == null && !function.hasBlockBody() && !function.isLocal) {
            report(
                CodeSmell(
                    issue,
                    Entity.from(function),
                    "The function '${function.name}' should declare an explicit return type."
                )
            )
        }
    }
}

