package io.github.devngho.kuarkgrad.func

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.DecimalMode
import com.ionspin.kotlin.bignum.decimal.RoundingMode
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import io.github.devngho.kuarkgrad.util.simplify
import io.github.devngho.kuarkgrad.value.Const
import io.github.devngho.kuarkgrad.value.ValueWithGrad
import io.github.devngho.kuarkgrad.value.Graph
import io.github.devngho.kuarkgrad.value.Parameter
import io.github.devngho.kuarkgrad.value.Variable
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.pow

// precision used when none is given; division needs one, since results like 1 / 3 don't terminate
val defaultDecimalMode = DecimalMode(decimalPrecision = 30L, roundingMode = RoundingMode.ROUND_HALF_CEILING)

context(mode: DecimalMode)
private fun BigDecimal.safeDiv(other: BigDecimal): BigDecimal {
    if (other.isZero()) throw ArithmeticException("Division by zero")
    return divide(other, mode)
}

// converts a Double result back, rejecting NaN and infinity
private fun Double.toFiniteBigDecimal(name: String): BigDecimal {
    require(isFinite()) { "$name is not finite" }
    return toBigDecimal()
}

object Add: Func {
    context(mode: DecimalMode)
    override fun forward(vararg args: BigDecimal): BigDecimal {
        require(args.count() == 2)
        return args[0] + args[1]
    }

    context(mode: DecimalMode)
    override fun forward(vararg args: ValueWithGrad): ValueWithGrad {
        require(args.count() == 2)

        // (x + y)' = x' + y'
        return ValueWithGrad(args[0].value + args[1].value, args[0].derivative + args[1].derivative)
    }

    override fun derivative(of: Parameter, vararg args: Variable): Variable {
        require(args.count() == 2)

        // (x + y)' = x' + y'
        return Graph(listOf(args[0].derivative(of), args[1].derivative(of)), Add)
    }

    override fun toString(vararg args: Variable): String = "(${args[0]} + ${args[1]})"
}

operator fun Variable.plus(other: Variable): Variable {
    return Graph(listOf(this, other), Add)
}

object Subtract: Func {
    context(mode: DecimalMode)
    override fun forward(vararg args: BigDecimal): BigDecimal {
        require(args.count() == 2)
        return args[0] - args[1]
    }

    context(mode: DecimalMode)
    override fun forward(vararg args: ValueWithGrad): ValueWithGrad {
        require(args.count() == 2)

        // (x - y)' = x' - y'
        return ValueWithGrad(args[0].value - args[1].value, args[0].derivative - args[1].derivative)
    }

    override fun derivative(of: Parameter, vararg args: Variable): Variable {
        require(args.count() == 2)

        // (x - y)' = x' - y'
        return Graph(listOf(args[0].derivative(of), args[1].derivative(of)), Subtract)
    }

    override fun toString(vararg args: Variable): String = "(${args[0]} - ${args[1]})"
}

operator fun Variable.minus(other: Variable): Variable {
    return Graph(listOf(this, other), Subtract)
}

object Multiply: Func {
    context(mode: DecimalMode)
    override fun forward(vararg args: BigDecimal): BigDecimal {
        require(args.count() == 2)
        return args[0] * args[1]
    }

    context(mode: DecimalMode)
    override fun forward(vararg args: ValueWithGrad): ValueWithGrad {
        require(args.count() == 2)

        val (x, dx) = args[0]
        val (y, dy) = args[1]

        // (x * y)' = x' * y + x * y'
        return ValueWithGrad(x * y, dx * y + x * dy)
    }

    override fun derivative(of: Parameter, vararg args: Variable): Variable {
        require(args.count() == 2)

        // (x * y)' = x' * y + x * y'
        val left = Graph(listOf(args[0].derivative(of), args[1]), Multiply)
        val right = Graph(listOf(args[1].derivative(of), args[0]), Multiply)
        return Graph(listOf(left, right), Add)
    }

    override fun toString(vararg args: Variable): String = "(${args[0]} * ${args[1]})"
}

operator fun Variable.times(other: Variable): Variable {
    return Graph(listOf(this, other), Multiply)
}

object Divide: Func {
    context(mode: DecimalMode)
    override fun forward(vararg args: BigDecimal): BigDecimal {
        require(args.count() == 2)
        return args[0].safeDiv(args[1])
    }

    context(mode: DecimalMode)
    override fun forward(vararg args: ValueWithGrad): ValueWithGrad {
        require(args.count() == 2)

        val (x, dx) = args[0]
        val (y, dy) = args[1]

        // (x / y)' = (x' * y - x * y') / y^2
        return ValueWithGrad(x.safeDiv(y), (dx * y - x * dy).safeDiv(y * y))
    }

    override fun derivative(of: Parameter, vararg args: Variable): Variable {
        require(args.count() == 2)

        // (x / y)' = (x' * y - x * y') / y^2
        val left = Graph(listOf(args[0].derivative(of), args[1]), Multiply)
        val right = Graph(listOf(args[1].derivative(of), args[0]), Multiply)
        val numerator = Graph(listOf(left, right), Subtract)
        val denominator = Graph(listOf(args[1], Const(BigDecimal.fromInt(2))), PowerOf)
        return Graph(listOf(numerator, denominator), Divide)
    }

