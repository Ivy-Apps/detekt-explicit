package com.github.ivy.explicit.rule

import com.github.ivy.explicit.util.Message
import io.gitlab.arturbosch.detekt.api.*
import io.gitlab.arturbosch.detekt.rules.isOverride
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtNamedFunction

class DataClassFunctionsRule(config: Config) : Rule(config) {

    override val issue = Issue(
        id = "DataClassFunctions",
        severity = Severity.Maintainability,
        description = "Data classes should not be tied to any behavior. " +
                "Their responsibility is to solely model data.",
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
        append("Found: function '${Message.functionSignature(function)}'. ")
        append("Data classes should only model data and should not be tied to any behavior.")
    }
}
