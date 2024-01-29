package com.github.ivy.explicit

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.RuleSet
import io.gitlab.arturbosch.detekt.api.RuleSetProvider

class IvyExplicitRuleSetProvider : RuleSetProvider {
    override val ruleSetId: String = "IvyExplicit"

    override fun instance(config: Config): RuleSet {
        return RuleSet(
            ruleSetId,
            listOf(
                DataClassFunctionsRule(config),
            ),
        )
    }
}
