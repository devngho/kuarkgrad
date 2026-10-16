package io.github.devngho.kuarkgrad.func

import io.github.devngho.kuarkgrad.value.Const
import io.github.devngho.kuarkgrad.value.Variable

// lets plain numbers be used as constants, e.g. 2 * x.pow(2) + 1

operator fun Variable.plus(other: Number): Variable = this + Const(other)
operator fun Number.plus(other: Variable): Variable = Const(this) + other

operator fun Variable.minus(other: Number): Variable = this - Const(other)
operator fun Number.minus(other: Variable): Variable = Const(this) - other

operator fun Variable.times(other: Number): Variable = this * Const(other)
operator fun Number.times(other: Variable): Variable = Const(this) * other

operator fun Variable.div(other: Number): Variable = this / Const(other)
operator fun Number.div(other: Variable): Variable = Const(this) / other

operator fun Variable.unaryMinus(): Variable = Const(-1) * this

fun Variable.pow(exponent: Number): Variable = pow(Const(exponent))
