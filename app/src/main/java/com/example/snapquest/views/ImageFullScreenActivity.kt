package com.example.snapquest.views

import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.snapquest.R
import com.google.android.material.button.MaterialButton

class ImageFullScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply fade animation for activity entry
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_image_full_screen)

        // Ensure background is solid black to prevent colored edges
        window.setBackgroundDrawableResource(android.R.color.black)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val imageUrl = intent.getStringExtra("IMAGE_URL")
        val imageView = findViewById<ImageView>(R.id.fullScreenImageView)
        val btnClose = findViewById<MaterialButton>(R.id.btnClose)

        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(imageUrl)
                .into(imageView)
        }

        btnClose.setOnClickListener {
            onBackPressed()
        }
    }

    override fun finish() {
        super.finish()
        // Apply fade animation for activity exit
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}
