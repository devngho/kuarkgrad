package io.github.devngho.kuarkgrad

import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import io.github.devngho.kuarkgrad.func.exp
import io.github.devngho.kuarkgrad.func.plus
import io.github.devngho.kuarkgrad.func.pow
import io.github.devngho.kuarkgrad.func.times
import io.github.devngho.kuarkgrad.util.simplify
import io.github.devngho.kuarkgrad.value.Const
import io.github.devngho.kuarkgrad.value.Parameter
import io.kotest.core.spec.style.FreeSpec
import io.kotest.datatest.withData
import io.kotest.matchers.comparables.shouldBeAtMost
import io.kotest.matchers.shouldBe

class SecondDerivativeTest: FreeSpec({
    val x = Parameter("x")

    withData(
        nameFn = { (a, expected, input) -> "($a)' = ${expected.second}; f''(${input.first}) = ${input.second}" },
        listOf(
            Triple(
                // (2x^3+3x^2+4)'' = (6x^2+6x)' = 12x + 6
                Const(2.0) * x.pow(Const(3.0)) + Const(3.0) * x.pow(Const(2.0)) + Const(4.0),
                "x" to Const(12.0) * x + Const(6.0),
                1.0 to 18.0
            ),
            Triple(
                // (e^x + 3x^2)' = (e^x + 6x)' = e^x + 6
                exp(x) + Const(3.0) * x.pow(Const(2.0)),
                "x" to exp(x) + Const(6.0),
                1.0 to kotlin.math.exp(1.0) + 6.0
            ),
        )
    ) { (a, p, inp) ->
        val (of, expected) = p
        val (input, expectedValue) = inp

        val result = a.derivative(Parameter(of)).simplify().derivative(Parameter(of)).simplify()

        result shouldBe expected

        val forward = result.evaluate(mapOf(Parameter(of) to input.toBigDecimal()))

        (forward - expectedValue.toBigDecimal()).abs() shouldBeAtMost 1e-10.toBigDecimal()
    }
})