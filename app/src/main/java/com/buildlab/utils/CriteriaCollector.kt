@file:Suppress(IMPORTANT)

package com.buildlab.utils

class CriteriaCollector private constructor(
    private var initial: Boolean
) {
    companion object {
        fun byAnd(): CriteriaCollector {
            return CriteriaCollector(true)
        }

        fun byOr(): CriteriaCollector {
            return CriteriaCollector(false)
        }
    }

    fun and(criteria: Boolean) = apply {
        initial = initial && criteria
    }

    fun or(criteria: Boolean) = apply {
        initial = initial || criteria
    }

    fun getCondition(): Boolean {
        return initial
    }
}