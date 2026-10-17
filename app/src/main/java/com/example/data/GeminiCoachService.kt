package com.example.data

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiCoachService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    suspend fun askScienceCoach(userQuestion: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY

        // Check if API key is empty, default placeholder, or missing
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            // Find offline matching scientific answer
            return@withContext getOfflineAnswer(userQuestion)
        }

        try {
            val systemPrompt = "You are Jeremy AI, the expert Lead Exercise Physiologist and Sports Nutritionist for Gym3D (Built With Science methodology). " +
                    "Your answers must be grounded in peer-reviewed exercise science (referencing Schoenfeld, Helms, Aragon, Phillips). " +
                    "Always emphasize progressive mechanical tension, energy balance, evidence-based supplementation (Creatine 5g/day, Protein 1.6-2.2g/kg), and biomechanical alignment. " +
                    "Provide clear, actionable, concise advice formatted cleanly."

            val jsonBody = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", "$systemPrompt\n\nUser Question: $userQuestion")
                            })
                        })
                    })
                }
                put("contents", contentsArray)

                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("topP", 0.95)
                    put("maxOutputTokens", 800)
                })
            }

            val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext getOfflineAnswer(userQuestion)
            }

            val responseText = response.body?.string() ?: ""
            val jsonResponse = JSONObject(responseText)
            val candidates = jsonResponse.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text")

            if (!text.isNullOrBlank()) {
                text
            } else {
                getOfflineAnswer(userQuestion)
            }
        } catch (e: Exception) {
            getOfflineAnswer(userQuestion)
        }
    }

    private fun getOfflineAnswer(query: String): String {
        val lower = query.lowercase()
        val match = ScienceEngine.offlineScienceQA.firstOrNull { qa ->
            lower.contains("bench") && qa.question.contains("bench", ignoreCase = true) ||
            lower.contains("morning") && qa.question.contains("morning", ignoreCase = true) ||
            lower.contains("shoulder") || lower.contains("delt") && qa.question.contains("shoulder", ignoreCase = true) ||
            lower.contains("deficit") || lower.contains("cut") && qa.question.contains("deficit", ignoreCase = true) ||
            lower.contains("rest") && qa.question.contains("rest", ignoreCase = true)
        }

        return if (match != null) {
            "${match.answer}\n\n📚 Evidence Base: ${match.scientificCitations}"
        } else {
            "According to exercise science meta-analyses (Schoenfeld et al., Morton et al.):\n\n" +
            "1. Mechanical Tension: Ensure you are applying the Double Progression rule—reach the top of your target rep range (e.g., 8–12 reps) across all working sets before adding +2.5 kg.\n" +
            "2. Volume Landmarks: Aim for 12–20 weekly sets per muscle group (Maximum Adaptive Volume).\n" +
            "3. Nutrition: Consume 1.6–2.2 g/kg of daily protein and maintain an energy balance matching your goal.\n\n" +
            "📚 Citations: Helms et al. (2015), Schoenfeld (2016)"
        }
    }
}
