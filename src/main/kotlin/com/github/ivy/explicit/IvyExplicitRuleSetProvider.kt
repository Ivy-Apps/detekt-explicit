package com.github.ivy.explicit

import com.github.ivy.explicit.rule.DataClassDefaultValuesRule
import com.github.ivy.explicit.rule.DataClassFunctionsRule
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
                DataClassDefaultValuesRule(config),
            ),
        )
    }
}
