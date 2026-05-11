package com.example.snapquest.views.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.result.ActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.snapquest.R
import android.net.Uri
import android.graphics.drawable.Drawable
import android.media.Image
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.core.util.Pools
import androidx.core.view.isGone
import com.example.snapquest.UploadPost
import com.example.snapquest.extensions.AppConstants
import com.example.snapquest.extensions.LoadingDialog
import com.example.snapquest.extensions.showLoading
import com.google.android.material.button.MaterialButton
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import org.checkerframework.checker.units.qual.Length
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class UploadPostFragment : Fragment() {

    var selectedImage : ImageView? = null

    var tapToSelectImage : ImageView? = null

    var tapToSelect : TextView? = null
    var cancelBtn : ImageView? = null

    var uploadPhoto : MaterialButton? = null

    var captionEditText : EditText? = null

    var selectedImageUri : Uri? = null
    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                selectedImageUri = uri
                selectedImage?.setImageURI(uri)
                tapToSelectImage?.visibility = View.GONE
                tapToSelect?.visibility = View.GONE
                cancelBtn?.visibility = View.VISIBLE

            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_upload_post, null)

        LoadingDialog.createLoadingDialog(this.requireActivity())
        selectedImage = view.findViewById<ImageView>(R.id.selectedImage)
        tapToSelectImage = view.findViewById(R.id.selectImageBtn)
        tapToSelect = view.findViewById<TextView>(R.id.tapToSelectText)
        cancelBtn = view.findViewById<ImageView>(R.id.btnCancelImage)
        uploadPhoto = view.findViewById<MaterialButton>(R.id.uploadPhoto)
        captionEditText = view.findViewById<EditText>(R.id.captionEditText)


        tapToSelectImage?.setOnClickListener {
            imagePickerLauncher.launch("image/*")

        }

        cancelBtn?.setOnClickListener {
            selectedImage?.setImageResource(R.drawable.upload_placeholder)
            tapToSelectImage?.visibility = View.VISIBLE
            tapToSelect?.visibility = View.VISIBLE
            cancelBtn?.visibility = View.GONE
            selectedImageUri = null

        }

        uploadPhoto?.setOnClickListener {

            if (selectedImageUri == null) {
                Toast.makeText(this.requireActivity(), "Select an image first", Toast.LENGTH_SHORT).show()
            }else {
//                uploadImage(selectedImageUri!!)
                checkDailyLimit()
            }
        }

        return  view

    }

    private fun uploadImage(imageUri: Uri) {

        LoadingDialog.showDialog()

        val storageRef = FirebaseStorage.getInstance().reference
        val fileRef = storageRef.child("postImages/${System.currentTimeMillis()}.jpg")

        fileRef.putFile(imageUri)
            .addOnSuccessListener {

                fileRef.downloadUrl.addOnSuccessListener { downloadUri ->

                    val imageUrl = downloadUri.toString()

                    val sharedPreferences = this.requireActivity()
                        .getSharedPreferences(AppConstants.perferenceName, Context.MODE_PRIVATE)


                    sharedPreferences.getString("USER_UID", "")?.let { documentId ->

                        val name = sharedPreferences.getString("name","")
                        val profileImage = sharedPreferences.getString("profileImage","")

                        val postData = UploadPost(
                            uid = documentId, profileName = name, profileImage = profileImage,
                            description = captionEditText?.text.toString(), postDate = System.currentTimeMillis().toString(), likes = 0, comments = 0,
                            likesList = null, commentsList = null, photo = imageUrl)

                        FirebaseFirestore.getInstance()
                            .collection("snapPost")
                            .document()
                            .set(postData).addOnSuccessListener {

                                val todayDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
                                FirebaseFirestore.getInstance().collection("accounts").document(documentId)
                                    .update("currentDate", todayDate)

                                LoadingDialog.dismiss()

                                Toast.makeText(requireContext(), "Upload Successfully", Toast.LENGTH_SHORT).show()
                                resetUI()

                            }.addOnFailureListener {
                                LoadingDialog.dismiss()
                                Toast.makeText(requireContext(), "Upload Failed", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
            }
    }

    private fun checkDailyLimit(){
        val sharedPreferences = this.requireActivity().getSharedPreferences(AppConstants.perferenceName,
            Context.MODE_PRIVATE)

        val uid = sharedPreferences.getString("USER_UID","") ?: return

        val todayDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())

        FirebaseFirestore.getInstance().collection("accounts").document(uid)
            .get().addOnSuccessListener { doc ->
                val currentDate = doc.getString("currentDate") ?: ""

                if (currentDate == todayDate){
                    Toast.makeText(requireContext(), "You Can only Upload One Photo per day!", Toast.LENGTH_SHORT).show()
                    resetUI()
                }else {
                    uploadImage(selectedImageUri!!)
                }

        }.addOnFailureListener { error ->
                Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun resetUI() {

        selectedImage?.setImageResource(R.drawable.upload_placeholder)

        captionEditText?.text?.clear()

        selectedImageUri = null

        tapToSelectImage?.visibility = View.VISIBLE

        tapToSelect?.visibility = View.VISIBLE

        cancelBtn?.visibility = View.GONE

    }

}



