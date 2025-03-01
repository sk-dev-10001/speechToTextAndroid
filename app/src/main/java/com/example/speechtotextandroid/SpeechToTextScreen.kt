package com.example.speechtotextandroid

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat

@Composable
fun SpeechToTextScreen() {
    val context = LocalContext.current
    var speechText by remember { mutableStateOf("Tap the mic and start speaking...") }
    var isListening by remember { mutableStateOf(false) }

    val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }

    // Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (!granted) speechText = "Microphone permission required!"
        }
    )

    // Request Permission if Not Granted
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // Speech Recognizer Listener
    val recognitionListener = object : RecognitionListener {
        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                speechText = matches[0]  // Final Speech Result
            }
            isListening = false
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                speechText = matches[0]  // Live Partial Speech Update
            }
        }

        override fun onError(error: Int) {
            speechText = "Recognition Error: $error"
            isListening = false
        }

        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}
        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    speechRecognizer.setRecognitionListener(recognitionListener)

    // Start Listening Function
    fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)  // Enable Partial Results
        }
        speechRecognizer.startListening(intent)
        isListening = true
    }

    // Stop Listening Function
    fun stopListening() {
        speechRecognizer.stopListening()
        isListening = false
    }

    // --- UI Layout ---
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Speech Output Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .background(Color(0xFFEFEFEF), RoundedCornerShape(12.dp))
                .border(2.dp, Color.Gray, RoundedCornerShape(12.dp))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = speechText, fontSize = 18.sp, color = Color.Black)
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Animated Listening Indicator
        AnimatedVisibility(visible = isListening, enter = fadeIn(), exit = fadeOut()) {
            Text(text = "Listening...", color = Color.Red, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(10.dp))
            CircularProgressIndicator(color = Color.Red)
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Microphone Button
        IconButton(
            onClick = { if (isListening) stopListening() else startListening() },
            modifier = Modifier
                .size(80.dp)
                .background(if (isListening) Color.Red else Color.Blue, shape = CircleShape)
        ) {
            Icon(
                imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                contentDescription = "Mic",
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
        }
    }
}
