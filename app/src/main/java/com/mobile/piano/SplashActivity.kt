package com.mobile.piano

import android.content.Intent
import android.os.Bundle
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.mobile.piano.R
import com.mobile.piano.HomeActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val videoView = findViewById<VideoView>(R.id.videoView)

        // Coloque o vídeo na pasta res/raw, ex: splash_video.mp4
        val videoUri = "android.resource://$packageName/${R.raw.splash_video}"
        videoView.setVideoPath(videoUri)

        videoView.setOnCompletionListener {
            // Fade out
            videoView.animate()
                .alpha(0f)
                .setDuration(500)
                .withEndAction {
                    startActivity(Intent(this, HomeActivity::class.java))
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish()
                }
                .start()
        }
        videoView.start()
    }
}
