package com.example.notificacion

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity()  {
    companion object {
        const val MY_CHANNEL = "myChannel"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val noti : Button = findViewById(R.id.noti)

        createChannel()

        noti.setOnClickListener {
            alert1()
        }
    }

    private fun alert1() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Hola")
        builder.setMessage("Bhsgdkha sahgdasdgahsgd dkgashdgak jhsdghasdg")
        builder.setPositiveButton(android.R.string.ok) { _, _ -> alert2() }
        builder.setNegativeButton("Cancelar",null)
        builder.show()
    }

    @SuppressLint("InflateParams")
    private fun alert2() {
        val dialogBinding = layoutInflater.inflate(R.layout.custom_alert_1,null)
        val myDialog = Dialog(this)

        myDialog.setContentView(dialogBinding)
        myDialog.setCancelable(false)

        myDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        myDialog.show()

        val yesBtn : Button = dialogBinding.findViewById(R.id.bnt_alert1_yes)
        val noBtn : Button = dialogBinding.findViewById(R.id.bnt_alert1_no)

        yesBtn.setOnClickListener {
            myDialog.dismiss()
            createNotification()
            alert3()
        }

        noBtn.setOnClickListener {
            myDialog.dismiss()
        }
    }

    @SuppressLint("InflateParams")
    private fun alert3() {
        val dialogBinding = layoutInflater.inflate(R.layout.custom_alert_2,null)
        val myDialog = Dialog(this)

        myDialog.setContentView(dialogBinding)
        myDialog.setCancelable(false)

        myDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        myDialog.show()

        val yesBtn : Button = dialogBinding.findViewById(R.id.bnt_alert2_yes)

        yesBtn.setOnClickListener {
            myDialog.dismiss()
        }
    }

    private fun createChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                MY_CHANNEL,
                "MySuperChannel",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "NoSeQueVaAqui"
            }

            val notificationManager : NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun createNotification() {

        val intent = Intent(this,MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        var myTime = (Calendar.getInstance().timeInMillis) + 1800000
        val format = SimpleDateFormat("HH:mm")
        val time = format.format(myTime)

        val flag = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        val pendingIntent : PendingIntent = PendingIntent.getActivity(this, 0, intent, flag)

        val builder = NotificationCompat.Builder(this,MY_CHANNEL)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Titulo de la notificacion")
            .setContentText("El tiempo de su reserva termina a las $time")
            //.setStyle(NotificationCompat.BigTextStyle().bigText("Texto de la notificacion.\nClic para mas info\nEl tiempo de su areserva es tantos minutos y segundos"))
            .setContentIntent(pendingIntent)
            .setColor(resources.getColor(android.R.color.holo_blue_dark))
            .setSilent(true)
            .setUsesChronometer(true)
            .setTimeoutAfter(1800000)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)

        with(NotificationManagerCompat.from(this)) {
            notify(1, builder.build())
        }
    }
}