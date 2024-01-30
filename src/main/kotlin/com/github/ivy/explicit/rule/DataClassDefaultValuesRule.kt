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
                "Default values lead to implicit creation of instances which leads to problems.",
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
        val className = klass.name
        append("Data class '$className' should not have default values. ")
        append("Found default value for property '${Message.parameter(parameter)}'. ")
        append("This allows for instances of '$className' to be created without explicitly specifying all properties, ")
        append("potentially leading to unintended or inconsistent states.")
    }
}
