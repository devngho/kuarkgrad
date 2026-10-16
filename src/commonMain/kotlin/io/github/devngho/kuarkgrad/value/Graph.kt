package io.github.devngho.kuarkgrad.value

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.DecimalMode
import io.github.devngho.kuarkgrad.func.Func

data class Graph(
    val nodes: List<Variable>,
    val op: Func
): Variable {
    override fun derivative(of: Parameter): Variable = op.derivative(of, *nodes.toTypedArray())

    override fun evaluate(context: Map<Parameter, BigDecimal>, mode: DecimalMode): BigDecimal {
        val evaluatedNodes = nodes.map { it.evaluate(context, mode) }

        return with(mode) { op.forward(*evaluatedNodes.toTypedArray()) }
    }

    override fun evaluateWithDerivative(of: Parameter, context: Map<Parameter, BigDecimal>, mode: DecimalMode): ValueWithGrad {
        val evaluatedNodes = nodes.map { it.evaluateWithDerivative(of, context, mode) }

        return with(mode) { op.forward(*evaluatedNodes.toTypedArray()) }
    }

    override fun toString(): String = op.toString(*nodes.toTypedArray())
}