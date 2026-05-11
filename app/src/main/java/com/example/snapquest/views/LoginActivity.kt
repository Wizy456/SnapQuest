package com.example.snapquest.views

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.example.snapquest.R
import com.example.snapquest.ViewFeedActivity
import com.example.snapquest.extensions.AppConstants
import com.example.snapquest.extensions.LoadingDialog
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private var auth : FirebaseAuth? = null
    private var sharedPreferences : SharedPreferences? = null
    private var loginButton : MaterialButton? = null
    private var emailEditText : EditText? = null
    private var passwordEditText : EditText? = null
    private lateinit var credentialManager: CredentialManager
    private var forgotPasswordText : TextView? = null
    private var signInGoogleBtn : ImageView? = null

    private fun init() {
        LoadingDialog.createLoadingDialog(this)
        sharedPreferences = getSharedPreferences(AppConstants.perferenceName , MODE_PRIVATE)
        loginButton = findViewById(R.id.signInButton)
        emailEditText = findViewById(R.id.cvEmailTextField)
        passwordEditText = findViewById(R.id.cvPasswordTextField)
        forgotPasswordText = findViewById(R.id.forgotPasswordLabel)
        signInGoogleBtn = findViewById(R.id.signInGoogle)
        auth = FirebaseAuth.getInstance()
    }
    private fun signUPButton() {
        val myButton = findViewById<TextView>(R.id.SignUPText)
        myButton.setOnClickListener {
            val intent = Intent(this, SignUPActivity::class.java)
            startActivity(intent)
        }
    }
    private fun actions() {

        loginButton?.setOnClickListener {

            if (emailEditText?.text?.isEmpty() == true){
                emailEditText?.error = "Email is Required"
            } else if (passwordEditText?.text?.isEmpty() == true){
                passwordEditText?.error = "Password is Required"
            }else {
                if (emailEditText != null && passwordEditText != null) {
                    LoadingDialog.showDialog()
                    auth?.signInWithEmailAndPassword(
                        emailEditText?.text.toString(),
                        passwordEditText?.text.toString()
                    )
                        ?.addOnCompleteListener { task ->
                            LoadingDialog.dismiss()
                            if (task.isSuccessful) {

                                task.result.user?.uid?.let { uid ->

                                    FirebaseFirestore.getInstance().collection("accounts")
                                        .document(uid).get().addOnSuccessListener { document ->

                                            if (document.exists()) {

                                                val name = document.getString("name")
                                                val email = document.getString("email")
                                                val profileImage =
                                                    document.getString("profileImage") ?: ""
                                                val createdAt =
                                                    document.getTimestamp("createdAt")?.toDate()
                                                        ?.toString() // CHANGED THIS LINE

                                                val sharedPreferences = this.getSharedPreferences(
                                                    AppConstants.perferenceName,
                                                    Context.MODE_PRIVATE
                                                )

                                                sharedPreferences.edit().putString("USER_UID", uid)
                                                    .apply()
                                                sharedPreferences.edit().putString("name", name)
                                                    .apply()
                                                sharedPreferences.edit().putString("email", email)
                                                    .apply()
                                                sharedPreferences.edit()
                                                    .putString("profileImage", profileImage).apply()
                                                sharedPreferences.edit()
                                                    .putString("createdAt", createdAt).apply()
                                                sharedPreferences.edit()
                                                    .putBoolean("isLoggedIn", true).apply()

                                                Toast.makeText(this, "success", Toast.LENGTH_SHORT)
                                                    .show()
                                                val intent =
                                                    Intent(this, ViewFeedActivity::class.java)
                                                startActivity(intent)
                                                finish()
                                            }
                                        }.addOnFailureListener { error ->
                                            Toast.makeText(this, error.message, Toast.LENGTH_SHORT)
                                                .show()
                                            LoadingDialog.dismiss()
                                        }

                                }

                            }

                        }?.addOnFailureListener { error ->
                            Toast.makeText(this, error.message, Toast.LENGTH_SHORT).show()
                            LoadingDialog.dismiss()
                        }
                }
            }
        }

        forgotPasswordText?.setOnClickListener {
            val intent = Intent(this, ForgotScreen::class.java)
            startActivity(intent)
        }

        signInGoogleBtn?.setOnClickListener {
            signInWithGoogle()
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        FirebaseAuth.getInstance()
            .signInWithCredential(credential)
            .addOnSuccessListener { authResult ->

                val user = authResult.user ?: return@addOnSuccessListener

                val uid = user.uid
                val name = user.displayName ?: ""
                val email = user.email ?: ""
                val profileImage = user.photoUrl?.toString() ?: ""

                val sharedPreferences = getSharedPreferences(AppConstants.perferenceName, Context.MODE_PRIVATE)
                sharedPreferences.edit()
                    .putString("USER_UID", uid)
                    .putString("name", name)
                    .putString("email", email)
                    .putString("profileImage", profileImage)
                    .putBoolean("isLoggedIn", true)
                    .apply()

                // save to Firestore accounts collection
                val userData = hashMapOf(
                    "name" to name,
                    "email" to email,
                    "profileImage" to profileImage,
                    "uid" to uid
                )

                FirebaseFirestore.getInstance()
                    .collection("accounts")
                    .document(uid)
                    .set(userData, SetOptions.merge())

                Toast.makeText(this, "Login Successfully", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, ViewFeedActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Login failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
    private fun signInWithGoogle() {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(getString(R.string.client_id))
            .build()

        val request = GetCredentialRequest(listOf(googleIdOption))

        lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(
                    request = request,
                    context = this@LoginActivity
                )
                val credential = result.credential
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                firebaseAuthWithGoogle(googleIdTokenCredential.idToken)
            } catch (e: GetCredentialException) {
                Toast.makeText(this@LoginActivity, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        signUPButton()
        init()
        actions()

        credentialManager = CredentialManager.create(this)

    }

}