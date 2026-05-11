package com.example.snapquest

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.snapquest.FeedAdapter.ViewHolder
import com.google.android.material.imageview.ShapeableImageView
import java.text.SimpleDateFormat
import java.util.Locale

class CommentsAdapter(private val commentsList : List<CommentData>):
    RecyclerView.Adapter<CommentsAdapter.CommentViewHolder>() {

    class CommentViewHolder(view : View): RecyclerView.ViewHolder(view){

        val imgProfile : ShapeableImageView = view.findViewById(R.id.imgCommentProfile)
        val commentUser : TextView = view.findViewById(R.id.commentUsername)
        val commentText : TextView = view.findViewById(R.id.commentText)
        val timeText : TextView = view.findViewById(R.id.commentTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val item = commentsList[position]
        holder.commentUser.text = item.userName
        holder.commentText.text = item.commentText

        Glide.with(holder.itemView.context).
        load(item.userProfileImage).
        placeholder(R.drawable.loading).
        into(holder.imgProfile)
    }

    override fun getItemCount(): Int {

        return commentsList.size
    }

}