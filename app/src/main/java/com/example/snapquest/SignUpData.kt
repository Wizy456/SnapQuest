package com.example.snapquest

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.serialization.Serializable
import java.util.Date


data class SignUpData(var name:String, var email:String, var profileImage: String,
                      @ServerTimestamp
                      val createdAt: Date? = null, var currentDate: String? = null)
