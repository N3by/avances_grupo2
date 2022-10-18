package com.example.notificacion

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity()  {
    companion object {
        const val channelId = "com.parkea"
    }

    private lateinit var notificationManager: NotificationManager
    lateinit var builder: NotificationCompat.Builder
    private lateinit var time : String
    var hms = "30:00"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val noti : Button = findViewById(R.id.noti)

        createChannel()

        noti.setOnClickListener {
            alert1()
        }
    }

    @SuppressLint("InflateParams")
    private fun alert1() {
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
            initializeTimerTask("30")
            createTimerNotification()
            createReminder()
            alert2()
        }

        noBtn.setOnClickListener {
            myDialog.dismiss()
        }
    }

    @SuppressLint("InflateParams")
    private fun alert2() {
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

    private fun initializeTimerTask(time: String?) {

        object : CountDownTimer(time!!.toLong() * 1000 * 60, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val f: NumberFormat = DecimalFormat("00")
                val min = millisUntilFinished / 60000 % 60
                val sec = millisUntilFinished / 1000 % 60
                hms = f.format(min).toString() + ":" + f.format(sec)
                raiseNotification(builder, hms)
            }

            override fun onFinish() {
                hms = "00:00"
            }
        }.start()
    }

    private fun createChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "MySuperChannel",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Test Notification"
            }

            val notificationManager : NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun createTimerNotification() {

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(this,MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val flag = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        val pendingIntent : PendingIntent = PendingIntent.getActivity(this, 0, intent, flag)

        val myTime = (Calendar.getInstance().timeInMillis) + 1800000
        val format = SimpleDateFormat("HH:mm")
        time = format.format(myTime)

        builder = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Reserva Parqueadero")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Tiempo restante para finalizar la reserva: $hms \nEl tiempo de su reserva termina a las $time."))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setColor(resources.getColor(android.R.color.holo_blue_dark))
            .setOnlyAlertOnce(true)
            .setTimeoutAfter(1800000)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setDefaults(Notification.DEFAULT_VIBRATE)
            .setPriority(NotificationCompat.PRIORITY_MAX)

        with(NotificationManagerCompat.from(this)) {
            notify(1, builder.build())
        }
    }

    private fun raiseNotification(b: NotificationCompat.Builder, hms: String) {
        b.setStyle(NotificationCompat.BigTextStyle().bigText("Tiempo restante para finalizar la reserva: $hms \nEl tiempo de su reserva termina a las $time."))
        b.setOngoing(true)

        notificationManager.notify(1, b.build())
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    fun createReminder() {
        val i =  Intent(this,Reminder::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this,0,i,0)
        val a = getSystemService(ALARM_SERVICE) as AlarmManager
        val myTime = (Calendar.getInstance().timeInMillis) + 1800000
        a.set(AlarmManager.RTC_WAKEUP,myTime,pendingIntent)
    }
}