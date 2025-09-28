package com.mobile.piano

import android.content.Intent
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import com.bumptech.glide.request.target.Target


class PautaActivity : AppCompatActivity() {

    private var selectedBrush: ImageButton? = null
    private var selectedBrushId: Int? = null
    private var mediaPlayer: MediaPlayer? = null

    // Sequência correta para cada nota (nc1..nc8)
    private val correctSequence = listOf(1, 4, 2, 5, 6, 3, 7, 1)

    // Guardar o estado atual das notas (0 = não pintada, 1..7 = cor do pincel)
    private val currentNotes = IntArray(8) { 0 }
    // Salvamos os drawables originais para restaurar depois do glow
    private lateinit var btnSom: ImageButton // 🔹 botão de som
    private lateinit var tvSom: TextView // 🔹 botão de som
    private val originalDrawables = mutableMapOf<Int, Drawable?>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pauta)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.pauta_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        Glide.with(this)
            .asGif()
            .load(R.raw.confeti)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .preload()

        setupBackButton()
        setupBrushes()
        setupNotes()
        btnSom = findViewById(R.id.btnSom)
        tvSom = findViewById(R.id.tvSom)
        btnSom.visibility = ImageButton.INVISIBLE // começa invisível
        tvSom.visibility = TextView.INVISIBLE // começa invisível

        // 🔹 Ao clicar no botão, toca o som de vitória e anima as notas
        btnSom.setOnClickListener {
            playVictorySound()
        }
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
            brush?.drawable?.let { originalDrawables[brushId] = cloneDrawable(it) }
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

        // restaura todos
        brushes.forEach {
            val b = findViewById<ImageButton>(it)
            b?.animate()?.translationY(0f)?.setDuration(200)?.scaleX(1.0f)?.scaleY(1.0f)?.start()
            restoreOriginalDrawable(b)
        }

        // anima o selecionado
        brush.animate().translationY(-0f).setDuration(200).scaleX(1.25f).scaleY(1.25f).start()

        val neonColor = when (brushNumber) {
            1 -> Color.parseColor("#FF7FB3")
            2 -> Color.parseColor("#2D89B7")
            3 -> Color.parseColor("#FA8430")
            4 -> Color.parseColor("#FFC830")
            5 -> Color.parseColor("#00A491")
            6 -> Color.parseColor("#B46EFB")
            7 -> Color.parseColor("#FF3E3E")
            else -> Color.WHITE
        }

        // aplica glow no pincel
        applyNeonGlow(brush, neonColor)

        selectedBrush = brush
        selectedBrushId = brushNumber
    }

    /** Configura notas */
    private fun setupNotes() {
        val notes = listOf(
            R.id.nc1, R.id.nc2, R.id.nc3, R.id.nc4,
            R.id.nc5, R.id.nc6, R.id.nc7, R.id.nc8
        )

        notes.forEachIndexed { index, noteId ->
            val note = findViewById<ImageView>(noteId)
            note?.drawable?.let { originalDrawables[noteId] = cloneDrawable(it) }
            note?.setOnClickListener { paintNote(note, index) }
        }
    }

    /** Pinta a nota com cor e glow */
    private fun paintNote(note: ImageView, index: Int) {
        val color = when (selectedBrushId) {
            1 -> Color.parseColor("#FF7FB3")
            2 -> Color.parseColor("#2D89B7")
            3 -> Color.parseColor("#FA8430")
            4 -> Color.parseColor("#FFC830")
            5 -> Color.parseColor("#00A491")
            6 -> Color.parseColor("#B46EFB")
            7 -> Color.parseColor("#FF3E3E")
            else -> null
        }

        if (selectedBrushId != null && color != null) {
            note.setColorFilter(color) // mantém a imagem e aplica cor
            applyNeonGlow(note, color) // adiciona glow
            currentNotes[index] = selectedBrushId!!
            playNoteSound(index, selectedBrushId!!)

            checkSequence() // 🔹 verifica se sequência está completa
        }
    }

    /** Toca os sons individuais */
    private fun playNoteSound(noteIndex: Int, brushId: Int) {
        val soundRes = when {
            noteIndex == 0 && brushId == 1 -> R.raw.c3
            noteIndex == 7 && brushId == 1 -> R.raw.c4
            noteIndex == 1 && brushId == 4 -> R.raw.d
            noteIndex == 2 && brushId == 2 -> R.raw.e
            noteIndex == 3 && brushId == 5 -> R.raw.f
            noteIndex == 4 && brushId == 6 -> R.raw.g
            noteIndex == 5 && brushId == 3 -> R.raw.a
            noteIndex == 6 && brushId == 7 -> R.raw.b
            else -> R.raw.wrong
        }

        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(this, soundRes)

        if (soundRes != R.raw.wrong) {
            val sequenceCompleted = checkSequencePreview()
            mediaPlayer?.setOnCompletionListener {
                it.release()
                if (sequenceCompleted) playVictorySound()
            }
        } else {
            mediaPlayer?.setOnCompletionListener { it.release() }
        }

        mediaPlayer?.start()
    }

    private fun checkSequencePreview(): Boolean {
        val notesStr = currentNotes.joinToString("")
        val correctStr = correctSequence.joinToString("")
        return notesStr.contains(correctStr)
    }

    private fun playVictorySound() {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(this, R.raw.vitoria)
        mediaPlayer?.setOnCompletionListener { it.release() }
        mediaPlayer?.start()
        playVictoryAnimation()
    }

    private fun playVictoryAnimation() {
        val notes = listOf(
            R.id.nc1, R.id.nc2, R.id.nc3, R.id.nc4,
            R.id.nc5, R.id.nc6, R.id.nc7, R.id.nc8
        )

// 🔹 Mostra o GIF de confete (usa cache e reduz tamanho para o dispositivo)
        val confettiView = findViewById<ImageView>(R.id.confettiView)
        confettiView.visibility = View.VISIBLE

        val metrics: DisplayMetrics = resources.displayMetrics
        val screenW = metrics.widthPixels
        val screenH = metrics.heightPixels

        Glide.with(this)
            .asGif()
            .load(R.raw.confeti)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .override(screenW, screenH) // pede uma resolução próxima da tela para reduzir custo
            .listener(object : RequestListener<GifDrawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<GifDrawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    // falhou no load → esconde a view para não bloquear UI
                    confettiView.post { confettiView.visibility = View.GONE }
                    return false
                }

                override fun onResourceReady(
                    resource: GifDrawable,
                    model: Any,
                    target: Target<GifDrawable>,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    // toca só uma vez e registra callback para esconder a view ao terminar
                    try {
                        resource.setLoopCount(1)
                        resource.registerAnimationCallback(object : Animatable2Compat.AnimationCallback() {
                            override fun onAnimationEnd(drawable: Drawable?) {
                                confettiView.post { confettiView.visibility = View.GONE }
                            }
                        })
                    } catch (t: Throwable) {
                        // fallback: some depois de um tempo seguro se algo falhar
                        confettiView.postDelayed({ confettiView.visibility = View.GONE }, 2500)
                    }
                    // retorna false para permitir que o Glide coloque o drawable na ImageView
                    return false
                }
            })
            .into(confettiView)

        // 🔹 Continua a animação das notas
        correctSequence.forEachIndexed { i, brushId ->
            val note = findViewById<ImageView>(notes[i])
            val color = when (brushId) {
                1 -> Color.parseColor("#FF7FB3")
                2 -> Color.parseColor("#2D89B7")
                3 -> Color.parseColor("#FA8430")
                4 -> Color.parseColor("#FFC830")
                5 -> Color.parseColor("#00A491")
                6 -> Color.parseColor("#B46EFB")
                7 -> Color.parseColor("#FF3E3E")
                else -> Color.WHITE
            }

            note?.postDelayed({
                applyNeonGlow(note, color)
                note.animate().scaleX(1.08f).scaleY(1.08f).setDuration(140).withEndAction {
                    note.animate().scaleX(1f).scaleY(1f).setDuration(140).start()
                }.start()
            }, i * 180L)
        }
    }

    private fun checkSequence() {
        if (currentNotes.contentEquals(correctSequence.toIntArray())) {
            btnSom.visibility = ImageButton.VISIBLE
            tvSom.visibility = TextView.VISIBLE
        } else {
            btnSom.visibility = ImageButton.INVISIBLE
            tvSom.visibility = TextView.INVISIBLE
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
    }

    /** 🔹 Glow simples via Paint + setLayerType */
    private fun applyNeonGlow(view: ImageView?, color: Int, blurRadius: Float = 25f) {
        if (view == null) return
        view.setLayerType(ImageView.LAYER_TYPE_SOFTWARE, null)
        val paint = Paint()
        paint.setShadowLayer(blurRadius, 0f, 0f, color)
        view.setPadding(0, 0, 0, 0) // espaço p/ o glow
    }

    private fun restoreOriginalDrawable(view: ImageView?) {
        if (view == null) return
        val original = originalDrawables[view.id]
        if (original != null) view.setImageDrawable(cloneDrawable(original))
        view.clearColorFilter()
        view.setLayerType(ImageView.LAYER_TYPE_NONE, null)
    }

    private fun cloneDrawable(drawable: Drawable?): Drawable? {
        if (drawable == null) return null
        return try {
            val cs = drawable.constantState
            cs?.newDrawable()?.mutate() ?: drawable.mutate()
        } catch (e: Exception) {
            drawable.mutate()
        }
    }
}
