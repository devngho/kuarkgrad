package io.github.devngho.kuarkgrad.util

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.DecimalMode
import io.github.devngho.kuarkgrad.func.Add
import io.github.devngho.kuarkgrad.func.Divide
import io.github.devngho.kuarkgrad.func.Exp
import io.github.devngho.kuarkgrad.func.Func
import io.github.devngho.kuarkgrad.func.Log
import io.github.devngho.kuarkgrad.func.Multiply
import io.github.devngho.kuarkgrad.func.PowerOf
import io.github.devngho.kuarkgrad.func.Subtract
import io.github.devngho.kuarkgrad.func.defaultDecimalMode
import io.github.devngho.kuarkgrad.value.Const
import io.github.devngho.kuarkgrad.value.Graph
import io.github.devngho.kuarkgrad.value.Variable

// mode is the precision used when folding constants
fun Variable.simplify(mode: DecimalMode = defaultDecimalMode): Variable =
    if (this is Graph) with(mode) { simplifyNode(op, nodes.map { it.simplify(mode) }) } else this

private fun Variable?.isZero() = this is Const && value.isZero()
private fun Variable?.isOne() = this is Const && value == BigDecimal.ONE

context(mode: DecimalMode)
private fun simplifyNode(op: Func, nodes: List<Variable>): Variable {
    // if all nodes are Const, fold them into a single Const (exp and log are kept symbolic)
    if (op !is Exp && op !is Log && nodes.all { it is Const }) {
        return Const(op.forward(*nodes.map { (it as Const).value }.toTypedArray()))
    }

    val a = nodes[0]
    val b = nodes.getOrNull(1)

    return when (op) {
        is Add -> when {
            // 0 + x = x, x + 0 = x
            a.isZero() -> b!!
            b.isZero() -> a
            // c + x = x + c, so constants always end up on the right
            a is Const -> simplifyNode(Add, listOf(b!!, a))
            // (x + c1) + c2 = x + (c1 + c2)
            b is Const && a is Graph && a.op is Add && a.nodes[1] is Const ->
                simplifyNode(Add, listOf(a.nodes[0], Const((a.nodes[1] as Const).value + b.value)))
            else -> Graph(nodes, op)
        }

        is Subtract -> when {
            // x - 0 = x
            b.isZero() -> a
            // x - x = 0
            a == b -> Const(BigDecimal.ZERO)
            else -> Graph(nodes, op)
        }

        is Multiply -> when {
            // 0 * x = 0, x * 0 = 0
            a.isZero() || b.isZero() -> Const(BigDecimal.ZERO)
            // 1 * x = x, x * 1 = x
            a.isOne() -> b!!
            b.isOne() -> a
            // x * c = c * x, so constants always end up on the left
            b is Const -> simplifyNode(Multiply, listOf(b, a))
            // c1 * (c2 * x) = (c1 * c2) * x
            a is Const && b is Graph && b.op is Multiply && b.nodes[0] is Const ->
                simplifyNode(Multiply, listOf(Const(a.value * (b.nodes[0] as Const).value), b.nodes[1]))
            else -> Graph(nodes, op)
        }

        is Divide -> when {
            // 0 / x = 0
            a.isZero() -> Const(BigDecimal.ZERO)
            b.isZero() -> throw ArithmeticException("Division by zero")
            // x / 1 = x
            b.isOne() -> a
            else -> Graph(nodes, op)
        }

        is PowerOf -> when {
            // x^0 = 1
            b.isZero() -> Const(BigDecimal.ONE)
            // 0^x = 0 (for x > 0)
            a.isZero() && b is Const && b.value > BigDecimal.ZERO -> Const(BigDecimal.ZERO)
            // x^1 = x
            b.isOne() -> a
            else -> Graph(nodes, op)
        }

        else -> Graph(nodes, op)
    }
}
