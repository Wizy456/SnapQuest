package com.example.snapquest

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.DocumentSnapshot


class MyAdapter : RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    private var posts: List<DocumentSnapshot> = emptyList()
    private var onItemClick: ((String) -> Unit)? = null // add this

    fun setOnItemClickListener(listener: (String) -> Unit) { // add this
        onItemClick = listener
    }

    fun submitList(newPosts: List<DocumentSnapshot>) {
        posts = newPosts
        notifyDataSetChanged()
}
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val myUplImages : ImageView = itemView.findViewById(R.id.titleImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_list,parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val doc = posts[position]
        val imageUrl = doc.get("photo")?.toString() ?: ""

        Glide.with(holder.myUplImages).
        load(imageUrl).
        centerCrop().
        placeholder(R.drawable.loading).
        into(holder.myUplImages)

        holder.itemView.setOnClickListener {
//            val postId = doc.id
//            val bundle = Bundle()
//            bundle.putString("postId", postId)
//            holder.itemView.findNavController().
//            navigate(R.id.action_profileFragment_to_ProfilePostDetailFragment, bundle)

            onItemClick?.invoke(doc.id)

        }
    }

    override fun getItemCount(): Int {
    return posts.size
    }

}