package com.example.snapquest.views

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.snapquest.R
import com.google.firebase.firestore.DocumentSnapshot

class UserSearchAdapter( private val onUserClick: (String) -> Unit
): RecyclerView.Adapter<UserSearchAdapter.ViewHolder>() {

    private var users: List<DocumentSnapshot> = emptyList()

    fun submitList(newUsers: List<DocumentSnapshot>) {
        users = newUsers
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivAvatar: ImageView = itemView.findViewById(R.id.ivAvatar)
        val tvUsername: TextView = itemView.findViewById(R.id.tvUsername)
        val tvBio: TextView = itemView.findViewById(R.id.tvBio)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_search, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val doc = users[position]

        holder.tvUsername.text = doc.getString("name")
        holder.tvBio.text = doc.getString("bio") ?: ""

        Glide.with(holder.ivAvatar)
            .load(doc.getString("profileImage"))
            .circleCrop()
            .placeholder(R.drawable.loading)
            .into(holder.ivAvatar)

        holder.itemView.setOnClickListener {
            onUserClick(doc.id) // pass userId
        }
    }

    override fun getItemCount() = users.size
}