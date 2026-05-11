package com.example.snapquest.views

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.snapquest.R
import com.example.snapquest.SignUpData
import com.example.snapquest.ViewFeedActivity
import com.example.snapquest.extensions.AppConstants
import com.example.snapquest.extensions.LoadingDialog
import com.example.snapquest.extensions.getCurrentDate
import com.example.snapquest.extensions.showLoading
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignUPActivity : AppCompatActivity() {

    private var signUpButton: MaterialButton? = null
    private var nameEditText: EditText? = null
    private var emailEditText: EditText? = null
    private var passwordEditText: EditText? = null
    private var confirmPassEditText: EditText? = null
    private var auth: FirebaseAuth? = null

    private fun init() {
        LoadingDialog.createLoadingDialog(this)
        signUpButton = findViewById(R.id.signUpButton)
        nameEditText = findViewById(R.id.nameTextField)
        emailEditText = findViewById(R.id.emailTextfield)
        passwordEditText = findViewById(R.id.passwordTextField)
        confirmPassEditText = findViewById(R.id.cvConfirmPasswordTextField)
        auth = FirebaseAuth.getInstance()

    }
    private fun actions() {
        signUpButton?.setOnClickListener {
            val name = nameEditText?.text?.toString()?.trim()
            val email = emailEditText?.text?.toString()?.trim()
            val pass = passwordEditText?.text?.toString()?.trim()
            val confirmPass = confirmPassEditText?.text?.toString()?.trim()

            if (name?.isEmpty() == true) {
//                Toast.makeText(this, "Name is required!", Toast.LENGTH_SHORT).show()
                nameEditText?.error = "Name is required!"
            } else if (email?.isEmpty() == true) {
                emailEditText?.error = "Email is required!"
            } else if (pass?.isEmpty() == true) {
                passwordEditText?.error = "Password is required!"
            } else if (confirmPass?.isEmpty() == true) {
                confirmPassEditText?.error = "Confirm Password is Required!"
            } else if (passwordEditText?.text.toString() != confirmPassEditText?.text.toString()) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()

            } else {
                nameEditText?.error = null
                emailEditText?.error = null
                passwordEditText?.error = null
                confirmPassEditText?.error = null
                if (email != null && pass != null) {
                    LoadingDialog.showDialog()
                    auth?.createUserWithEmailAndPassword(email, pass)
                        ?.addOnCompleteListener { task ->
                            LoadingDialog.dismiss()
                            if (task.isSuccessful) {

                                Toast.makeText(this, "success", Toast.LENGTH_SHORT).show()

                                auth?.signInWithEmailAndPassword(email,pass)?.addOnCompleteListener { task ->
                                    if (task.isSuccessful){

                                        val signUpData = SignUpData(
                                            nameEditText?.text.toString(),
                                            emailEditText?.text.toString(),
                                            "",
                                            null,
                                            currentDate = getCurrentDate())


                                        FirebaseFirestore.getInstance().collection("accounts").
                                        document(task.result.user?.uid ?: "").set(signUpData).
                                        addOnCompleteListener {

                                                Toast.makeText(this, "success", Toast.LENGTH_SHORT).show()
                                                val uid = task.result.user?.uid
                                                val sharedPreferences = getSharedPreferences(AppConstants.perferenceName, Context.MODE_PRIVATE)
                                                sharedPreferences.edit().putString("USER_UID", uid).apply()
                                                sharedPreferences.edit().putString("name",name).apply()
                                                sharedPreferences.edit().putBoolean("isLoggedIn", true).apply()

                                                val intent = Intent(this, ViewFeedActivity::class.java)
                                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                startActivity(intent)

                                            }.addOnFailureListener { error ->
                                                Toast.makeText(this, error.message, Toast.LENGTH_SHORT).show()

                                            }
                                    }
                                }?.addOnFailureListener { error ->
                                    Toast.makeText(this, error.message, Toast.LENGTH_SHORT).show()
                                }

                            }
                        }?.addOnFailureListener { error ->
                            Toast.makeText(this, error.message, Toast.LENGTH_SHORT).show()
                            LoadingDialog.dismiss()
                        }

                }

            }

        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_upactivity)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        init()
        actions()
        findViewById<ImageView>(R.id.ivBack).setOnClickListener {
            finish()
        }

    }
}