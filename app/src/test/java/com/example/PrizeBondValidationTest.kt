package com.example

import com.example.ocr.PrizeBondOcrEngine
import com.example.util.PrizeBondNumberValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PrizeBondValidationTest {
  @Test fun acceptsExactlySevenLatinDigits() {
    assertEquals("0123456", PrizeBondNumberValidator.validate("0123456"))
    assertEquals("9999999", PrizeBondNumberValidator.validate("9999999"))
  }

  @Test fun acceptsExactlySevenBengaliDigits() {
    assertEquals("0123456", PrizeBondNumberValidator.validate("০১২৩৪৫৬"))
  }

  @Test fun rejectsInvalidLengthsAndCharactersWithoutPadding() {
    assertNull(PrizeBondNumberValidator.validate("123"))
    assertNull(PrizeBondNumberValidator.validate("123456"))
    assertNull(PrizeBondNumberValidator.validate("12345678"))
    assertNull(PrizeBondNumberValidator.validate("12A4567"))
  }

  @Test fun ocrExtractsOnlyStrictSevenDigitValues() {
    val candidates = PrizeBondOcrEngine.extractBondsFromRecognizedText(
      "VALID 0123456 INVALID 12345 INVALID 12345678"
    )
    assertEquals(1, candidates.size)
    assertEquals("0123456", candidates.single().number)
  }

  @Test fun bengaliConversionRemainsSupported() {
    assertEquals("0123456789", PrizeBondOcrEngine.convertBengaliDigitsToLatin("০১২৩৪৫৬৭৮৯"))
    assertTrue(PrizeBondNumberValidator.validate("০১২৩৪৫৬") != null)
  }
}
