package com.jarvis.ai

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var statusText: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val button = Button(this).apply {
            text = "Initialize Jarvis"
            setOnClickListener { initJarvis() }
        }
        statusText = TextView(this).apply {
            text = "Waiting for initialization..."
        }
        
        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            addView(button)
            addView(statusText)
        }
        setContentView(layout)

        tts = TextToSpeech(this, this)
    }

    private fun initJarvis() {
        statusText?.text = "Jarvis is online. Waiting for commands."
        tts?.speak("Sir, main online hoon. Aap hukum kijiye.", TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.US
        }
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}
