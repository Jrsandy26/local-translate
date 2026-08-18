package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.engine.OfflineTranslationEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Live Translate", appName)
    }

    @Test
    fun `offline translation engine translates known phrase`() {
        val input = "I would like to express my heartfelt gratitude for your presentations and your time today."
        val translatedJa = OfflineTranslationEngine.translate(input, "en", "ja")
        assertTrue(translatedJa.contains("感謝") || translatedJa.contains("本日"))

        val translatedEs = OfflineTranslationEngine.translate(input, "en", "es")
        assertTrue(translatedEs.contains("agradecimiento") || translatedEs.contains("presentaciones"))
    }

    @Test
    fun `offline translation engine handles template sentences`() {
        val input = "Welcome to our conference"
        val translatedJa = OfflineTranslationEngine.translate(input, "en", "ja")
        assertNotNull(translatedJa)
        assertTrue(translatedJa.isNotEmpty())
    }
}

