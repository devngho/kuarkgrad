package io.github.devngho.kuarkgrad.value

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.DecimalMode
import io.github.devngho.kuarkgrad.func.defaultDecimalMode

interface Variable {
    fun derivative(of: Parameter): Variable

    fun evaluate(context: Map<Parameter, BigDecimal>, mode: DecimalMode = defaultDecimalMode): BigDecimal

    fun evaluateWithDerivative(of: Parameter, context: Map<Parameter, BigDecimal>, mode: DecimalMode = defaultDecimalMode): ValueWithGrad
}

// shorthand for evaluate(mapOf(x to 2.toBigDecimal())), e.g. f.evaluate(x to 2)
fun Variable.evaluate(vararg values: Pair<Parameter, Number>, mode: DecimalMode = defaultDecimalMode): BigDecimal =
    evaluate(values.associate { (p, v) -> p to v.asBigDecimal() }, mode)

fun Variable.evaluateWithDerivative(of: Parameter, vararg values: Pair<Parameter, Number>, mode: DecimalMode = defaultDecimalMode): ValueWithGrad =
    evaluateWithDerivative(of, values.associate { (p, v) -> p to v.asBigDecimal() }, mode)
