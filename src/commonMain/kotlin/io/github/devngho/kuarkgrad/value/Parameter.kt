package io.github.devngho.kuarkgrad.value

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.DecimalMode
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty

data class Parameter(
    val name: String
): Variable {
    override fun derivative(of: Parameter): Variable = if (of == this) Const(1.0.toBigDecimal()) else Const(0.0.toBigDecimal())

    override fun evaluate(context: Map<Parameter, BigDecimal>, mode: DecimalMode): BigDecimal = context[this] ?: throw IllegalArgumentException("Parameter $name not found in context")

    override fun evaluateWithDerivative(of: Parameter, context: Map<Parameter, BigDecimal>, mode: DecimalMode): ValueWithGrad =
        ValueWithGrad(evaluate(context, mode), if (of == this) BigDecimal.ONE else BigDecimal.ZERO)

    override fun toString(): String = name
}

// names a parameter after its property: `val x by param()` is Parameter("x")
fun param() = PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, Parameter>> { _, property ->
    val parameter = Parameter(property.name)
    ReadOnlyProperty { _, _ -> parameter }
}
