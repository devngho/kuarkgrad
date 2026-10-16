package io.github.devngho.kuarkgrad

import io.github.devngho.kuarkgrad.func.*
import io.github.devngho.kuarkgrad.util.simplify
import io.github.devngho.kuarkgrad.value.Const
import io.github.devngho.kuarkgrad.value.Parameter
import io.kotest.core.spec.style.FreeSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe

class SimplifierTest: FreeSpec({
    val x = Parameter("x")

    withData(
        nameFn = { (a, expected) -> "$a = $expected" },
        listOf(
            // add
            Pair(
                x + Const(0.0),
                x
            ),
            Pair(
                Const(0.0) + x,
                x
            ),
            Pair(
                Const(1.0) + Const(2.0),
                Const(3.0)
            ),
            Pair(
                Const(1.0) + x,
                x + Const(1.0)
            ),
            Pair(
                (x + Const(1.0)) + Const(2.0),
                x + Const(3.0)
            ),
            Pair(
                (Const(1.0) + x) + Const(-1.0),
                x
            ),
            // subtract
            Pair(
                x - Const(0.0),
                x
            ),
            Pair(
                Const(1.0) - Const(2.0),
                Const(-1.0)
            ),
            // multiply
            Pair(
                x * Const(1.0),
                x
            ),
            Pair(
                Const(1.0) * x,
                x
            ),
            Pair(
                x * Const(0.0),
                Const(0.0)
            ),
            Pair(
                Const(0.0) * x,
                Const(0.0)
            ),
            Pair(
                Const(2.0) * Const(3.0),
                Const(6.0)
            ),
            Pair(
                x * Const(2.0),
                Const(2.0) * x
            ),
            Pair(
                (Const(2.0) * x) * Const(2.0),
                Const(4.0) * x
            ),
            Pair(
                Const(3.0) * (x * Const(2.0)),
                Const(6.0) * x
            ),
            Pair(
                (Const(2.0) * x) * Const(0.5),
                x
            ),
            // div
            Pair(
                Const(0.0) / x,
                Const(0.0)
            ),
            Pair(
                Const(1.0) / x,
                Const(1.0) / x
            ),
            Pair(
                x / Const(1.0),
                x
            ),
            Pair(
                Const(6.0) / Const(3.0),
                Const(2.0)
            ),
            // pow
            Pair(
                x.pow(Const(1.0)),
                x
            ),
            Pair(
                x.pow(Const(0.0)),
                Const(1.0)
            ),
            Pair(
                Const(0.0).pow(Const(2.0)),
                Const(0.0)
            ),
            Pair(
                // 0^x is left as is, since it isn't 0 for x <= 0
                Const(0.0).pow(x),
                Const(0.0).pow(x)
            ),
            Pair(
                Const(2.0).pow(Const(3.0)),
                Const(8.0)
            )
        )
    ) { (a, expected) ->
        val result = a.simplify()

        result shouldBe expected
    }
})