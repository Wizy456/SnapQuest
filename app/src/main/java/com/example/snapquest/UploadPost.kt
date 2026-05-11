package com.example.snapquest

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class UploadPost(var uid: String? = null, var photo:String? = null, var profileName: String? = null,
                      var profileImage: String? = null, var description: String? = null, var likes: Int? = null,
                      var comments: Int? = null, var postDate: String? = null, @ServerTimestamp val timeStamp: Date? = null,
                      var likesList:ArrayList<UserProfile>? = null, var commentsList:ArrayList<UserProfile>? = null)


data class UserProfile(var uid: String? = null, var name: String? = null, var profileImage: String? = null,
                       var likes: String? = null, var comments: String? = null, @ServerTimestamp val timeStamp: Date? =null)
