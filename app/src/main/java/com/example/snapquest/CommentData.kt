package com.example.snapquest

import com.google.firebase.Timestamp

data class CommentData( val userId: String = "", val userName: String = "", val userProfileImage: String = "",
                        val commentText: String = "", val timestamp: Timestamp? = null )
