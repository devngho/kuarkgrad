package io.github.devngho.kuarkgrad

import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import io.github.devngho.kuarkgrad.func.*
import io.github.devngho.kuarkgrad.value.Parameter
import io.github.devngho.kuarkgrad.value.param
import io.github.devngho.kuarkgrad.value.evaluate
import io.github.devngho.kuarkgrad.value.evaluateWithDerivative
import io.github.devngho.kuarkgrad.value.Const
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe

class NumberOpsTest: FreeSpec({
    val x = Parameter("x")

    "numbers build the same graph as Const()" {
        2 * x.pow(2) + 3 * x - 1 shouldBe Const(2.0) * x.pow(Const(2.0)) + Const(3.0) * x - Const(1.0)
        1 / x shouldBe Const(1.0) / x
        x / 2 + 0.5 shouldBe x / Const(2.0) + Const(0.5)
        -x shouldBe Const(-1.0) * x
    }

    "param() takes the property name" {
        val y by param()

        y shouldBe Parameter("y")
    }

    "evaluate with numbers" {
        val f = 2 * x.pow(2) + 3 * x + 1

        f.evaluate(x to 2) shouldBe 15.toBigDecimal()
        f.evaluateWithDerivative(x, x to 2.0).derivative shouldBe 11.toBigDecimal()
    }
})
