package com.example.snapquest.extensions

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import com.example.snapquest.R

object LoadingDialog {
    private var loadingDialog : AlertDialog? = null
    fun createLoadingDialog(context: Activity){
        val builder = AlertDialog.Builder(context)
        val view = context.layoutInflater.inflate(R.layout.loading_dialog, null)
        builder.setView(view)
        builder.setCancelable(false)
        loadingDialog = builder.create()
        loadingDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }
    fun showDialog() {
        loadingDialog?.show()
    }

    fun dismiss() {
        loadingDialog?.dismiss()
    }
}