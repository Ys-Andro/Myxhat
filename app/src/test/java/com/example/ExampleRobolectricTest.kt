package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.CharacterProfile
import com.example.model.ConversationStyle
import com.example.model.VisualStyle
import com.example.model.VoiceTone
import org.junit.Assert.assertEquals
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
    assertEquals("Persona AI", appName)
  }

  @Test
  fun `test character profile system instruction contains personality and appearance`() {
    val profile = CharacterProfile(
      name = "Aria",
      personality = "Curiosa y dulce",
      appearance = "Pelo violeta neón",
      visualStyle = VisualStyle.ANIME,
      voiceTone = VoiceTone.WARM,
      conversationStyle = ConversationStyle.ROLEPLAY
    )
    val instruction = profile.buildSystemInstruction()
    assertTrue(instruction.contains("Aria"))
    assertTrue(instruction.contains("Curiosa y dulce"))
    assertTrue(instruction.contains("Pelo violeta neón"))
    assertTrue(instruction.contains("Anime"))
  }

  @Test
  fun `verify presets exist`() {
    assertTrue(CharacterProfile.PRESETS.isNotEmpty())
    val aria = CharacterProfile.PRESETS.first()
    assertEquals("Aria", aria.name)
  }
}
