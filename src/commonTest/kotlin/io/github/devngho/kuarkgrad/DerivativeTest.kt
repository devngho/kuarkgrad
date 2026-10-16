package io.github.devngho.kuarkgrad

import io.github.devngho.kuarkgrad.func.*
import io.github.devngho.kuarkgrad.util.simplify
import io.github.devngho.kuarkgrad.value.Parameter
import io.github.devngho.kuarkgrad.value.Const
import io.kotest.core.spec.style.FreeSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe

class DerivativeTest: FreeSpec({
    val x = Parameter("x")
    val y = Parameter("y")

    withData(
        nameFn = { (a, b, expected) -> "$a + $b = $expected" },
        listOf(
            Triple(
                Const(1.0),
                Const(2.0),
                "x" to Const(0.0) + Const(0.0)
            ),
            Triple(
                x,
                Const(1.0),
                "x" to Const(1.0) + Const(0.0)
            ),
            Triple(
                x,
                y,
                "x" to Const(1.0) + Const(0.0)
            )
        )
    ) { (a, b, p) ->
        val (of, expected) = p

        val result = Add.derivative(Parameter(of), a, b)

        result shouldBe expected
    }

    withData(
        nameFn = { (a, b, expected) -> "$a * $b = $expected" },
        listOf(
            Triple(
                Const(1.0),
                Const(2.0),
                "x" to Const(0.0) * Const(2.0) + Const(0.0) * Const(1.0)
            ),
            Triple(
                x,
                Const(1.0),
                "x" to Const(1.0) * Const(1.0) + Const(0.0) * x
            ),
            Triple(
                x,
                y,
                "x" to Const(1.0) * y + Const(0.0) * x
            )
        )
    ) { (a, b, p) ->
        val (of, expected) = p

        val result = Multiply.derivative(Parameter(of), a, b)

        result shouldBe expected
    }

    withData(
        nameFn = { (a, b, expected) -> "$a ^ $b = $expected" },
        listOf(
            Triple(
                Const(2.0),
                Const(3.0),
                "x" to Const(3.0) * (Const(2.0).pow(Const(3.0) - Const(1.0))) * Const(0.0)
            ),
            Triple(
                x,
                Const(3.0),
                "x" to Const(3.0) * (x.pow(Const(3.0) - Const(1.0))) * Const(1.0)
            ),
            Triple(
                x,
                y,
                "x" to y * (x.pow(y - Const(1.0))) * Const(1.0)
            )
        )
    ) { (a, b, p) ->
        val (of, expected) = p

        val result = PowerOf.derivative(Parameter(of), a, b)

        result shouldBe expected
    }

    withData(
        nameFn = { (a, b, expected) -> "$a / $b = $expected" },
        listOf(
            Triple(
                Const(2.0),
                Const(3.0),
                "x" to (Const(0.0) * Const(3.0) - Const(0.0) * Const(2.0)) / (Const(3.0).pow(Const(2.0)))
            ),
            Triple(
                x,
                Const(3.0),
                "x" to (Const(1.0) * Const(3.0) - Const(0.0) * x) / (Const(3.0).pow(Const(2.0)))
            ),
            Triple(
                x,
                y,
                "x" to (Const(1.0) * y - Const(0.0) * x) / (y.pow(Const(2.0)))
            )
        )
    ) { (a, b, p) ->
        val (of, expected) = p

        val result = Divide.derivative(Parameter(of), a, b)

        result shouldBe expected
    }

    // exp
    withData(
        nameFn = { (a, expected) -> "exp($a) = $expected" },
        listOf(
            Pair(
                Const(2.0),
                "x" to exp(Const(2.0)) * Const(0.0)
            ),
            Pair(
                x,
                "x" to exp(x) * Const(1.0)
            ),
            Pair(
                Const(2.0) * x,
                "x" to // (e^(2x))' = e^(2x) * (2x)' = e^(2x) * (0*x + 1*2)
                    exp(Const(2.0) * x) * (Const(0.0) * x + Const(1.0) * Const(2.0))
            )
        )
    ) { (a, p) ->
        val (of, expected) = p

        val result = Exp.derivative(Parameter(of), a)

        result shouldBe expected
    }

    withData(
        nameFn = { (a, expected) -> "ln($a) = $expected" },
        listOf(
            Pair(
                Const(2.0),
                "x" to Const(1.0) / Const(2.0) * Const(0.0)
            ),
            Pair(
                x,
                "x" to Const(1.0) / x * Const(1.0)
            ),
            Pair(
                Const(2.0) * x,
                "x" to // (ln(2x))' = 1/(2x) * (2x)' = 1/(2x) * (0*x + 1*2)
                    Const(1.0) / (Const(2.0) * x) * (Const(0.0) * x + Const(1.0) * Const(2.0))
            )
        )
    ) { (a, p) ->
        val (of, expected) = p

        val result = Log.derivative(Parameter(of), a)

        result shouldBe expected
    }

    // mix
    withData(
        nameFn = { (a, expected) -> "($a)' = ${expected.second}" },
        listOf(
            Pair(
                // (2e^x + 3)' = (2e^x)' + (3)' = 2(e^x)' + 0 = 2e^x
                exp(x) * Const(2.0) + Const(3.0),
                "x" to Const(2.0) * exp(x)
            ),
            Pair(
                // (2ln(x) + 3)' = (2ln(x))' + (3)' = 2(ln(x))' + 0 = 2/x
                log(x) * Const(2.0) + Const(3.0),
                "x" to Const(2.0) * (Const(1.0) / x)
            ),
            Pair(
                // (2x^2+3x+4)' = 2x*2+3 = 4x+3
                Const(2.0) * x.pow(Const(2.0)) + Const(3.0) * x + Const(4.0),
                "x" to Const(4.0) * x + Const(3.0)
            )
        )
    ) { (a, p) ->
        val (of, expected) = p

        val result = a.derivative(Parameter(of)).simplify()

        result shouldBe expected
    }
})