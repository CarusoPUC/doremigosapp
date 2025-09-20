package com.seuprojeto.app

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.mobile.piano.MainActivity
import com.mobile.piano.R

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Clique Piano Virtual -> MainActivity
        val pianoVirtual = findViewById<LinearLayout>(R.id.btnPianoVirtual)
        pianoVirtual.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}
