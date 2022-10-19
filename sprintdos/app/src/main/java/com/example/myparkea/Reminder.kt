package com.example.myparkea

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class Reminder : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.i(String(),"Recordatorio aaa")
        val intent = Intent(context,MapsActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val flag = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        val pendingIntent : PendingIntent = PendingIntent.getActivity(context, 0, intent, flag)

        val builder = context?.let {
            NotificationCompat.Builder(it, MapsActivity.MY_CHANNEL)
                .setSmallIcon(R.drawable.logo_color)
                .setContentTitle("El tiempo de su reserva ha termiando.")
                .setContentIntent(pendingIntent)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setAutoCancel(true)
        }
        val alarmSound: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        builder!!.setSound(alarmSound)

        with(context.let { NotificationManagerCompat.from(it) }) {
            this.notify(1, builder.build())
            Log.i(String(),"Recordatorio bbb")
        }
    }
}