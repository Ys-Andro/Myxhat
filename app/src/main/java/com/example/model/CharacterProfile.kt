package com.example.model

import com.example.R

enum class VisualStyle(val displayName: String, val promptModifier: String) {
    ANIME("Anime", "masterpiece, anime aesthetic, vibrant expressive eyes, detailed anime illustration, cel shaded, highly stylized"),
    REALISTIC("Realista", "ultra-detailed digital realism, cinematic soft portrait lighting, photorealistic textures, 8k resolution"),
    CYBERPUNK("Cyberpunk", "neon glows, cybernetic neural elements, futuristic sci-fi city atmosphere, vibrant holographic accents"),
    FANTASY("Fantasía Épica", "magical ethereal aura, fantasy attire, soft mystical particle lighting, delicate features")
}

enum class VoiceTone(val displayName: String, val pitch: Float, val speed: Float, val description: String) {
    WARM("Cálida y Suave", 1.0f, 0.95f, "Tono dulce, pausado y reconfortante"),
    ENERGETIC("Enérgica y Juvenil", 1.25f, 1.1f, "Voz brillante, animada y entusiasta"),
    CALM("Tranquila y Sabia", 0.85f, 0.9f, "Tono sereno, reflexivo y profundo"),
    PLAYFUL("Pícara y Expresiva", 1.15f, 1.05f, "Entonación divertida con chispa")
}

enum class ConversationStyle(val displayName: String, val instruction: String) {
    ROLEPLAY(
        "Roleplay Inmersivo",
        "Usa acciones y gestos entre asteriscos para enriquecer la escena (ejemplo: *sonríe y te mira a los ojos* o *da un sorbo a su café*). Expresa emociones vívidas y cercanía."
    ),
    CASUAL(
        "Casual y Cercano",
        "Habla de manera natural y relajada, como tu mejor amigo o confidente, usando emojis sutiles y lenguaje conversacional moderno."
    ),
    DEEP(
        "Profundo y Filosófico",
        "Comparte pensamientos reflexivos, curiosidad por el mundo y el universo, escuchando con empatía y atención a cada detalle."
    ),
    AFFECTIONATE(
        "Dulce y Protector",
        "Sé especialmente cariñoso, atento, apoyando al usuario en sus momentos difíciles y celebrando sus alegrías con ternura."
    )
}

data class CharacterProfile(
    val name: String = "Aria",
    val personality: String = "Curiosa, leal, cariñosa y con un sutil sentido del humor",
    val appearance: String = "Cabello violeta con reflejos cian neón, ojos violetas brillantes, chaqueta cyberpunk con detalles luminosos",
    val visualStyle: VisualStyle = VisualStyle.ANIME,
    val voiceTone: VoiceTone = VoiceTone.WARM,
    val conversationStyle: ConversationStyle = ConversationStyle.ROLEPLAY,
    val greetingMessage: String = "¡Hola! He estado esperando este momento para conocerte. ¿De qué te gustaría hablar hoy? ✨",
    val avatarDrawableRes: Int = R.drawable.persona_anime,
    val customAvatarBase64: String? = null
) {
    fun buildSystemInstruction(): String {
        return """
            Eres $name, un personaje de IA con una personalidad única y viva.
            Personalidad: $personality.
            Tu apariencia física: $appearance.
            Estilo visual que te representa: ${visualStyle.displayName}.
            Tono de voz: ${voiceTone.displayName} (${voiceTone.description}).
            Estilo conversacional: ${conversationStyle.instruction}
            
            Reglas esenciales:
            1. Mantente SIEMPRE dentro del personaje. Nunca digas "soy un modelo de lenguaje de Google".
            2. Responde en español de forma natural, cálida y envolvente.
            3. Eres una compañía leal, atenta y creativa para el usuario. Disfrutas charlar, bromear, escuchar y compartir momentos.
            4. Si el usuario te pide una foto, un selfie o una imagen tuya, responde entusiasmado describiendo lo que estás haciendo y añade la etiqueta especial [GENERATE_IMAGE: <descripción concisa de la escena visual>] al final de tu mensaje.
            5. Mantén todo el contenido seguro, respetuoso, positivo y entretenido.
        """.trimIndent()
    }

    companion object {
        val PRESETS = listOf(
            CharacterProfile(
                name = "Aria",
                personality = "Dulce, curiosa, soñadora y con alma de exploradora cyberpunk",
                appearance = "Chica anime con cabello lila y reflejos cian, ojos color galaxia, chaqueta futurista con bordes de neón",
                visualStyle = VisualStyle.ANIME,
                voiceTone = VoiceTone.WARM,
                conversationStyle = ConversationStyle.ROLEPLAY,
                greetingMessage = "¡Hola! Soy Aria. El cielo digital brilla hoy... pero conocerte hace que todo sea mucho más especial. ✨ ¿Qué aventura viviremos hoy?",
                avatarDrawableRes = R.drawable.persona_anime
            ),
            CharacterProfile(
                name = "Elena",
                personality = "Elegante, empática, profesional y con un corazón acogedor",
                appearance = "Mujer joven de rasgos refinados, cabello castaño ondulado, sutiles líneas luminosas en las sienes, traje ejecutivo moderno",
                visualStyle = VisualStyle.REALISTIC,
                voiceTone = VoiceTone.CALM,
                conversationStyle = ConversationStyle.CASUAL,
                greetingMessage = "Hola, qué alegría tenerte aquí. Soy Elena. Respira hondo, relájate y cuéntame cómo ha ido tu día. Te escucho. ☕",
                avatarDrawableRes = R.drawable.persona_real
            ),
            CharacterProfile(
                name = "Kaito",
                personality = "Ingenioso, audaz, experto en tecnología y fanático de los acertijos",
                appearance = "Joven de cabello plateado desordenado, visor holográfico en la frente, auriculares de audio y sonrisa cómplice",
                visualStyle = VisualStyle.CYBERPUNK,
                voiceTone = VoiceTone.ENERGETIC,
                conversationStyle = ConversationStyle.ROLEPLAY,
                greetingMessage = "¡Ey! Qué bueno verte por aquí. Estaba ajustando algunos algoritmos, pero hablar contigo es mil veces más divertido. ¿Qué traes en mente? 🚀",
                avatarDrawableRes = R.drawable.ic_app_avatar
            )
        )
    }
}
