package com.example.snapquest.views

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.snapquest.R
import com.example.snapquest.extensions.LoadingDialog
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth

class ForgotScreen : AppCompatActivity() {
    private lateinit var email: EditText
    private lateinit var sendResetLinkBtn: MaterialButton
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.foget_password)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        
        // Initialize Loading Dialog
        LoadingDialog.createLoadingDialog(this)

        findViewById<ImageView>(R.id.goBack).setOnClickListener {
            finish()
        }

        email = findViewById(R.id.forgotEmailEditText)
        sendResetLinkBtn = findViewById(R.id.sendResetLink)

        sendResetLinkBtn.setOnClickListener {
            val emailText = email.text.toString().trim()

            if (emailText.isEmpty()) {
                email.error = "Please enter your email"
                email.requestFocus()
                return@setOnClickListener
            }

            sendPasswordReset(emailText)
        }
    }

    private fun sendPasswordReset(emailAddress: String) {
        LoadingDialog.showDialog()
        
        auth.sendPasswordResetEmail(emailAddress)
            .addOnCompleteListener { task ->
                LoadingDialog.dismiss()
                if (task.isSuccessful) {
                    Toast.makeText(
                        this,
                        "Reset link sent to your email",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                } else {
                    val errorMessage = task.exception?.message ?: "Failed to send reset link"
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
    }
}