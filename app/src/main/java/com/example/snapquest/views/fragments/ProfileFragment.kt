package com.example.snapquest.views.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.snapquest.MyAdapter
import com.example.snapquest.R
import com.example.snapquest.extensions.AppConstants
import com.example.snapquest.extensions.showLoading
import com.example.snapquest.views.LoginActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage

class ProfileFragment : Fragment() {
    private var profileName : TextView? = null
    private var editProfile: ImageView? = null
    private var profieImage: ImageView? = null
    private var selectedImageUri: Uri? = null
    private var popUpBtn: ImageView? = null
    private var editName: ImageView? = null
    private var updateEditText : EditText? = null
    private var updateButton : MaterialButton? = null
    private var closeBtn : ImageView? = null
    var loadingDialog: AlertDialog? = null

    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                selectedImageUri = uri
                profieImage?.setImageURI(uri)
                uploadProfileImage(uri)
            }
        }

    private fun uploadProfileImage(imageUri: Uri) {

        val storageRef = FirebaseStorage.getInstance().reference
        val fileRef = storageRef.child("profileImages/${System.currentTimeMillis()}.jpg")

        loadingDialog?.showLoading(true)

        fileRef.putFile(imageUri)
            .addOnSuccessListener { task ->
                fileRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    val imageUrl = downloadUri.toString()

                    val sharedPreferences = this.requireActivity().getSharedPreferences(AppConstants.perferenceName, Context.MODE_PRIVATE)

                    sharedPreferences.getString("USER_UID" , "")?.let { documentId ->

                        val hashMap : HashMap<String , String> = hashMapOf(
                            "profileImage" to imageUrl)

                        FirebaseFirestore.getInstance()
                            .collection("accounts")
                            .document(documentId)
                            .set(hashMap , SetOptions.merge())
                    }?.addOnSuccessListener {

                        sharedPreferences.edit().putString("profileImage", imageUrl).apply()

                        val uid = sharedPreferences.getString("USER_UID", "") ?: ""

                        FirebaseFirestore.getInstance()
                            .collection("snapPost")
                            .whereEqualTo("uid", uid)
                            .get()
                            .addOnSuccessListener { snapshots ->
                                for (doc in snapshots.documents) {
                                    doc.reference.update("profileImage", imageUrl)
                                }
                            }
                        Toast.makeText(this.requireActivity(), "Upload Successfully", Toast.LENGTH_SHORT).show()
                        loadingDialog?.dismiss()
                    }

                }
            }
            .addOnFailureListener {
                Toast.makeText(this.requireActivity(), "Upload failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_profile, null)

        createLoadingDialog()
        profileName = view.findViewById(R.id.profileName)
        editProfile = view.findViewById(R.id.editProfileCamera)
        profieImage = view.findViewById(R.id.profilePic)
        popUpBtn = view.findViewById(R.id.popBtn)
        editName = view.findViewById(R.id.editName)

        val sharedPreferences = this.requireActivity()
            .getSharedPreferences(AppConstants.perferenceName, Context.MODE_PRIVATE)

        val name = sharedPreferences.getString("name","")
        profileName?.text = name

        val imageUrl = sharedPreferences.getString("profileImage", "") ?: ""

        if (imageUrl.isNotEmpty()){

            profieImage?.let { Glide.with(this).load(imageUrl).placeholder(R.drawable.loading).into(it) }
        }


        popUpBtn?.setOnClickListener { view ->
            val wrapper = ContextThemeWrapper(requireContext(), R.style.PopupMenuStyle)
            val popup = PopupMenu(wrapper, view)
            popup.menuInflater.inflate(R.menu.popup_menu, popup.menu)

            popup.show()
            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.item1 -> {
                        Toast.makeText(
                            this.requireActivity(),
                            "Logout Succesfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        logoutUser()
                        true
                    }

                    else -> false
                }

            }
        }

        editProfile?.setOnClickListener {

            imagePicker.launch("image/*")

        }

        editName?.setOnClickListener {

            val bottomSheetDialog = BottomSheetDialog(requireContext())

            bottomSheetDialog.window?.setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

            val view = layoutInflater.inflate(R.layout.profile_bottom_sheet, null)

            updateEditText = view.findViewById<EditText>(R.id.updateEditText)
            updateButton = view.findViewById<MaterialButton>(R.id.updateButton)
            closeBtn = view.findViewById<ImageView>(R.id.closeBottom)

            closeBtn?.setOnClickListener {
                bottomSheetDialog.dismiss()
            }

            updateButton?.setOnClickListener {

                val name = updateEditText?.text.toString()

                val sharedPreferences = this.requireActivity()
                    .getSharedPreferences(AppConstants.perferenceName, Context.MODE_PRIVATE)

                sharedPreferences.getString("USER_UID", "")?.let { documentId ->

                    loadingDialog?.show()

                    FirebaseFirestore.getInstance().collection("accounts").document(documentId)
                        .update("name", name)
                        .addOnSuccessListener {
                            loadingDialog?.dismiss()
                            Toast.makeText(requireContext(), "Name updated", Toast.LENGTH_SHORT).show()
                            sharedPreferences.edit().putString("name",name).apply()

                            val uid = sharedPreferences.getString("USER_UID", "") ?: ""

                            FirebaseFirestore.getInstance()
                                .collection("snapPost")
                                .whereEqualTo("uid", uid)
                                .get()
                                .addOnSuccessListener { snapshots ->
                                    for (doc in snapshots.documents) {
                                        doc.reference.update("profileName", name)
                                    }
                                }

                                    bottomSheetDialog.dismiss()
                        }.addOnFailureListener {
                            Toast.makeText(requireContext(), "Failed to update", Toast.LENGTH_SHORT)
                                .show()
                        }
                }
            }

            bottomSheetDialog.setContentView(view)
            bottomSheetDialog.show()
            // 🔹 Auto open keyboard
            updateEditText?.requestFocus()
            bottomSheetDialog.window?.setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
            )
        }

        val recyclerView: RecyclerView = view.findViewById(R.id.profileRecycleView)

        recyclerView.layoutManager = GridLayoutManager(this.requireActivity(), 3)

        recyclerView.addItemDecoration(GridSpacingDecoration(3, 5))

        val adapter = MyAdapter()
        adapter.setOnItemClickListener{ postId ->
//            val bundle = Bundle()
//            bundle.putString("postId", postId)
//            findNavController().navigate(
//                R.id.action_profileFragment_to_ProfilePostDetailFragment, bundle
//            )
            val intent = Intent(requireContext(), ProfilePostDetailFragment::class.java)
            intent.putExtra("postId", postId)
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        val uid = FirebaseAuth.getInstance().currentUser?.uid

        FirebaseFirestore.getInstance().collection("snapPost")
            .whereEqualTo("uid", uid)
            .get().addOnSuccessListener { snapshots ->

                adapter.submitList(snapshots.documents)

            }.addOnFailureListener { error ->
                Toast.makeText(this.requireActivity(), error.message, Toast.LENGTH_SHORT).show()
            }

        return view

    }

    private fun logoutUser() {

        FirebaseAuth.getInstance().signOut()

        val sharedPreferences = requireContext().getSharedPreferences(AppConstants.perferenceName, Context.MODE_PRIVATE)

        sharedPreferences.edit().putBoolean("isLoggedIn", false).apply()

        val intent = Intent(this.requireActivity(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)

    }

    private fun createLoadingDialog(){
        val builder = AlertDialog.Builder(this.requireActivity())
        val view = layoutInflater.inflate(R.layout.loading_dialog, null)
        builder.setView(view)
        builder.setCancelable(false)
        loadingDialog = builder.create()
        loadingDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

}
