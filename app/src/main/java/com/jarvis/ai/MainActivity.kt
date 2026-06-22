package com.jarvis.ai

import android.Manifest
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.Locale

class MainActivity : Activity(), TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var statusText: TextView? = null
    private var isListening = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        val micButton = findViewById<Button>(R.id.micButton)

        tts = TextToSpeech(this, this)

        micButton.setOnClickListener {
            if (checkMicPermission()) {
                startListening()
            }
        }
    }

    private fun checkMicPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 1)
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> { // RECORD_AUDIO
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startListening()
                } else {
                    speak("Microphone permission chahiye. Settings mein jaake permission de dijiyega.")
                }
            }
            2 -> { // CALL_PHONE
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    speak("Call permission mil gaya.")
                } else {
                    speak("Call permission nahi mili.")
                }
            }
        }
    }

    private fun startListening() {
        if (!checkMicPermission()) return

        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            speak("Speech recognition is not available on this device.")
            statusText?.text = "Speech recognition unavailable"
            return
        }

        speechRecognizer?.destroy()
        isListening = true
        statusText?.text = "Listening..."

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() { statusText?.text = "Listening..." }
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() { statusText?.text = "Processing..." }
            override fun onError(error: Int) {
                handleSpeechError(error)
            }
            override fun onResults(results: Bundle?) {
                val data = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val command = data?.getOrNull(0)?.lowercase() ?: ""
                if (command.isBlank()) {
                    speak("Samjha nahi, thoda phir se boliye.")
                    statusText?.text = "Tap to Speak"
                    isListening = false
                } else {
                    processCommand(command)
                }
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
        speechRecognizer?.startListening(intent)
    }

    private fun handleSpeechError(error: Int) {
        val msg = when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "Microphone mein dikkat hai."
            SpeechRecognizer.ERROR_CLIENT -> "Recognition client error."
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission nahi mili."
            SpeechRecognizer.ERROR_NETWORK -> "Network error. Internet check kijiye."
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout."
            SpeechRecognizer.ERROR_NO_MATCH -> "Samjha nahi. Thoda phir se boliye."
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer busy. Thodi der baad try kijiye."
            SpeechRecognizer.ERROR_SERVER -> "Speech service error."
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Kuch nahi suna. Dobara try kijiye."
            else -> "Unknown error: $error"
        }
        Log.w("MainActivity", "Speech error $error: $msg")
        speak(msg)
        statusText?.text = "Tap to Speak"
        isListening = false

        if (error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
            Handler(Looper.getMainLooper()).postDelayed({ startListening() }, 1000)
        }
    }

    private fun processCommand(command: String) {
        statusText?.text = "You said: $command"

        when {
            "hey jarvis" in command || "hello jarvis" in command -> {
                speak("Yes Sir, main tumhari kaise madad karoon?")
                startListening()
            }
            "scroll down" in command -> {
                speak("Scrolling down, Sir.")
                val service = JarvisAccessibilityService.instance
                if (service == null) {
                    speak("Accessibility service disabled. Main settings khol raha hoon, kripya enable kariye.")
                    startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
                } else {
                    service.performScrollDown()
                }
                startListening()
            }
            "scroll up" in command -> {
                speak("Scrolling up, Sir.")
                val service = JarvisAccessibilityService.instance
                if (service == null) {
                    speak("Accessibility service disabled. Main settings khol raha hoon, kripya enable kariye.")
                    startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
                } else {
                    service.performScrollUp()
                }
                startListening()
            }
            "go back" in command || "back" in command -> {
                speak("Going back, Sir.")
                val service = JarvisAccessibilityService.instance
                if (service == null) {
                    speak("Accessibility service disabled. Main settings khol raha hoon, kripya enable kariye.")
                    startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
                } else {
                    service.goBack()
                }
                startListening()
            }
            "go home" in command || "home" in command -> {
                speak("Going home, Sir.")
                val service = JarvisAccessibilityService.instance
                if (service == null) {
                    speak("Accessibility service disabled. Main settings khol raha hoon, kripya enable kariye.")
                    startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
                } else {
                    service.goHome()
                }
                startListening()
            }
            "call" in command -> {
                val name = command.replace("call ", "").trim()
                speak("Calling $name, Sir.")
                val callIntent = Intent(Intent.ACTION_CALL).apply {
                    data = Uri.parse("tel:$name")
                }
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                    startActivity(callIntent)
                } else {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CALL_PHONE), 2)
                }
                startListening()
            }
            "open" in command -> {
                val appName = command.replace("open ", "").trim()
                speak("Opening $appName, Sir.")
                openApp(appName)
            }
            "wi-fi" in command || "wifi" in command -> {
                speak("Settings khol raha hoon, Sir.")
                startActivity(Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
                startListening()
            }
            "type" in command -> {
                val textToType = command.replace("type ", "").trim()
                speak("Typing $textToType, Sir.")
                val service = JarvisAccessibilityService.instance
                if (service == null) {
                    speak("Accessibility service disabled. Main settings khol raha hoon, kripya enable kariye.")
                    startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
                } else {
                    service.typeText(textToType)
                }
                startListening()
            }
            "send" in command -> {
                speak("Sending message, Sir.")
                val service = JarvisAccessibilityService.instance
                if (service == null) {
                    speak("Accessibility service disabled. Main settings khol raha hoon, kripya enable kariye.")
                    startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
                } else {
                    service.goBack()
                }
                startListening()
            }
            else -> {
                speak("Samjha nahi Sir, phir se try karein.")
                startListening()
            }
        }
    }

    private fun openApp(appName: String) {
        val packageName = when (appName) {
            "whatsapp" -> "com.whatsapp"
            "youtube" -> "com.google.android.youtube"
            "camera" -> "com.android.camera2"
            "instagram" -> "com.instagram.android"
            "chrome" -> "com.android.chrome"
            else -> null
        }

        val intent = packageName?.let { packageManager.getLaunchIntentForPackage(it) }
        if (intent != null) {
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        } else {
            speak("App nahi mila, Sir.")
        }
        startListening()
    }

    private fun speak(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale("hi", "IN")
            speak("Sir, main online hoon. Aap hukum kijiye.")
        }
    }

    override fun onDestroy() {
        speechRecognizer?.destroy()
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}
