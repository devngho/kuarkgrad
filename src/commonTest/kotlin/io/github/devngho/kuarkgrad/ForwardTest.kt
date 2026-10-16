package io.github.devngho.kuarkgrad

import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import io.github.devngho.kuarkgrad.func.*
import io.kotest.core.spec.style.FreeSpec
import io.kotest.datatest.withData
import io.kotest.matchers.comparables.shouldBeAtMost
import io.kotest.matchers.shouldBe

class ForwardTest: FreeSpec({
    withData(
        nameFn = { (a, b, expected) -> "$a + $b = $expected" },
        listOf(
            Triple(1.0, 2.0, 3.0),
            Triple(-1.0, 1.0, 0.0),
            Triple(0.0, 0.0, 0.0),
            Triple(1.5, 2.5, 4.0),
            Triple(-1.5, -2.5, -4.0)
        ).map { (a, b, expected) -> Triple(a.toBigDecimal(), b.toBigDecimal(), expected.toBigDecimal()) }
    ) { (a, b, expected) ->
        val result = with(defaultDecimalMode) { Add.forward(a, b) }

        result shouldBe expected
    }

    withData(
        nameFn = { (a, b, expected) -> "$a * $b = $expected" },
        listOf(
            Triple(1.0, 2.0, 2.0),
            Triple(-1.0, 1.0, -1.0),
            Triple(0.0, 0.0, 0.0),
            Triple(1.5, 2.5, 3.75),
            Triple(-1.5, -2.5, 3.75)
        ).map { (a, b, expected) -> Triple(a.toBigDecimal(), b.toBigDecimal(), expected.toBigDecimal()) }
    ) { (a, b, expected) ->
        val result = with(defaultDecimalMode) { Multiply.forward(a, b) }

        result shouldBe expected
    }

    withData(
        nameFn = { (a, b, expected) -> "$a - $b = $expected" },
        listOf(
            Triple(1.0, 2.0, -1.0),
            Triple(-1.0, 1.0, -2.0),
            Triple(0.0, 0.0, 0.0),
            Triple(1.5, 2.5, -1.0),
            Triple(-1.5, -2.5, 1.0)
        ).map { (a, b, expected) -> Triple(a.toBigDecimal(), b.toBigDecimal(), expected.toBigDecimal()) }
    ) { (a, b, expected) ->
        val result = with(defaultDecimalMode) { Subtract.forward(a, b) }

        result shouldBe expected
    }

    withData(
        nameFn = { (a, b, expected) -> "$a / $b = $expected" },
        listOf(
            Triple(1.0, 2.0, 0.5),
            Triple(-1.0, 1.0, -1.0),
            Triple(0.0, 1.0, 0.0),
            Triple(1.5, 2.5, 0.6),
            Triple(-1.5, -2.5, 0.6)
        ).map { (a, b, expected) -> Triple(a.toBigDecimal(), b.toBigDecimal(), expected.toBigDecimal()) }
    ) { (a, b, expected) ->
        val result = with(defaultDecimalMode) { Divide.forward(a, b) }

        result shouldBe expected
    }

    withData(
        nameFn = { (a, b, expected) -> "$a ^ $b = $expected" },
        listOf(
            Triple(2.0, 3.0, 8.0),
            Triple(-2.0, 3.0, -8.0),
            Triple(2.0, -3.0, 0.125),
            Triple(2.0, 0.5, kotlin.math.sqrt(2.0)),
            Triple(4.0, 0.5, 2.0)
        ).map { (a, b, expected) -> Triple(a.toBigDecimal(), b.toBigDecimal(), expected.toBigDecimal()) }
    ) { (a, b, expected) ->
        val result = with(defaultDecimalMode) { PowerOf.forward(a, b) }

        (result - expected).abs() shouldBeAtMost 1e-10.toBigDecimal()
    }

    withData(
        nameFn = { (a, expected) -> "exp($a) = $expected" },
        listOf(
            Pair(0.0, 1.0),
            Pair(1.0, kotlin.math.E),
            Pair(-1.0, 1 / kotlin.math.E),
            Pair(2.0, kotlin.math.E * kotlin.math.E)
        ).map { (a, expected) -> Pair(a.toBigDecimal(), expected.toBigDecimal()) }
    ) { (a, expected) ->
        val result = with(defaultDecimalMode) { Exp.forward(a) }

        (result - expected).abs() shouldBeAtMost 1e-10.toBigDecimal()
    }

    withData(
        nameFn = { (a, expected) -> "log($a) = $expected" },
        listOf(
            Pair(1.0, 0.0),
            Pair(kotlin.math.E, 1.0),
            Pair(kotlin.math.E * kotlin.math.E, 2.0)
        ).map { (a, expected) -> Pair(a.toBigDecimal(), expected.toBigDecimal()) }
    ) { (a, expected) ->
        val result = with(defaultDecimalMode) { Log.forward(a) }

        (result - expected).abs() shouldBeAtMost 1e-10.toBigDecimal()
    }
})