package com.example

import com.example.util.DateFormats
import com.example.util.TextStats
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun textStats_calculation_isCorrect() {
    val sampleText = "Hello world! This is AI Notes.\n\nSecond paragraph here."
    val stats = TextStats.calculate(sampleText)

    assertEquals(8, stats.words)
    assertEquals(3, stats.sentences)
    assertEquals(2, stats.paragraphs)
    assertTrue(stats.characters > 0)
    assertTrue(stats.charactersNoSpaces > 0)
  }

  @Test
  fun dateFormats_formatsTimestampCorrectly() {
    val now = System.currentTimeMillis()
    val formatted = DateFormats.formatNoteTimestamp(now)
    assertTrue(formatted.startsWith("Today"))
  }
}

