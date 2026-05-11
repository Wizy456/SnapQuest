package com.example.snapquest.extensions

import android.app.Activity
import android.app.AlertDialog
import com.example.snapquest.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun AlertDialog?.showLoading(boolean: Boolean){
    if (boolean){
        this?.show()
    }else{
        if (this?.isShowing == true){
            this.dismiss()
        }
    }
}
fun Activity.getCurrentDate(): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date())
}