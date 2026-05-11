package com.example.snapquest.views.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.snapquest.FeedAdapter
import com.example.snapquest.FeedModel
import com.example.snapquest.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.auth.User
import com.google.firebase.firestore.toObject


class HomeFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_home, null)

        val recycleView = view.findViewById<RecyclerView>(R.id.feedRecyclerView)

        recycleView.layoutManager = LinearLayoutManager(this.requireActivity())

        FirebaseFirestore.getInstance().collection("snapPost").
        orderBy("timeStamp", Query.Direction.DESCENDING).
        get().addOnSuccessListener { snapshots ->

            val postList = ArrayList<FeedModel>()

            for (document in snapshots.documents){
                val post = document.toObject(FeedModel::class.java)
                if (post!= null){
                    post.documentId = document.id
                    postList.add(post)
                }
            }
            recycleView.adapter = FeedAdapter( postList) { userID ->
//                val bundle = Bundle()
//                bundle.putString("userId", userID)
//                findNavController().navigate(R.id.action_homeFragment_to_otherUserProfileFragment, bundle)

                val intent = Intent(requireContext(), OtherUserProfileFragment::class.java)
                intent.putExtra("userId", userID)
                startActivity(intent)
            }
        }

        return view
    }
}