package com.example.snapquest

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import android.content.res.ColorStateList
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.snapquest.extensions.AppConstants
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldPath.documentId
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class CommentsBottomSheet : BottomSheetDialogFragment() {

    private var postId: String? = null
    private var adapter: CommentsAdapter? = null
    private val commentList = ArrayList<CommentData>()
    private var postComment: ImageView? = null
    private var recyclerView: RecyclerView? = null
    private var commentEditText: EditText? = null
    private var profileImage : ImageView? = null

    private var sharedPreferences : SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        postId = arguments?.getString("postId")

        if (postId.isNullOrEmpty()) {
            dismiss() // safely close bottom sheet
        }
    }
    companion object {
        fun newInstance(postId: String): CommentsBottomSheet {
            return CommentsBottomSheet().apply {
                arguments = Bundle().apply {
                    putString("postId", postId)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.bottom_sheet_comments, container, false)

        postComment = view.findViewById(R.id.btnPost)
        commentEditText = view.findViewById(R.id.etComment)
        recyclerView = view.findViewById<RecyclerView>(R.id.rvComments)
        profileImage = view.findViewById(R.id.imgCurrentUserProfile)

        val sharedPreferences = this.requireActivity().getSharedPreferences(AppConstants.perferenceName,
            Context.MODE_PRIVATE)

        profileImage?.let {
            Glide.with(requireContext())
                .load(sharedPreferences.getString("profileImage", ""))
                .placeholder(R.drawable.loading)
                .into(it)
        }

        loadsCommentData()

        commentEditText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val hasText = s?.trim()?.isNotEmpty() == true
                ImageViewCompat.setImageTintList(
                    postComment!!,
                    if (hasText) ColorStateList.valueOf(Color.parseColor("#FF5722")) else null
                )
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        postComment?.setOnClickListener {

            val comment = commentEditText?.text.toString().trim()
            if (comment.isEmpty()) return@setOnClickListener

            val sharedPreferences = this.requireActivity().getSharedPreferences(AppConstants.perferenceName,
                Context.MODE_PRIVATE)

            val userId = sharedPreferences.getString("USER_ID", "") ?: ""
            val name = sharedPreferences.getString("name", "") ?: ""
            val profileImage = sharedPreferences.getString("profileImage", "") ?: ""

            val commentData = CommentData(
                userId = userId,
                userName = name,
                userProfileImage = profileImage,
                commentText = comment,
                timestamp = com.google.firebase.Timestamp.now())

            postId?.let { documentPath -> FirebaseFirestore.getInstance().collection("snapPost").document(documentPath) }
                ?.collection("commentsList")?.add(commentData)?.addOnSuccessListener {

                postId?.let { documentPath -> FirebaseFirestore.getInstance().collection("snapPost").document(documentPath) }
                    ?.update("comments", FieldValue.increment(1))

                commentEditText?.text?.clear()
                ImageViewCompat.setImageTintList(postComment!!, null)

                    postId?.let { documentPath ->
                        FirebaseFirestore.getInstance()
                            .collection("snapPost")
                            .document(documentPath)
                            .get()
                            .addOnSuccessListener { postDoc ->
                                val postOwnerUid =
                                    postDoc.getString("uid") ?: return@addOnSuccessListener
                                val currentUid = sharedPreferences.getString("USER_UID", "") ?: ""

                                // don't notify yourself
                                if (currentUid != postOwnerUid) {
                                    FirebaseFirestore.getInstance()
                                        .collection("accounts")
                                        .document(postOwnerUid)
                                        .get()
                                        .addOnSuccessListener { accountDoc ->
                                            val fcmToken = accountDoc.getString("fcmToken")
                                                ?: return@addOnSuccessListener
                                            sendCommentNotification(
                                                requireContext(),
                                                fcmToken,
                                                name
                                            )
                                        }
                                }
                            }
                    }
            }
        }

        return view
    }

    private fun loadsCommentData(){

       adapter =  CommentsAdapter(commentList)
        postId?.let { FirebaseFirestore.getInstance().collection("snapPost").document(it) }?.
        collection("commentsList")?.orderBy("timestamp", Query.Direction.ASCENDING)?.
        addSnapshotListener { snapshot, _ ->

            if (snapshot == null) return@addSnapshotListener

            if (activity?.isFinishing?.not() == true) {
                commentList.clear()
                commentList.addAll(snapshot.toObjects(CommentData::class.java))

                recyclerView?.layoutManager = LinearLayoutManager(this.requireActivity())

                recyclerView?.adapter = adapter
            }
        }
    }

    private fun sendCommentNotification(context: Context, token: String, commenterName: String) {
        Thread {
            try {
                val googleCredentials = context.assets.open("service-account.json").use { inputStream ->
                    GoogleCredentials
                        .fromStream(inputStream)
                        .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))
                }
                googleCredentials.refresh()
                val accessToken = googleCredentials.accessToken.tokenValue

                val notificationJson = JSONObject().apply {
                    put("message", JSONObject().apply {
                        put("token", token)
                        put("notification", JSONObject().apply {
                            put("title", "New Comment! 💬")
                            put("body", "$commenterName Commented On Your Post")
                        })
                    })
                }

                val url =
                    URL("https://fcm.googleapis.com/v1/projects/family-album-2697b/messages:send")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Authorization", "Bearer $accessToken")
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

                connection.outputStream.use { outputStream ->
                    outputStream.write(notificationJson.toString().toByteArray())
                }

                val responseCode = connection.responseCode
                Log.d("FCM", "Comment notification response: $responseCode")

            } catch (e: Exception) {
                Log.e("FCM", "Error: ${e.message}")
            }
        }.start()
    }

}
