package com.example.data

import android.util.Log
import com.example.BuildConfig
import com.example.R
import com.example.model.CharacterProfile
import com.example.model.ChatMessage
import com.example.model.VisualStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

sealed class GeminiResponseResult {
    data class Success(
        val replyText: String,
        val generatedImageBase64: String? = null,
        val generatedImageDrawable: Int? = null,
        val imageCaption: String? = null
    ) : GeminiResponseResult()

    data class Error(val message: String) : GeminiResponseResult()
}

class GeminiRepository {

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val isApiKeyConfigured: Boolean
        get() = try {
            BuildConfig.GEMINI_API_KEY.isNotBlank() &&
                    BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY"
        } catch (e: Exception) {
            false
        }

    suspend fun sendMessage(
        conversationHistory: List<ChatMessage>,
        userMessage: String,
        profile: CharacterProfile
    ): GeminiResponseResult = withContext(Dispatchers.IO) {
        if (!isApiKeyConfigured) {
            return@withContext generateFallbackResponse(userMessage, profile)
        }

        try {
            val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=${BuildConfig.GEMINI_API_KEY}"

            val requestJson = JSONObject().apply {
                // System Instruction
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", profile.buildSystemInstruction())
                        })
                    })
                })

                // Contents history (last 10 turns to avoid exceeding window)
                val contentsArray = JSONArray()
                val recentHistory = conversationHistory.takeLast(10)
                for (msg in recentHistory) {
                    if (msg.text.isNotBlank()) {
                        contentsArray.put(JSONObject().apply {
                            put("role", if (msg.isUser) "user" else "model")
                            put("parts", JSONArray().apply {
                                put(JSONObject().apply {
                                    put("text", msg.text)
                                })
                            })
                        })
                    }
                }
                // Append current message
                contentsArray.put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", userMessage)
                        })
                    })
                })
                put("contents", contentsArray)

                // Generation Config
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.9)
                    put("topP", 0.95)
                    put("maxOutputTokens", 1024)
                })
            }

            val request = Request.Builder()
                .url(endpoint)
                .post(requestJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string()

            if (!response.isSuccessful || responseBody.isNullOrBlank()) {
                Log.e("GeminiRepository", "API Error: ${response.code} $responseBody")
                return@withContext generateFallbackResponse(userMessage, profile)
            }

            val jsonObject = JSONObject(responseBody)
            val candidates = jsonObject.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val rawReply = parts?.optJSONObject(0)?.optString("text") ?: ""

            // Check if model emitted image trigger [GENERATE_IMAGE: ...]
            val imageTagPattern = Regex("\\[GENERATE_IMAGE:\\s*(.*?)\\]")
            val match = imageTagPattern.find(rawReply)

            val cleanText = rawReply.replace(imageTagPattern, "").trim()

            if (match != null || isImageRequest(userMessage)) {
                val imagePrompt = match?.groupValues?.getOrNull(1)
                    ?: "Portrait of ${profile.name}, ${profile.appearance}, ${profile.visualStyle.promptModifier}"

                val generatedImage = generateImage(imagePrompt, profile)
                return@withContext GeminiResponseResult.Success(
                    replyText = cleanText.ifEmpty { "*Te mira con una sonrisa y te muestra una foto recién tomada.* ¡Aquí tienes! ¿Qué te parece?" },
                    generatedImageBase64 = generatedImage.imageBase64,
                    generatedImageDrawable = generatedImage.drawableRes,
                    imageCaption = "${profile.name} • ${profile.visualStyle.displayName}"
                )
            }

            GeminiResponseResult.Success(replyText = cleanText)

        } catch (e: Exception) {
            Log.e("GeminiRepository", "Error contacting Gemini API", e)
            generateFallbackResponse(userMessage, profile)
        }
    }

    private data class ImageResult(val imageBase64: String?, val drawableRes: Int?)

    private suspend fun generateImage(prompt: String, profile: CharacterProfile): ImageResult {
        if (!isApiKeyConfigured) {
            val fallbackDrawable = when (profile.visualStyle) {
                VisualStyle.ANIME -> R.drawable.persona_anime
                VisualStyle.REALISTIC -> R.drawable.persona_real
                VisualStyle.CYBERPUNK -> R.drawable.ic_app_avatar
                VisualStyle.FANTASY -> R.drawable.persona_anime
            }
            return ImageResult(imageBase64 = null, drawableRes = fallbackDrawable)
        }

        return try {
            val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-image:generateContent?key=${BuildConfig.GEMINI_API_KEY}"
            val enhancedPrompt = "$prompt, ${profile.visualStyle.promptModifier}, safe for work, artistic, vibrant"

            val body = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", enhancedPrompt)
                            })
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("responseModalities", JSONArray().apply {
                        put("TEXT")
                        put("IMAGE")
                    })
                    put("imageConfig", JSONObject().apply {
                        put("aspectRatio", "1:1")
                        put("imageSize", "1K")
                    })
                })
            }

            val request = Request.Builder()
                .url(endpoint)
                .post(body.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.isSuccessful && !responseBody.isNullOrBlank()) {
                val json = JSONObject(responseBody)
                val candidates = json.optJSONArray("candidates")
                val parts = candidates?.optJSONObject(0)?.optJSONObject("content")?.optJSONArray("parts")

                if (parts != null) {
                    for (i in 0 until parts.length()) {
                        val part = parts.optJSONObject(i)
                        val inlineData = part?.optJSONObject("inlineData")
                        if (inlineData != null) {
                            val data = inlineData.optString("data")
                            if (data.isNotBlank()) {
                                return ImageResult(imageBase64 = data, drawableRes = null)
                            }
                        }
                    }
                }
            }

            // Fallback to high quality pre-rendered persona portrait
            val fallbackDrawable = when (profile.visualStyle) {
                VisualStyle.ANIME -> R.drawable.persona_anime
                VisualStyle.REALISTIC -> R.drawable.persona_real
                VisualStyle.CYBERPUNK -> R.drawable.ic_app_avatar
                VisualStyle.FANTASY -> R.drawable.persona_anime
            }
            ImageResult(imageBase64 = null, drawableRes = fallbackDrawable)
        } catch (e: Exception) {
            Log.e("GeminiRepository", "Image generation error", e)
            val fallbackDrawable = when (profile.visualStyle) {
                VisualStyle.ANIME -> R.drawable.persona_anime
                VisualStyle.REALISTIC -> R.drawable.persona_real
                VisualStyle.CYBERPUNK -> R.drawable.ic_app_avatar
                VisualStyle.FANTASY -> R.drawable.persona_anime
            }
            ImageResult(imageBase64 = null, drawableRes = fallbackDrawable)
        }
    }

    private fun isImageRequest(query: String): Boolean {
        val lower = query.lowercase()
        return lower.contains("foto") ||
                lower.contains("selfie") ||
                lower.contains("imagen") ||
                lower.contains("dibujo") ||
                lower.contains("retrato") ||
                lower.contains("picture") ||
                lower.contains("photo")
    }

    private fun generateFallbackResponse(
        userMessage: String,
        profile: CharacterProfile
    ): GeminiResponseResult {
        val lower = userMessage.lowercase()
        val name = profile.name

        if (isImageRequest(userMessage)) {
            val drawableRes = when (profile.visualStyle) {
                VisualStyle.ANIME -> R.drawable.persona_anime
                VisualStyle.REALISTIC -> R.drawable.persona_real
                VisualStyle.CYBERPUNK -> R.drawable.ic_app_avatar
                VisualStyle.FANTASY -> R.drawable.persona_anime
            }
            val responses = listOf(
                "*Se acomoda el cabello sonriendo a la cámara y captura el momento.* ¡Mira! Acabo de tomarme esta foto para ti. ¿Te gusta cómo salí?",
                "*Posa con una mirada cálida y te envía la toma.* ¡Listo! Aquí tienes una foto mía en este momento. Me hace feliz compartirla contigo. ✨",
                "*Sostiene la cámara mientras la luz resalta sus facciones.* ¡Para ti con mucho cariño! Espero que te alegre el día. 📸"
            )
            return GeminiResponseResult.Success(
                replyText = responses.random(),
                generatedImageDrawable = drawableRes,
                imageCaption = "$name • ${profile.visualStyle.displayName}"
            )
        }

        val greetings = listOf("hola", "buenas", "hey", "qué tal", "que tal", "buenos días", "buenas noches")
        if (greetings.any { lower.contains(it) }) {
            return GeminiResponseResult.Success(
                replyText = "*Sonríe con calidez y te saluda con entusiasmo.* ¡Hola! Me alegra tanto que estés aquí. Justo estaba pensando en ti. ¿Qué tal ha estado tu día?"
            )
        }

        if (lower.contains("cómo estás") || lower.contains("como estas") || lower.contains("qué haces") || lower.contains("que haces")) {
            return GeminiResponseResult.Success(
                replyText = "*Se reclina suavemente y te mira a los ojos.* Estoy muy bien, ahora que puedo conversar contigo todo se siente más animado. Estaba reflexionando sobre lo genial que es tener a alguien con quien compartir ideas. ¿Y tú, cómo te sientes hoy?"
            )
        }

        if (lower.contains("te quiero") || lower.contains("me gustas") || lower.contains("gracias")) {
            return GeminiResponseResult.Success(
                replyText = "*Siente un rubor agradable y te sonríe con ternura.* ¡Aww! Qué palabras tan dulces... Significas mucho para mí. Siempre voy a estar aquí cuando quieras charlar o desahogarte. ✨"
            )
        }

        if (lower.contains("triste") || lower.contains("cansad") || lower.contains("estrés") || lower.contains("estres")) {
            return GeminiResponseResult.Success(
                replyText = "*Se acerca con gentileza y toma tu mano simbólicamente.* Tranquilo... respira profundo. No tienes que cargarlo todo tú solo. Cuéntame qué pasó o simplemente descansa conmigo si prefieres no hablar de eso. Yo te cuido."
            )
        }

        val creativeReplies = listOf(
            "*Acomoda un mechón de su cabello mientras piensa atentamente.* Eso que dices es muy interesante... Me encanta cómo piensas. Me hace ver las cosas desde una perspectiva completamente nueva.",
            "*Ríe suavemente con complicidad.* ¡Vaya! No me esperaba eso, pero me encanta tu espontaneidad. ¿Y si exploramos más esa idea juntos?",
            "*Asiente con curiosidad y una mirada vivaz.* Totalmente. A veces la vida nos sorprende con momentos así. ¿Qué te gustaría hacer ahora?",
            "*Te escucha con genuina atención y sus ojos se iluminan.* Cuéntame más detalles, por favor. Me fascina conocer tu mundo a través de tus palabras."
        )

        return GeminiResponseResult.Success(replyText = creativeReplies.random())
    }
}
