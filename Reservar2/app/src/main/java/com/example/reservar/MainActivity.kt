package com.example.reservar

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog


class MainActivity : AppCompatActivity() {
    private lateinit var button : Button

    //Creando un AlertDialog
    private lateinit var builder : AlertDialog.Builder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button = findViewById(R.id.button)
        builder = AlertDialog.Builder(this)

        button.setOnClickListener{
            builder.setTitle("Reserva!")
                .setMessage("¿Quiere reservar ahora?")
                .setCancelable(true)
                .setPositiveButton("SI"){dialogInterface,it ->
                    Toast.makeText(this@MainActivity, "Boton SI funcionando",Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("NO"){dialogInterface,it->
                    Toast.makeText(this@MainActivity, "Boton No funcionando",Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Ayuda"){dialogInterface,it ->
                    Toast.makeText(this@MainActivity, "Boton ayuda oprimido",Toast.LENGTH_SHORT).show()
                }
                .show()
        }

    }
}