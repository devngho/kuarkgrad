## kuarkgrad

English | [한국어](README.ko.md)

A tiny symbolic/forward-pass differentiation library written in Kotlin. Intended for educational purposes.

This isn't optimized for performance, so it's not suitable for production use.

## Usage

### Add the dependency

TODO

### Symbolic differentiation

```kt
val x by param() // is equivalent to `val x = Parameter("x")`
val f = 2 * x.pow(2) + 3 * x + 1

val df = f.derivative(x).simplify()

println(f) // (((2 * (x^2)) + (3 * x)) + 1)
println(df) // ((4 * x) + 3)

println(f.evaluate(x to 2).toStringExpanded()) // 15
println(df.evaluate(x to 2).toStringExpanded()) // 11
```

### Forward-mode automatic differentiation

`evaluateWithDerivative` computes the value and the derivative together in a single pass, without building a derivative graph.

```kt
val (value, derivative) = f.evaluateWithDerivative(x, x to 2) // 15, 11
```

### Supported operations

| Operation      | Usage      |
|----------------|------------|
| Addition       | `a + b`    |
| Subtraction    | `a - b`    |
| Multiplication | `a * b`    |
| Division       | `a / b`    |
| Power          | `a.pow(b)` |
| Exponential    | `exp(a)`   |
| Natural log    | `log(a)`   |

Values are `BigDecimal`s and calculated with arbitrary precision, with two exceptions:

- Division rounds to 30 significant digits by default. Pass a `DecimalMode` to `evaluate` to change it, and to `simplify` too, since it folds constants.
- `exp`, `log`, and `pow` with a noninteger exponent are computed through `Double`, so their results only have double precision.

## License

MIT License

This library uses open-source libraries as follows:
- [ionspin/kotlin-multiplatform-bignum](http://github.com/ionspin/kotlin-multiplatform-bignum/) - [Apache License Version 2.0](https://github.com/ionspin/kotlin-multiplatform-bignum/blob/main/LICENSE)
- [kotest](https://github.com/kotest/kotest) - [Apache License Version 2.0](https://github.com/kotest/kotest/blob/master/LICENSE)