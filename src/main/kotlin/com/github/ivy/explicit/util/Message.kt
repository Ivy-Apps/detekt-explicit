package com.github.ivy.explicit.util

import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtParameter

object Message {
    fun functionSignature(function: KtNamedFunction): String {
        // Extract the function name
        val fName = function.name ?: ""

        // Build the parameter list string
        val inputParams = function.valueParameters.joinToString(separator = ", ") { param ->
            "${param.name}: ${param.typeReference?.text}"
        }

        // Get the return type, considering both explicit and inferred types
        val returnType = try {
            when {
                function.typeReference != null -> function.typeReference!!.text
                function.hasBlockBody() -> "Unit" // Assume Unit for block bodies without a return type
                function.hasDeclaredReturnType() -> function.typeReference!!.text // Explicit return type
                else -> null
            }
        } catch (e: Exception) {
            null
        }
        // Construct and return the formatted string
        return "$fName($inputParams)" + if (returnType != null) {
            ": $returnType"
        } else ""
    }

    fun parameter(param: KtParameter): String = buildString {
        append(param.name)
        val type = param.typeReference?.text
        if (type != null) {
            append(": ")
            append(type)
            param.defaultValue?.text?.let { append(" = $it") }
        }
    }
}
