package com.github.ivy.explicit.rule

import io.gitlab.arturbosch.detekt.api.*
import io.gitlab.arturbosch.detekt.rules.isOverride
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtParameter

class DataClassTypedIDsRule(config: Config) : Rule(config) {
    companion object {
        private val ExcludedClassNameEndings by lazy {
            setOf("Dto", "Entity")
        }

        private val ExcludedAnnotations by lazy {
            setOf("Entity", "Serializable")
        }

        private val IdFieldEndings by lazy {
            setOf("Id", "ID")
        }
    }

    override val issue = Issue(
        id = "DataClassTypedIDs",
        severity = Severity.Maintainability,
        description = "Domain data models should use type-safe `value class` ids. " +
                "Typed-IDs provide compile-time safety and prevent mixing IDs of different entities.",
        debt = Debt.TWENTY_MINS
    )

    override fun visitClass(klass: KtClass) {
        super.visitClass(klass)
        if (klass.isData() && !klass.isIgnoredClass()) {
            klass.getPrimaryConstructorParameterList()
                ?.parameters
                ?.filter { param ->
                    !param.isOverride() && param.seemsLikeID()
                }
                ?.forEach { parameter ->
                    report(
                        CodeSmell(
                            issue,
                            Entity.from(parameter),
                            message = failureMessage(klass, parameter)
                        )
                    )
                }
        }
    }

    private fun KtClass.isIgnoredClass(): Boolean {
        name?.let { klasName ->
            val isIgnored = ExcludedClassNameEndings.any {
                klasName.endsWith(it, ignoreCase = true)
            }
            if (isIgnored) return true
        }

        return annotationEntries.any {
            val annotationName = it.shortName?.asString()
            annotationName in ExcludedAnnotations
        }
    }

    private fun KtParameter.seemsLikeID(): Boolean {
        val paramType = typeReference?.text
        if (paramType == "UUID") return true

        name?.let { paramName ->
            val endsLikeID = IdFieldEndings.any {
                paramName.endsWith(it, ignoreCase = false)
            }
            if (endsLikeID) return true
        }

        return false
    }

    private fun failureMessage(klass: KtClass, parameter: KtParameter) = buildString {
        val paramType = parameter.typeReference?.text
        append("Data class '${klass.name}' should use type-safe IDs ")
        append("instead of $paramType for property '${parameter.name}'. ")
        append("Typed-IDs like `value class SomeId(val id: UUID)` provide ")
        append("compile-time safety and prevent mixing IDs of different entities.")
    }
}
