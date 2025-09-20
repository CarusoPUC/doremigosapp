package com.mobile.piano

import android.annotation.SuppressLint
import android.media.SoundPool
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var soundPool: SoundPool
    private val noteSoundMap = mutableMapOf<Int, Int>()

    // Mapeia os IDs das views para notas (todas as pretas com d.wav temporário)
    private val noteMap = mapOf(
        // Teclas brancas
        R.id.key_do to R.raw.c3,
        R.id.key_re to R.raw.d,
        R.id.key_mi to R.raw.e,
        R.id.key_fa to R.raw.f,
        R.id.key_sol to R.raw.g,
        R.id.key_la to R.raw.a,
        R.id.key_si to R.raw.b,
        R.id.key_dod to R.raw.c4,

        // Teclas pretas (placeholder d.wav)
        R.id.key_do_sus to R.raw.db,
        R.id.key_re_sus to R.raw.eb,
        R.id.key_fa_sus to R.raw.gb,
        R.id.key_sol_sus to R.raw.ab,
        R.id.key_la_sus to R.raw.bb
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        soundPool = SoundPool.Builder()
            .setMaxStreams(12)
            .build()

        // Carrega os sons
        noteMap.forEach { (viewId, resId) ->
            val soundId = soundPool.load(this, resId, 1)
            noteSoundMap[viewId] = soundId
        }

        setupKeys()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupKeys() {
        noteMap.keys.forEach { id ->
            val v = findViewById<View>(id)
            v?.setOnTouchListener { view, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        playNote(id)
                        view.isPressed = true
                        true
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        view.isPressed = false
                        true
                    }
                    else -> false
                }
            }
        }
    }

    private fun playNote(viewId: Int) {
        val soundId = noteSoundMap[viewId] ?: return
        soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
    }

    override fun onDestroy() {
        super.onDestroy()
        soundPool.release()
    }
}
