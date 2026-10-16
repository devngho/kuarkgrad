package io.github.devngho.kuarkgrad

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import io.github.devngho.kuarkgrad.func.*
import io.github.devngho.kuarkgrad.value.ValueWithGrad
import io.github.devngho.kuarkgrad.value.Parameter
import io.kotest.assertions.throwables.shouldThrow
import io.github.devngho.kuarkgrad.value.Const
import io.kotest.core.spec.style.FreeSpec
import io.kotest.datatest.withData
import io.kotest.matchers.comparables.shouldBeAtMost
import io.kotest.matchers.shouldBe

class ForwardModeTest: FreeSpec({
    val x = Parameter("x")
    val y = Parameter("y")

    val px = Parameter("x")
    val py = Parameter("y")

    "derivative of 2x^2 + 3x + 1 at x = 2" {
        val f = Const(2.0) * x.pow(Const(2.0)) + Const(3.0) * x + Const(1.0)

        val (value, derivative) = f.evaluateWithDerivative(px, mapOf(px to 2.0.toBigDecimal()))

        value shouldBe 15.0.toBigDecimal()
        derivative shouldBe 11.0.toBigDecimal()
    }

    "partial derivatives of x * y" {
        val f = x * y
        val context = mapOf(px to 3.0.toBigDecimal(), py to 5.0.toBigDecimal())

        f.evaluateWithDerivative(px, context) shouldBe ValueWithGrad(15.0.toBigDecimal(), 5.0.toBigDecimal())
        f.evaluateWithDerivative(py, context) shouldBe ValueWithGrad(15.0.toBigDecimal(), 3.0.toBigDecimal())
    }

    "pow rejects an exponent that depends on the differentiated parameter" {
        val f = x.pow(x)

        shouldThrow<IllegalArgumentException> { f.evaluateWithDerivative(px, mapOf(px to 2.0.toBigDecimal())) }
        shouldThrow<IllegalArgumentException> { f.derivative(px) }
    }

    "pow accepts an exponent that is constant with respect to the differentiated parameter" {
        val f = x.pow(y)
        val context = mapOf(px to 2.0.toBigDecimal(), py to 3.0.toBigDecimal())

        f.evaluateWithDerivative(px, context) shouldBe ValueWithGrad(8.0.toBigDecimal(), 12.0.toBigDecimal())
        f.derivative(px).evaluate(context) shouldBe 12.0.toBigDecimal()
    }

    // forward mode should agree with evaluating the symbolic derivative
    withData(
        nameFn = { (name, _) -> "matches symbolic derivative: $name" },
        listOf(
            "x - x^3" to x - x.pow(Const(3.0)),
            "x / (x + 1)" to x / (x + Const(1.0)),
            "exp(2x)" to exp(Const(2.0) * x),
            "log(x^2)" to log(x.pow(Const(2.0))),
            "x * exp(x) * log(x)" to x * exp(x) * log(x),
        )
    ) { (_, f) ->
        listOf(0.25, 1.0, 4.0).forEach {
            val context = mapOf(px to it.toBigDecimal())

            val (value, derivative) = f.evaluateWithDerivative(px, context)

            (value - f.evaluate(context)).abs() shouldBeAtMost BigDecimal.parseString("1e-9")
            (derivative - f.derivative(px).evaluate(context)).abs() shouldBeAtMost BigDecimal.parseString("1e-9")
        }
    }
})
