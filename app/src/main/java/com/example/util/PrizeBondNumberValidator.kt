package com.example.util

object PrizeBondNumberValidator {
  private val bengaliDigits = charArrayOf('০','১','২','৩','৪','৫','৬','৭','৮','৯')
  private val latinDigits = charArrayOf('0','1','2','3','4','5','6','7','8','9')

  fun normalize(input: String): String {
    val builder = StringBuilder(input.length)
    input.forEach { ch ->
      val index = bengaliDigits.indexOf(ch)
      builder.append(if (index >= 0) latinDigits[index] else ch)
    }
    return builder.toString()
  }

  fun validate(input: String): String? {
    val normalized = normalize(input).trim()
    return normalized.takeIf { it.matches(Regex("\\d{7}")) }
  }

  fun requireValid(input: String): String =
    validate(input) ?: throw IllegalArgumentException(
      "Bond number must contain exactly 7 digits (e.g. 0123456)."
    )
}
