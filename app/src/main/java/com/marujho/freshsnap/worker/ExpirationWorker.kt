package com.marujho.freshsnap.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.marujho.freshsnap.R
import com.marujho.freshsnap.data.model.FirestoreUser
import com.marujho.freshsnap.data.model.UserProduct
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import kotlin.math.ceil

@HiltWorker
class ExpirationWorker @AssistedInject constructor(
    @Assisted context : Context,
    @Assisted params : WorkerParameters,
    private val firestore : FirebaseFirestore,
    private val auth : FirebaseAuth
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        return try{
            val userId = auth.currentUser?.uid ?: return Result.failure()

            val userSnapshot = firestore.collection("users").document(userId).get().await()
            val user = userSnapshot.toObject(FirestoreUser::class.java)
            val daysToNotify = user?.daysToNotify ?: 3

            val productSnapshot = firestore.collection("users")
                .document(userId)
                .collection("products")
                .get()
                .await()
            val products = productSnapshot.toObjects(UserProduct::class.java)
            val productsAboutToExpire = mutableListOf<String>()

            val now = System.currentTimeMillis()
            products.forEach{product ->
                product.expirationDate?.let{expDate ->
                    val diffInMillis = expDate - now
                    val daysRemaining = TimeUnit.MILLISECONDS.toDays(diffInMillis)
                    if(daysRemaining in 0..daysToNotify.toLong()){
                        productsAboutToExpire.add(product.name)
                    }
                }
            }
            if (productsAboutToExpire.isNotEmpty()){
                sendNotification(productsAboutToExpire,daysToNotify)
            }
            Result.success()
        }catch (e:Exception){
            Result.retry()
        }
    }

    private fun sendNotification(products : List<String>, daysSetting : Int){
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "freshsnap_expiration_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = NotificationChannel(
                channelId,
                "Caducidad de Alimentos",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }
        val title = "Estan apunto de caducar alimentos"
        val content = if(products.size == 1){
            "${products[0]} está a punto de caducar"
        }else{
            "${products.size} alimentos caducan en $daysSetting días."
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_freshsnap_logo)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(1001,notification)
    }
}