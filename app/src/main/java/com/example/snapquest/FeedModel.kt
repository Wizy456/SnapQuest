package com.example.snapquest

import com.google.firebase.Timestamp

data class FeedModel(
    var documentId: String? = null,
    val uid: String = "",
    val profileName: String = "",
    val profileImage: String = "",
    val photo: String = "",
    val description: String = "",
    val postDate: String = "",
    var isLiked: Boolean = false,
    var likes: Int = 0,
    val comments: Int = 0,
    val timeStamp: Timestamp? = null
)
