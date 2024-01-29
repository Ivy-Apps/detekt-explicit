package com.github.ivy.explicit.rule

import com.github.ivy.explicit.util.Message
import io.gitlab.arturbosch.detekt.api.*
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtParameter

class DataClassDefaultValuesRule(config: Config) : Rule(config) {

    override val issue = Issue(
        id = "DataClassDefaultValues",
        severity = Severity.Maintainability,
        description = "Data class properties should not have default values. " +
                "Default values lead to implicit instance constructions and problems.",
        debt = Debt.TWENTY_MINS,
    )

    override fun visitClass(klass: KtClass) {
        super.visitClass(klass)
        if (klass.isData()) {
            klass.primaryConstructorParameters.filter {
                it.hasDefaultValue()
            }.forEach { parameter ->
                report(
                    CodeSmell(
                        issue = issue,
                        entity = Entity.from(parameter),
                        message = failureMessage(klass, parameter)
                    )
                )
            }
        }
    }

    private fun failureMessage(klass: KtClass, parameter: KtParameter): String = buildString {
        append("Data class '${klass.name}' should not have default values for properties. ")
        append("Found default value for property '${Message.parameter(parameter)}'. ")
        append("This can lead to implicit instance constructions and problems.")
    }
}