    override fun toString(vararg args: Variable): String = "(${args[0]} / ${args[1]})"
}

operator fun Variable.div(other: Variable): Variable {
    return Graph(listOf(this, other), Divide)
}

object PowerOf: Func {
    context(mode: DecimalMode)
    override fun forward(vararg args: BigDecimal): BigDecimal {
        require(args.count() == 2)

        if (args[1].isWholeNumber()) {
            require(args[1].abs() <= Int.MAX_VALUE.toBigDecimal()) { "Exponent ${args[1]} is too large" }
            val n = args[1].intValue(true)

            // x^-n = 1 / x^n
            return if (n < 0) BigDecimal.ONE.safeDiv(args[0].pow(-n)) else args[0].pow(n)
        }

        // use the standard library's pow function for non-integer exponents
        // todo: more accurate implementation of pow for non-integer exponents
        return args[0].doubleValue(false).pow(args[1].doubleValue(false)).toFiniteBigDecimal("${args[0]}^${args[1]}")
    }

    context(mode: DecimalMode)
    override fun forward(vararg args: ValueWithGrad): ValueWithGrad {
        require(args.count() == 2)

        val (x, dx) = args[0]
        val (n, dn) = args[1]

        // the power rule below only holds for a constant exponent
        require(dn.isZero()) { "exponent must be constant with respect to the differentiated parameter" }

        // (x^n)' = n * x^(n-1) * x'
        return ValueWithGrad(forward(x, n), n * forward(x, n - BigDecimal.ONE) * dx)
    }

    override fun derivative(of: Parameter, vararg args: Variable): Variable {
        require(args.count() == 2)

        // the power rule below only holds for a constant exponent
        require(args[1].derivative(of).simplify().let { it is Const && it.value.isZero() }) {
            "exponent must be constant with respect to ${of.name}"
        }

        // (x^n)' = n * x^(n-1) * x'
        val nMinusOne = Graph(listOf(args[1], Const(BigDecimal.fromInt(1))), Subtract)
        val xToNMinusOne = Graph(listOf(args[0], nMinusOne), PowerOf)
        val nTimesXToNMinusOne = Graph(listOf(args[1], xToNMinusOne), Multiply)
        return Graph(listOf(nTimesXToNMinusOne, args[0].derivative(of)), Multiply)
    }

    override fun toString(vararg args: Variable): String = "(${args[0]}^${args[1]})"
}

fun Variable.pow(exponent: Variable): Variable {
    return Graph(listOf(this, exponent), PowerOf)
}

object Exp: Func {
    context(mode: DecimalMode)
    override fun forward(vararg args: BigDecimal): BigDecimal {
        require(args.count() == 1)

        // bignum does not have a built-in exp function, so we use the standard library's exp function and convert to BigDecimal
        // todo: more accurate implementation of exp
        return exp(args[0].doubleValue(false)).toFiniteBigDecimal("exp(${args[0]})")
    }

    context(mode: DecimalMode)
    override fun forward(vararg args: ValueWithGrad): ValueWithGrad {
        require(args.count() == 1)

        val expX = forward(args[0].value)

        // (e^x)' = e^x * x'
        return ValueWithGrad(expX, expX * args[0].derivative)
    }

    override fun derivative(of: Parameter, vararg args: Variable): Variable {
        require(args.count() == 1)

        // (e^x)' = e^x * x'
        val expX = Graph(listOf(args[0]), Exp)
        return Graph(listOf(expX, args[0].derivative(of)), Multiply)
    }

    override fun toString(vararg args: Variable): String = "exp(${args[0]})"
}

fun exp(variable: Variable): Variable {
    return Graph(listOf(variable), Exp)
}

object Log: Func {
    context(mode: DecimalMode)
    override fun forward(vararg args: BigDecimal): BigDecimal {
        require(args.count() == 1)

        // bignum does not have a built-in log function, so we use the standard library's log function and convert to BigDecimal
        require(args[0] > BigDecimal.ZERO) { "log(${args[0]}) is undefined" }
        return ln(args[0].doubleValue(false)).toBigDecimal()
    }

    context(mode: DecimalMode)
    override fun forward(vararg args: ValueWithGrad): ValueWithGrad {
        require(args.count() == 1)

        val (x, dx) = args[0]

        // (ln(x))' = 1/x * x'
        return ValueWithGrad(forward(x), dx.safeDiv(x))
    }

    override fun derivative(of: Parameter, vararg args: Variable): Variable {
        require(args.count() == 1)

        // (ln(x))' = 1/x * x'
        val oneOverX = Graph(listOf(Const(BigDecimal.fromInt(1)), args[0]), Divide)
        return Graph(listOf(oneOverX, args[0].derivative(of)), Multiply)
    }

    override fun toString(vararg args: Variable): String = "log(${args[0]})"
}

fun log(variable: Variable): Variable {
    return Graph(listOf(variable), Log)
}