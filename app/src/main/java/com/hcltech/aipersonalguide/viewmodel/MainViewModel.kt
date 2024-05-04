package com.hcltech.aipersonalguide.viewmodel

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    // set your API key here
    private val apiKey = "Your API Key"
    val isGenerating = mutableStateOf(false)
    val conversations = mutableStateListOf<Triple<String, String, List<Bitmap>?>>()
    private val config = generationConfig {
        temperature = 0.7f
        //maxOutputTokens = 5000
    }

    private val geneminiProModel by lazy {
        GenerativeModel(
            modelName = "gemini-1.0-pro-001",
            apiKey = apiKey,
            generationConfig = config

        ).apply {
            startChat()
        }
    }

    private val geneminiProVisionModel by lazy {
        GenerativeModel(
            modelName = "gemini-1.0-pro-vision-latest",
            apiKey = apiKey,
           generationConfig = config
        ).apply {
            startChat()
        }
    }

    fun sendText(textPrompt: String, images: SnapshotStateList<Bitmap>) {
        val prompt = "I want to use you as my personal tourist guide. " +
                "I'm currently at a location and would like information about it.  " +
                "Can you analyze the image and tell me:" +
                "The name and history of the location\n" +
                "Interesting facts or trivia\n" +
                "Any ongoing events or exhibitions\n" +
                "Recommended things to do nearby\n" +
                "Links to relevant websites or reviews\n" +
                "Accessibility information (if applicable)" +
                "Additionally, based on the image, you can suggest:\n" +
                "\n" +
                "Similar places I might enjoy visiting\n" +
                "Hidden gems or off-the-beaten-path locations nearby\n" +
                "Restaurants or cafes with a good view or local specialties."
        isGenerating.value = true

        conversations.add(Triple("sent", textPrompt, images.toList()))
        conversations.add(Triple("received", "", null))

        val generativeModel = if (images.isNotEmpty()) geneminiProVisionModel else geneminiProModel

        val inputContent = content {
            images.forEach { imageBitmap ->
                image(imageBitmap)
            }
            text(prompt+textPrompt)

        }
        viewModelScope.launch {
            generativeModel.generateContentStream(inputContent)
                .collect { chunk ->

                    //Log.d("GeminiPro", "onNext: $chunk");
                    conversations[conversations.lastIndex] = Triple(
                        "received",
                        conversations.last().second + chunk.text,
                        null
                    )
                }
            isGenerating.value = false
        }
    }

}