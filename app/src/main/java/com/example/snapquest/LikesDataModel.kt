package com.example.snapquest

import android.widget.ImageView
import com.google.firebase.Timestamp

data class LikesDataModel(val userId : String = "",  val name: String = "",
                          val profileImage : String = "", val timeStamp : Timestamp? = null)
