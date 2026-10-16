package io.github.devngho.kuarkgrad

import com.ionspin.kotlin.bignum.decimal.DecimalMode
import com.ionspin.kotlin.bignum.decimal.RoundingMode
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import io.github.devngho.kuarkgrad.func.div
import io.github.devngho.kuarkgrad.func.exp
import io.github.devngho.kuarkgrad.func.log
import io.github.devngho.kuarkgrad.func.pow
import io.github.devngho.kuarkgrad.util.simplify
import io.github.devngho.kuarkgrad.value.Parameter
import io.kotest.assertions.throwables.shouldThrow
import io.github.devngho.kuarkgrad.value.Const
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.comparables.shouldBeAtMost
import io.kotest.matchers.shouldBe

class ArithmeticTest: FreeSpec({
    val x = Parameter("x")

    val px = Parameter("x")
    val three = mapOf(px to 3.0.toBigDecimal())
    val oneThird = (1.0 / 3.0).toBigDecimal()
    val tolerance = 1e-9.toBigDecimal()

    "Arithmetic exceptions" - {
        "1 / 3" {
            val graph = Const(1.0) / Const(3.0)

            // unlimited precision can't represent 1 / 3
            shouldThrow<ArithmeticException> { graph.evaluate(emptyMap(), DecimalMode.DEFAULT) }

            (graph.evaluate(emptyMap()) - oneThird).abs() shouldBeAtMost tolerance
        }
    }

    "Non-terminating division" - {
        "x / 3 at x = 1" {
            val f = x / Const(3.0)

            (f.evaluate(mapOf(px to 1.0.toBigDecimal())) - oneThird).abs() shouldBeAtMost tolerance
        }

        "x^-1 at x = 3" {
            val f = x.pow(Const(-1.0))

            (f.evaluate(three) - oneThird).abs() shouldBeAtMost tolerance
        }

        "log(x)' at x = 3" {
            val f = log(x)

            (f.derivative(px).simplify().evaluate(three) - oneThird).abs() shouldBeAtMost tolerance
            (f.evaluateWithDerivative(px, three).derivative - oneThird).abs() shouldBeAtMost tolerance
        }
    }

    "Precision can be chosen per call" {
        val f = Const(1.0) / Const(3.0)

        f.evaluate(emptyMap(), DecimalMode(decimalPrecision = 3L, roundingMode = RoundingMode.ROUND_HALF_CEILING)) shouldBe "0.333".toBigDecimal()
        f.simplify(DecimalMode(decimalPrecision = 3L, roundingMode = RoundingMode.ROUND_HALF_CEILING)) shouldBe Const(0.333)
    }

    "Invalid inputs" - {
        fun at(v: Double) = mapOf(px to v.toBigDecimal())

        "log(-1)" {
            shouldThrow<IllegalArgumentException> { (log(x)).evaluate(at(-1.0)) }
        }

        "exp(1000)" {
            shouldThrow<IllegalArgumentException> { (exp(x)).evaluate(at(1000.0)) }
        }

        "(-8)^0.5" {
            shouldThrow<IllegalArgumentException> { (x.pow(Const(0.5))).evaluate(at(-8.0)) }
        }

        "x^1e10" {
            shouldThrow<IllegalArgumentException> { (x.pow(Const(1e10))).evaluate(at(1.0)) }
        }

        "0^-1" {
            shouldThrow<ArithmeticException> { (x.pow(Const(-1.0))).evaluate(at(0.0)) }
        }
    }

    "Constants are exact" {
        val c = Const(12345678901.0)

        c.evaluate(emptyMap()) shouldBeAtMost 12345678901.0.toBigDecimal()
        12345678901.0.toBigDecimal() shouldBeAtMost c.evaluate(emptyMap())
    }
})
