package io.github.devngho.kuarkgrad.value

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import com.ionspin.kotlin.bignum.decimal.DecimalMode

data class Const(val value: BigDecimal): Variable {
    constructor(value: Number) : this(value.asBigDecimal())

    override fun derivative(of: Parameter): Variable = Const(BigDecimal.ZERO)

    override fun evaluate(context: Map<Parameter, BigDecimal>, mode: DecimalMode): BigDecimal = value

    override fun evaluateWithDerivative(of: Parameter, context: Map<Parameter, BigDecimal>, mode: DecimalMode): ValueWithGrad = ValueWithGrad(value, BigDecimal.ZERO)

    override fun toString(): String = value.toStringExpanded()
}

internal fun Number.asBigDecimal(): BigDecimal = when (this) {
    is Int -> toBigDecimal()
    is Long -> toBigDecimal()
    is Float -> toBigDecimal()
    is Double -> toBigDecimal()
    else -> toDouble().toBigDecimal()
}
