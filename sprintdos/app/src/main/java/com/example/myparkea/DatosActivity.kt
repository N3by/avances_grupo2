package com.example.myparkea

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class DatosActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_datos)

        auth = Firebase.auth

        val currentUser = auth.currentUser

        getUserProfile()
        getProviderData()
    }

    private fun getUserProfile() {
        val user = Firebase.auth.currentUser
        user?.let {
            // Name, email address, and profile photo Url
            val name = user.displayName
            val email = user.email
            val photoUrl = user.photoUrl

            // Check if user's email is verified
            val emailVerified = user.isEmailVerified

            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getToken() instead.
            val uid = user.uid

            val txtemail : TextView = findViewById(R.id.datos_email)
            val txtnombre : TextView = findViewById(R.id.datos_apellido)
            val txtapellido : TextView = findViewById(R.id.datos_nombre)
            val txtproveedor : TextView = findViewById(R.id.datos_proveedor)
            val txtid : TextView = findViewById(R.id.datos_token)

            txtemail.text = email
            txtnombre.text = name
            txtapellido.text = emailVerified.toString()
            txtproveedor.text = FirebaseAuthProvider.PROVIDER_ID
            txtid.text = uid
        }
    }

    private fun getProviderData() {
        val user = Firebase.auth.currentUser
        user?.let {
            for (profile in it.providerData) {
                // Id of the provider (ex: google.com)
                val providerId = profile.providerId

                // UID specific to the provider
                val uid = profile.uid

                // Name, email address, and profile photo Url
                val name = profile.displayName
                val email = profile.email
                val photoUrl = profile.photoUrl

                val txtemail : TextView = findViewById(R.id.datosp_email)
                val txtnombre : TextView = findViewById(R.id.datosp_nombre)
                val txtproveedor : TextView = findViewById(R.id.datosp_proveedor)
                val txtid : TextView = findViewById(R.id.datosp_id)

                txtemail.text = email
                txtnombre.text = name
                txtproveedor.text = providerId
                txtid.text = uid
            }
        }
    }
}