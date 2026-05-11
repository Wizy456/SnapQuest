package com.example.snapquest.views

import android.content.Intent
import android.content.SharedPreferences
import android.media.Image
import android.os.Bundle
import android.os.Handler
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.layout.FirstBaseline
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.snapquest.R
import com.example.snapquest.ViewFeedActivity
import com.example.snapquest.extensions.AppConstants
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth

class SplashScreen : AppCompatActivity() {

    var sharedPreferences : SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash_screen)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sharedPreferences = getSharedPreferences(AppConstants.perferenceName, MODE_PRIVATE)


        Handler().postDelayed({

            val firebaseUser = FirebaseAuth.getInstance().currentUser
            val isLoggedIn = sharedPreferences?.getBoolean("isLoggedIn", false)


            // check both firebase and sharedpreferences
            val intent = if (firebaseUser != null && isLoggedIn == true) {
                Intent(this, ViewFeedActivity::class.java)
            } else {
                // clear sharedpreferences if firebase session is gone
                sharedPreferences?.edit()?.clear()?.apply()
                Intent(this, LoginActivity::class.java)
            }

            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()

        }, 2000)

    }


}