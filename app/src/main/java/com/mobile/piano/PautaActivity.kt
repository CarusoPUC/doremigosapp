package com.mobile.piano

import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class PautaActivity : AppCompatActivity() {

    private var selectedBrush: ImageButton? = null
    private var selectedBrushId: Int? = null
    private var mediaPlayer: MediaPlayer? = null

    // Sequência correta para cada nota (nc1..nc8)
    private val correctSequence = listOf(1, 4, 2, 5, 6, 3, 7, 1)

    // Guardar o estado atual das notas (0 = não pintada, 1..7 = cor do pincel)
    private val currentNotes = IntArray(8) { 0 }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pauta)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.pauta_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupBackButton()
        setupBrushes()
        setupNotes()
    }

    private fun setupBackButton() {
        val btnVoltar = findViewById<ImageButton>(R.id.btnVoltar)
        btnVoltar?.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    /** Configura os pinceis (p1..p7) */
    private fun setupBrushes() {
        val brushes = listOf(
            R.id.brush_p1, R.id.brush_p2, R.id.brush_p3,
            R.id.brush_p4, R.id.brush_p5, R.id.brush_p6, R.id.brush_p7
        )

        brushes.forEachIndexed { index, brushId ->
            val brush = findViewById<ImageButton>(brushId)
            brush?.setOnClickListener {
                selectBrush(brush, index + 1) // index+1 → p1..p7
            }
        }
    }

    /** Anima o pincel escolhido e guarda como selecionado */
    private fun selectBrush(brush: ImageButton, brushNumber: Int) {
        val brushes = listOf(
            R.id.brush_p1, R.id.brush_p2, R.id.brush_p3,
            R.id.brush_p4, R.id.brush_p5, R.id.brush_p6, R.id.brush_p7
        )

        brushes.forEach {
            val b = findViewById<ImageButton>(it)
            b?.animate()?.translationY(0f)?.setDuration(200)?.start()
        }

        brush.animate().translationY(-50f).setDuration(200).start()

        selectedBrush = brush
        selectedBrushId = brushNumber
    }

    /** Configura notas cinzas para "pintar" quando clicadas */
    private fun setupNotes() {
        val notes = listOf(
            R.id.nc1, R.id.nc2, R.id.nc3, R.id.nc4,
            R.id.nc5, R.id.nc6, R.id.nc7, R.id.nc8
        )

        notes.forEachIndexed { index, noteId ->
            val note = findViewById<ImageView>(noteId)
            note?.setOnClickListener {
                paintNote(note, index)
            }
        }
    }

    /** Pinta a nota e atualiza o estado */
    private fun paintNote(note: ImageView, index: Int) {
        val color = when (selectedBrushId) {
            1 -> Color.parseColor("#FF7FB3") // rosa
            2 -> Color.parseColor("#2D89B7") // azul
            3 -> Color.parseColor("#FA8430") // laranja
            4 -> Color.parseColor("#FFC830") // amarelo
            5 -> Color.parseColor("#00A491") // verde água
            6 -> Color.parseColor("#B46EFB") // roxo
            7 -> Color.parseColor("#FF3E3E") // vermelho
            else -> null
        }

        if (selectedBrushId != null && color != null) {
            note.setColorFilter(color)
            currentNotes[index] = selectedBrushId!!  // guarda o pincel usado nesta nota

            // Agora só toca a nota; a lógica da vitória está dentro do playNoteSound
            playNoteSound(index, selectedBrushId!!)
        }
    }

    /** Toca os sons individuais conforme a cor/posição */
    private fun playNoteSound(noteIndex: Int, brushId: Int) {
        val soundRes = when {
            noteIndex == 0 && brushId == 1 -> R.raw.c3     // rosa na 1 → c3.wav
            noteIndex == 7 && brushId == 1 -> R.raw.c4     // rosa na 8 → c4.wav
            noteIndex == 1 && brushId == 4 -> R.raw.d      // amarelo na 2 → d.wav
            noteIndex == 2 && brushId == 2 -> R.raw.e      // azul na 3 → e.wav
            noteIndex == 3 && brushId == 5 -> R.raw.f      // verde na 4 → f.wav
            noteIndex == 4 && brushId == 6 -> R.raw.g      // roxo na 5 → g.wav
            noteIndex == 5 && brushId == 3 -> R.raw.a      // laranja na 6 → a.wav
            noteIndex == 6 && brushId == 7 -> R.raw.b      // vermelho na 7 → b.wav
            else -> null
        }

        soundRes?.let {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(this, it)

            // Verifica se a sequência está correta ANTES de iniciar a nota
            val sequenceCompleted = checkSequencePreview()

            mediaPlayer?.setOnCompletionListener { mp ->
                mp.release()
                if (sequenceCompleted) {
                    playVictorySound()
                }
            }

            mediaPlayer?.start()
        }
    }

    /** Apenas verifica se a sequência correta foi formada */
    private fun checkSequencePreview(): Boolean {
        val notesStr = currentNotes.joinToString("")
        val correctStr = correctSequence.joinToString("")
        return notesStr.contains(correctStr)
    }

    /** Toca o som de vitória */
    private fun playVictorySound() {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(this, R.raw.vitoria)
        mediaPlayer?.setOnCompletionListener {
            it.release()
        }
        mediaPlayer?.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
    }
}
