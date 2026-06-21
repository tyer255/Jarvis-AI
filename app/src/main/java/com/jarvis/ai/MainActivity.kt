package com.jarvis.ai

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var statusText: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 96, 48, 48)
        }

        val title = TextView(this).apply {
            text = "JARVIS AI"
            textSize = 28f
        }

        val statusView = TextView(this).also { statusText = it }.apply {
            text = "Offline"
            textSize = 16f
        }

        val button = Button(this).apply {
            text = "Initialize Jarvis"
            setOnClickListener { initJarvis() }
        }

        layout.addView(title)
        layout.addView(statusView)
        layout.addView(button)
        setContentView(layout)

        tts = TextToSpeech(this, this)
    }

    private fun initJarvis() {
        statusText?.text = "Online — Ready for commands"
        tts?.speak("Sir, main online hoon. Aap hukum kijiye.", TextToSpeech.QUEUE_FLUSH, null, "jarvis_online")
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
