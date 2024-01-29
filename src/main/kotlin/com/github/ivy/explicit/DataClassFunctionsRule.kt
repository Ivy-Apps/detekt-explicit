package com.github.ivy.explicit

import io.gitlab.arturbosch.detekt.api.*
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtNamedFunction

class DataClassFunctionsRule(config: Config) : Rule(config) {
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
                ?.forEach { function ->
                    report(
                        CodeSmell(
                            issue,
                            Entity.from(function),
                            message = "Data class '${klass.name}' should not contain functions. " +
                                    "Found: function ${function.name}()."
                        )
                    )
                }
        }
    }
}
