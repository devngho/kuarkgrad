## kuarkgrad

[English](README.md) | 한국어

Kotlin으로 작성된 간단한 교육용 symbolic/forward-pass 미분 라이브러리입니다.

최적화되어 있지 않으므로, 프로덕션 환경에서는 사용하지 않는 것을 권장합니다.

## 사용법

### 의존성 추가

TODO

### symbolic 미분

```kt
val x by param() // `val x = Parameter("x")`와 같습니다
val f = 2 * x.pow(2) + 3 * x + 1

val df = f.derivative(x).simplify()

println(f) // (((2 * (x^2)) + (3 * x)) + 1)
println(df) // ((4 * x) + 3)

println(f.evaluate(x to 2).toStringExpanded()) // 15
println(df.evaluate(x to 2).toStringExpanded()) // 11
```

### forward-mode 자동 미분

`evaluateWithDerivative`는 `derivative`와 달리 도함수의 그래프를 만들지 않고, 한 번의 계산으로 값과 미분값을 함께 구할 수 있습니다.

```kt
val (value, derivative) = f.evaluateWithDerivative(x, x to 2) // 15, 11
```

### 지원하는 연산

| 연산     | 사용법     |
|----------|------------|
| 덧셈     | `a + b`    |
| 뺄셈     | `a - b`    |
| 곱셈     | `a * b`    |
| 나눗셈   | `a / b`    |
| 거듭제곱 | `a.pow(b)` |
| 지수함수 | `exp(a)`   |
| 자연로그 | `log(a)`   |

값은 `BigDecimal`이며, 아래 예외를 제외하고 임의 정밀도 계산을 지원합니다.

- 나눗셈은 기본적으로 유효숫자 30자리로 반올림하여 계산합니다. 이 동작을 변경하려면 `evaluate`를 호출할 때 `DecimalMode`를 설정하세요. `simplify`도 상수를 미리 계산하므로 이를 설정해야 합니다.
- `exp`, `log`, 정수가 아닌 지수의 `pow`는 `Double`로 계산하므로 정밀도도 이에 제한됩니다.

## 라이선스

MIT License

이 라이브러리는 다음과 같은 오픈소스 라이브러리를 사용합니다.
- [ionspin/kotlin-multiplatform-bignum](http://github.com/ionspin/kotlin-multiplatform-bignum/) - [Apache License Version 2.0](https://github.com/ionspin/kotlin-multiplatform-bignum/blob/main/LICENSE)
- [kotest](https://github.com/kotest/kotest) - [Apache License Version 2.0](https://github.com/kotest/kotest/blob/master/LICENSE)