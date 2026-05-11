package com.example.snapquest

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class LikesAdapter(private val likesList: List<LikesDataModel>) :
    RecyclerView.Adapter<LikesAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: ImageView = itemView.findViewById(R.id.ivProfile)
        val userName: TextView = itemView.findViewById(R.id.tvName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_like_user, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val userlikes = likesList[position]

        holder.userName.text = userlikes.name

        Glide.with(holder.itemView.context).
        load(userlikes.profileImage).
        placeholder(R.drawable.loading).
        into(holder.profileImage)

    }

    override fun getItemCount(): Int {
        return likesList.size
    }
}