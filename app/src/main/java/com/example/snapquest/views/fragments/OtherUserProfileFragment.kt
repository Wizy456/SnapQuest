package com.example.snapquest.views.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.snapquest.MyAdapter
import com.example.snapquest.R
import com.google.firebase.firestore.FirebaseFirestore

class OtherUserProfileFragment : AppCompatActivity() {
    private var rvPosts: RecyclerView? = null
    private var ivProfileImage: ImageView? = null
    private var tvName: TextView? = null
    private var adapter: MyAdapter? = null
    private var ivBack: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.fragment_other_user_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            WindowInsetsCompat.CONSUMED
        }

        rvPosts = findViewById(R.id.rvPosts)
        ivProfileImage = findViewById(R.id.profilePic)
        tvName = findViewById(R.id.profileName)
        ivBack = findViewById(R.id.ivBack)

        val userId = intent.extras?.getString("userId") ?: ""

        ivBack?.setOnClickListener {
            finish()
        }

        adapter = MyAdapter()
        adapter?.setOnItemClickListener { postId ->
            val bundle = Bundle()
            bundle.putString("postId", postId)
//            findNavController().navigate(R.id.action_otherUserProfileFragment_to_ProfilePostDetailFragment, bundle)
            val intent = Intent(this, ProfilePostDetailFragment::class.java)
            intent.putExtra("postId", postId)
            startActivity(intent)
        }

        rvPosts?.layoutManager = GridLayoutManager(this, 3)
        rvPosts?.addItemDecoration(GridSpacingDecoration(3, 2))
        rvPosts?.adapter = adapter

        fetchOtherUserProfile(userId)
        fetchOtherUserPosts(userId)
    }

    private fun fetchOtherUserProfile(userID: String) {

        FirebaseFirestore.getInstance().collection("accounts")
            .document(userID).get().addOnSuccessListener { doc ->
                val name = doc.getString("name")
                val profileImage = doc.getString("profileImage")

                tvName?.text = name

                Glide.with(this)
                    .load(profileImage)
                    .placeholder(R.drawable.name)
                    .into(ivProfileImage!!)
            }
    }

    private fun fetchOtherUserPosts(userId: String) {

        FirebaseFirestore.getInstance().collection("snapPost")
            .whereEqualTo("uid", userId)
            .get().addOnSuccessListener { snapshots ->
                adapter?.submitList(snapshots.documents)

            }.addOnFailureListener { error ->
                Toast.makeText(this, error.message, Toast.LENGTH_SHORT).show()
            }
    }
}