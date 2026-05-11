package com.example.snapquest.views

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.snapquest.R
import com.example.snapquest.ViewFeedActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DailyReminderWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val uid = FirebaseAuth.getInstance().currentUser?.uid

        // if user not logged in skip
        if (uid == null) return Result.success()

        // check if user posted today
        val today = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())

        val snapshot = FirebaseFirestore.getInstance()
            .collection("accounts")
            .document(uid)
            .get()
            .await()

        val currentDate = snapshot.getString("currentDate") ?: ""

        // if user didn't post today → send reminder
        if (currentDate != today) {
            showReminderNotification()
        }

        return Result.success()
    }
    private fun showReminderNotification() {
        val channelId = "reminder_channel"

        val intent = Intent(applicationContext, ViewFeedActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_like)
            .setContentTitle("Daily Reminder 📸")
            .setContentText("You haven't posted anything today! Share your moment!")
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Daily Reminder",
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }

        manager.notify(1, builder.build())
    }
}