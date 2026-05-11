package com.example.snapquest

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.snapquest.extensions.AppConstants
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Locale

class FeedAdapter(private val list: List<FeedModel>, private val onUserClick: (String) -> Unit) :
    RecyclerView.Adapter<FeedAdapter.ViewHolder>() {
    fun showLikedUsersBottomSheet(postId: String, context: Context) {

        val bottomSheetDialog = BottomSheetDialog(context, R.style.BottomSheetStyle)
        val view = LayoutInflater.from(context).inflate(R.layout.like_bottom_sheet, null)
        bottomSheetDialog.setContentView(view)

        val recyclerView = view.findViewById<RecyclerView>(R.id.rvLikedUsers)

        val likesList = ArrayList<LikesDataModel>()

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = LikesAdapter(likesList)


        FirebaseFirestore.getInstance()
            .collection("snapPost")
            .document(postId)
            .collection("likesList")
            .orderBy("timeStamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                val users =
                    snapshot.documents.mapNotNull { it.toObject(LikesDataModel::class.java) }

                val adapter = LikesAdapter(users)
                recyclerView.layoutManager = LinearLayoutManager(context)
                recyclerView.adapter = adapter
            }

        // Make it expand fully
        bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetDialog.behavior.skipCollapsed = true

        bottomSheetDialog.show()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val username: TextView = view.findViewById(R.id.txtUsername)
        val profile: ImageView = view.findViewById(R.id.imgProfile)
        val post: ImageView = view.findViewById(R.id.imgPost)
        val caption: TextView = view.findViewById(R.id.txtCaption)
        val postDate: TextView = view.findViewById(R.id.txtDate)
        val likeBtn: ImageView = view.findViewById(R.id.likeButton)
        val likes: TextView = view.findViewById(R.id.tvLikeCount)

        val commentBtn: ImageView = view.findViewById(R.id.commentButton)
        val comments: TextView = view.findViewById(R.id.commentCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_feed, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        // moved outside click listener
        val sharedPreferences = holder.itemView.context
            .getSharedPreferences(AppConstants.perferenceName, Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("USER_UID", "") ?: ""

        // Firebase check block added
        item.documentId?.let { documentPath ->
            FirebaseFirestore.getInstance()
                .collection("snapPost")
                .document(documentPath)
                .collection("likesList")
                .whereEqualTo("userId", userId)
                .limit(1)
                .get()
                .addOnSuccessListener { snapshot ->
                    item.isLiked = !snapshot.isEmpty
                    if (item.isLiked) {
                        holder.likeBtn.setImageResource(R.drawable.ic_like_filled)
                    } else {
                        holder.likeBtn.setImageResource(R.drawable.ic_like)
                        holder.likeBtn.clearColorFilter()
                    }
                }
        }
        holder.username.text = item.profileName
        holder.caption.text = item.description
        holder.likes.text = "${item.likes}"
        holder.comments.text = "${item.comments}"

        val timestamp = item.timeStamp

        if (timestamp != null) {
            val date = timestamp.toDate()
            val formatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            holder.postDate.text = formatter.format(date)
        }

        Glide.with(holder.itemView.context).load(item.profileImage).placeholder(R.drawable.loading)
            .into(holder.profile)

        Glide.with(holder.itemView.context).load(item.photo).placeholder(R.drawable.loading)
            .into(holder.post)

        holder.post.setOnClickListener {
            val intent = android.content.Intent(holder.itemView.context, com.example.snapquest.views.ImageFullScreenActivity::class.java)
            intent.putExtra("IMAGE_URL", item.photo)
            holder.itemView.context.startActivity(intent)
        }

        holder.profile.setOnClickListener {
            onUserClick(item.uid)
        }

        holder.username.setOnClickListener {
            onUserClick(item.uid)
        }

        holder.likeBtn.setOnClickListener {

            val sharedPreferences = holder.itemView.context
                .getSharedPreferences(AppConstants.perferenceName, Context.MODE_PRIVATE)

            val name = sharedPreferences.getString("name", "") ?: ""
            val profileImage = sharedPreferences.getString("profileImage", "") ?: ""

            val likesData = LikesDataModel(
                userId = userId,
                name = name,
                profileImage = profileImage
            )

            item.documentId?.let { documentPath ->
                FirebaseFirestore.getInstance().collection("snapPost")
                    .document(documentPath)
            }?.collection("likesList")
                ?.whereEqualTo("userId", userId)
                ?.limit(1)
                ?.get()
                ?.addOnCompleteListener {
                    if (it.isSuccessful) {
                        if (it.result.documents.isEmpty()) {

                            val postRef = item.documentId?.let { documentPath ->
                                FirebaseFirestore.getInstance().collection("snapPost")
                                    .document(documentPath)
                            }
                            postRef?.update("likes", item.likes.plus(1))

                            postRef?.collection("likesList")?.add(likesData)

                            list[position].isLiked = true
                            list[position].likes = list[position].likes.plus(1)
                            notifyItemChanged(position)

                            // get post owner's fcmToken and send notification
                            if (userId != item.uid) {
                            Toast.makeText(holder.itemView.context, "Step 1: Like clicked", Toast.LENGTH_SHORT).show()

                            FirebaseFirestore.getInstance()
                                    .collection("accounts")
                                    .document(item.uid) // uid of post owner
                                    .get()
                                    .addOnSuccessListener { doc ->

                                        val fcmToken =
                                            doc.getString("fcmToken") ?: return@addOnSuccessListener
                                        if (fcmToken.isEmpty()) {
                                            return@addOnSuccessListener
                                        }

                                        val likerName =
                                            sharedPreferences.getString("name", "") ?: ""
                                        sendLikeNotification(
                                            holder.itemView.context,
                                            fcmToken,
                                            likerName
                                        )
                                        Toast.makeText(holder.itemView.context, "Step 5: notification sent!", Toast.LENGTH_SHORT).show()

                                    }.addOnFailureListener { error ->
                                    Toast.makeText(holder.itemView.context, "Failed: ${error.message}", Toast.LENGTH_SHORT).show()

                                }
                            }

                        } else {

                            val postRef = item.documentId?.let { documentPath ->
                                FirebaseFirestore.getInstance().collection("snapPost")
                                    .document(documentPath)
                            }
                            postRef?.update("likes", item.likes.minus(1))

                            postRef?.collection("likesList")?.document(it.result.documents[0].id)
                                ?.delete()

                            list[position].isLiked = false
                            list[position].likes = list[position].likes.minus(1)

                            notifyItemChanged(position)
                        }

                    }
                }
                ?.addOnFailureListener {
                }
        }

        holder.likes.setOnClickListener {
            val postId = item.documentId ?: return@setOnClickListener
            showLikedUsersBottomSheet(postId, holder.itemView.context)
        }

        holder.commentBtn.setOnClickListener {
            val postId = item.documentId ?: return@setOnClickListener

            CommentsBottomSheet.newInstance(postId).show(
                (holder.itemView.context as AppCompatActivity)
                    .supportFragmentManager,
                "CommentsBottomSheet"
            )
        }

    }

    override fun getItemCount(): Int {
        return list.size
    }

    private fun sendLikeNotification(context: Context, token: String, likerName: String) {
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
                            put("title", "New Like! ❤️")
                            put("body", "$likerName Liked Your Post")
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

                val outputStream = connection.outputStream
                outputStream.write(notificationJson.toString().toByteArray())
                outputStream.flush()
                outputStream.close()

                val responseCode = connection.responseCode
                Log.d("FCM", "Response code: $responseCode")

                val responseStream = if (responseCode == 200) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }
                val responseMessage = java.io.BufferedReader(
                    java.io.InputStreamReader(responseStream)
                ).readText()
                Log.d("FCM", "Response: $responseMessage")

            } catch (e: Exception) {
                Log.e("FCM", "Error: ${e.message}")
            }
        }.start()
    }
}
