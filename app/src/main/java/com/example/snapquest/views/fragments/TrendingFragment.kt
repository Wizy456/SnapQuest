package com.example.snapquest.views.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.snapquest.FeedAdapter
import com.example.snapquest.FeedModel
import com.example.snapquest.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class TrendingFragment : Fragment() {
    private var tvEmpty : TextView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_trending, null)

        tvEmpty = view.findViewById(R.id.tvEmpty)

        val recycleView = view.findViewById<RecyclerView>(R.id.exploreRecyclerView)

        recycleView.layoutManager = LinearLayoutManager(this.requireActivity())

        FirebaseFirestore.getInstance().collection("snapPost").
        get().addOnSuccessListener { snapshots ->

            val postList = ArrayList<FeedModel>()

            for (document in snapshots.documents){
                val post = document.toObject(FeedModel::class.java)
                if (post != null){
                    post.documentId = document.id
                    postList.add(post)
                }
            }
                val avgScore = postList.map {
                    it.likes + it.comments
                }.average()

                val trendingList = postList.filter {
                    (it.likes + it.comments) > avgScore
                }

                val sortedTrending = trendingList.sortedByDescending {
                    it.likes + it.comments
                }
            recycleView.adapter = FeedAdapter( sortedTrending) { userId ->

//                val bundle = Bundle()
//                bundle.putString("userId", userId)
////                findNavController().navigate(R.id.action_trendingFragment_to_otherUserProfileFragment, bundle)
                val intent = Intent(requireContext(), OtherUserProfileFragment::class.java)
                intent.putExtra("userId", userId)
                startActivity(intent)
            }

            tvEmpty?.visibility = if (postList.isEmpty()) View.VISIBLE else View.GONE
            recycleView.visibility = if (postList.isEmpty()) View.GONE else View.VISIBLE

        }.addOnFailureListener { error ->
            Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
        }
        return view
    }
}