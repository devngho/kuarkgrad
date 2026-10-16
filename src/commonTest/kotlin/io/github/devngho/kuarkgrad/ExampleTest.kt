package io.github.devngho.kuarkgrad

import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import io.github.devngho.kuarkgrad.func.*
import io.github.devngho.kuarkgrad.util.simplify
import io.github.devngho.kuarkgrad.value.param
import io.github.devngho.kuarkgrad.value.evaluate
import io.github.devngho.kuarkgrad.value.evaluateWithDerivative
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe

// mirrors the README examples
class ExampleTest: FreeSpec({
    "derivative of 2x^2 + 3x + 1" {
        val x by param()
        val f = 2 * x.pow(2) + 3 * x + 1

        val df = f.derivative(x).simplify()

        f.toString() shouldBe "(((2 * (x^2)) + (3 * x)) + 1)"
        df.toString() shouldBe "((4 * x) + 3)"

        f.evaluate(x to 2) shouldBe 15.toBigDecimal()
        df.evaluate(x to 2) shouldBe 11.toBigDecimal()

        val (value, derivative) = f.evaluateWithDerivative(x, x to 2)

        value shouldBe 15.toBigDecimal()
        derivative shouldBe 11.toBigDecimal()
    }
})
