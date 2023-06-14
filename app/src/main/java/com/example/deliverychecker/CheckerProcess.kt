package com.example.deliverychecker

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


class CheckerProcess : Service() {

    private var isAvailable: Boolean = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Do a periodic task
        val service = Executors.newSingleThreadScheduledExecutor()
        val handler = Handler(Looper.getMainLooper())
        service.scheduleAtFixedRate({
            handler.run {
                if(checkVit()) {
                    if (!isAvailable) {
                        isAvailable = true
                        sendNotification()
                    }
                } else isAvailable = false
            }
        }, 0, 15, TimeUnit.SECONDS);
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        throw UnsupportedOperationException("Not yet implemented")
    }

    private fun checkVit() : Boolean{
        val url = URL("https://market-delivery.yandex.ru/eats/v1/eats-catalog" +
                "/v1/brand/vkusnoitochka?" +
                "regionId=3&latitude=59.93656164451229&longitude=30.500139515596462");
        val http = url.openConnection() as HttpURLConnection
        http.requestMethod = "GET"
        BufferedReader(InputStreamReader(http.inputStream)).use { br ->
            var line: String?
            while (br.readLine().also { line = it } != null) {
                if (line?.contains("BRAND_NOT_FOUND") == true) return false
            }
        }
        return true
    }

    fun sendNotification(){
        val builder = NotificationCompat.Builder(this, MainActivity.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_vit_foreground)
            .setContentTitle("Доступна \"Вкусно и Точка\"")
            .setContentText("Ресторан \"Вкусно и Точка\" ждет заказов.")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setDefaults(Notification.DEFAULT_SOUND and Notification.DEFAULT_VIBRATE)

        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.notify(MainActivity.NOTIFICATION_ID, builder.build())
    }
}