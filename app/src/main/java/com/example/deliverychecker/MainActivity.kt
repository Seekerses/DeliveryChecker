package com.example.deliverychecker

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Notification
import android.content.Context
import android.content.Intent
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


class MainActivity : AppCompatActivity() {

    companion object {
        const val NOTIFICATION_ID = 3221
        const val CHANNEL_ID = "delivery-checker-channel-id"
    }

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val image: ImageView = findViewById(R.mipmap.ic_vit_foreground)
        val serviceClass = CheckerProcess::class.java
        val intent = Intent(this, serviceClass)
        if (!isServiceRunning(serviceClass)) {
            startService(intent)
        }
        renderLogo(image)
    }

    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        // Loop through the running services
        for (service in activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                // If the service is running then return true
                return true
            }
        }
        return false
    }


    fun renderLogo(view: ImageView){
        if(!checkVit()) setUnavailable(view)
        else setAvailable(view)
    }

    private fun setUnavailable(view: ImageView){
        val matrix = ColorMatrix()
        matrix.setSaturation(0f) //0 means grayscale
        val cf = ColorMatrixColorFilter(matrix)
        view.colorFilter = cf
    }

    private fun setAvailable(view: ImageView){
        view.colorFilter = null
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
}