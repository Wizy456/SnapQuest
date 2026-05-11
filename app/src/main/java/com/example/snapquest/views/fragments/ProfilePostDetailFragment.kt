package com.example.snapquest.views.fragments

import androidx.appcompat.app.AlertDialog
import android.os.Bundle
import android.view.ContextThemeWrapper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.snapquest.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfilePostDetailFragment : AppCompatActivity() {
    private var ivProfileImage: ImageView? = null
    private var tvProfileName: TextView? = null
    private var tvPostDate: TextView? = null
    private var ivPostImage: ImageView? = null
    private var tvLikesCount: TextView? = null
    private var tvCommentsCount: TextView? = null
    private var tvDescription: TextView? = null
    private var ivBack: ImageView? = null
    private var ivOptions: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.fragment_profile_post_detail)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            WindowInsetsCompat.CONSUMED
        }
        ivProfileImage = findViewById(R.id.ivProfileImage)
        tvProfileName = findViewById(R.id.tvUsername)
        tvPostDate = findViewById(R.id.tvDate)
        ivPostImage = findViewById(R.id.ivPostImage)
        tvLikesCount = findViewById(R.id.tvLikesCount)
        tvCommentsCount = findViewById(R.id.tvCommentsCount)
        tvDescription = findViewById(R.id.tvDescription)
        ivBack = findViewById(R.id.ivBack)
        ivOptions = findViewById(R.id.ivOptions)

        // get postId passed from profile grid
        val postId = intent.extras?.getString("postId") ?: ""

        // back button
        ivBack?.setOnClickListener { finish() }

        // fetch post from Firestore using postId
        fetchPost(postId)

        ivOptions?.setOnClickListener {
            showOptionsMenu(it, postId)
        }
    }

    private fun fetchPost(postId: String) {

        val currentUid = FirebaseAuth.getInstance().currentUser?.uid

        FirebaseFirestore.getInstance()
            .collection("snapPost")
            .document(postId)
            .get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) return@addOnSuccessListener

                val postOwnerUid = doc.getString("uid") ?: ""

                // show options only if current user owns the post
                if (currentUid == postOwnerUid) {
                    ivOptions?.visibility = View.VISIBLE
                } else {
                    ivOptions?.visibility = View.GONE
                }

                val profileImage = doc.get("profileImage")?.toString() ?: ""
                val name = doc.getString("profileName") ?: ""
                val photo = doc.get("photo")?.toString() ?: ""
                val likes = doc.getLong("likes") ?: 0
                val comments = doc.getLong("comments") ?: 0
                val description = doc.getString("description") ?: ""
                val postDate = doc.getString("postDate") ?: ""

                Glide.with(this).load(profileImage).centerCrop().into(ivProfileImage!!)

                Glide.with(this)
                    .load(photo)
                    .centerCrop()
                    .into(ivPostImage!!)

                tvProfileName?.text = name
                tvPostDate?.text = postDate
                tvLikesCount?.text = "$likes likes"
                tvCommentsCount?.text = "$comments comments"
                tvDescription?.text = description
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, error.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun showOptionsMenu(view: View, postId: String) {
        val wrapper = ContextThemeWrapper(this, R.style.PopupMenuStyle)
        val popup = PopupMenu(wrapper, view)
        popup.menu.add("Edit")
        popup.menu.add("Delete")

        popup.setOnMenuItemClickListener { item ->
            when (item.title) {
                "Edit" -> showEditDialog(postId)
                "Delete" -> showDeleteDialog(postId)
            }
            true
        }
        popup.show()
    }

    private fun showEditDialog(postId: String) {
        val input = EditText(this)
        input.hint = "New description"
        input.setText(tvDescription?.text)
        input.setSelection(input.text.length)
        input.setSelectAllOnFocus(true)
        input.setPadding(50, 30, 50, 30)
        input.setTextColor(android.graphics.Color.BLACK)
        input.setHintTextColor(android.graphics.Color.GRAY)
//      input.setBackgroundColor(android.graphics.Color.parseColor("#F5F5F5"))

        val dialog = AlertDialog.Builder(this, R.style.CustomAlertDialog)
            .setTitle("Edit Description")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val newDescription = input.text.toString().trim()
                if (newDescription.isEmpty()) return@setPositiveButton

                FirebaseFirestore.getInstance()
                    .collection("snapPost")
                    .document(postId)
                    .update("description", newDescription)
                    .addOnSuccessListener {
                        tvDescription?.text = newDescription
                        Toast.makeText(this, "Updated!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { error ->
                        Toast.makeText(this, error.message, Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancel", null)
            .create()
        input.requestFocus()
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        dialog.show()
    }

    private fun showDeleteDialog(postId: String) {
        AlertDialog.Builder(this, R.style.CustomAlertDialog)
            .setTitle("Delete Post")
            .setMessage("Are you sure you want to delete this post?")
            .setPositiveButton("Delete") { _, _ ->

                FirebaseFirestore.getInstance()
                    .collection("snapPost")
                    .document(postId)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Post deleted!", Toast.LENGTH_SHORT).show()
                        finish() // go back to profile
                    }
                    .addOnFailureListener { error ->
                        Toast.makeText(this, error.message, Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
