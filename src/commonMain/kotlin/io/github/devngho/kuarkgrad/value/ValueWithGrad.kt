package io.github.devngho.kuarkgrad.value

import com.ionspin.kotlin.bignum.decimal.BigDecimal

data class ValueWithGrad(
    val value: BigDecimal,
    val derivative: BigDecimal
)
