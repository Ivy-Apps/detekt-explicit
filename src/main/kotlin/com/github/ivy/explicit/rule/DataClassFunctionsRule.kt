package com.github.ivy.explicit.rule

import com.github.ivy.explicit.util.FunctionMessage
import io.gitlab.arturbosch.detekt.api.*
import io.gitlab.arturbosch.detekt.rules.isOverride
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtNamedFunction

class DataClassFunctionsRule(config: Config) : Rule(config) {
    private val functionMessage: FunctionMessage by lazy { FunctionMessage() }

    override val issue = Issue(
        id = "DataClassFunctions",
        severity = Severity.Maintainability,
        description = "Data classes should not define behavior. " +
                "Their purpose is to model data.",
        debt = Debt.TWENTY_MINS,
    )

    override fun visitClass(klass: KtClass) {
        super.visitClass(klass)
        if (klass.isData()) {
            klass.body?.declarations
                ?.filterIsInstance<KtNamedFunction>()
                ?.filter {
                    // Functions overrides are fine
                    !it.isOverride()
                }
                ?.forEach { function ->
                    report(
                        CodeSmell(
                            issue,
                            Entity.from(function),
                            message = failureMessage(klass, function)
                        )
                    )
                }
        }
    }

    private fun failureMessage(
        klass: KtClass,
        function: KtNamedFunction
    ): String = buildString {
        append("Data class '${klass.name}' should not contain functions. ")
        append("Data classes should only model data and not define behavior. ")
        append("Found: function '${functionMessage.signature(function)}'.")
    }
}
