package io.github.devngho.kuarkgrad.func

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.DecimalMode
import io.github.devngho.kuarkgrad.value.ValueWithGrad
import io.github.devngho.kuarkgrad.value.Parameter
import io.github.devngho.kuarkgrad.value.Variable

interface Func {
    context(mode: DecimalMode)
    fun forward(vararg args: BigDecimal): BigDecimal

    context(mode: DecimalMode)
    fun forward(vararg args: ValueWithGrad): ValueWithGrad

    fun derivative(of: Parameter, vararg args: Variable): Variable
    fun toString(vararg args: Variable): String = "${this::class.simpleName}(${args.joinToString(", ")})"
}