package com.example.myparkea

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private var TAG = "MainActivity"
    private lateinit var auth: FirebaseAuth
    private val Google_Sign_In = 100
    private var db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {

        setTheme(R.style.SplashTheme)

        auth = Firebase.auth

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val chkRecuerdame : CheckBox = findViewById(R.id.chk_Recuerdame)

        chkRecuerdame.setOnClickListener {
            if(chkRecuerdame.isChecked){
                val preferences = getSharedPreferences("checkbox", MODE_PRIVATE)
                val editor = preferences.edit()
                editor.putString("remember", "true")
                editor.apply()

            }else if(!chkRecuerdame.isChecked) {
                val preferences = getSharedPreferences("checkbox", MODE_PRIVATE)
                val editor = preferences.edit()
                editor.putString("remember", "true")
                editor.apply()
            }
        }

        setUp()
    }

    public override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if(currentUser != null){
            val preferences = getSharedPreferences("checkbox", MODE_PRIVATE)
            val usuario = preferences.getString("remember", "")
            if(usuario == "true"){
                showHome()
            }
        }
    }

    private fun setUp() {
        val btnLogin : Button = findViewById(R.id.btn_Login)
        val btnLoginGoogle : Button = findViewById(R.id.btn_LoginGoogle)
        val txtRegistro : TextView = findViewById(R.id.txt_Registro)

        val txtEmail : TextInputEditText = findViewById(R.id.txt_EmailL_et)
        val txtPassword : TextInputEditText = findViewById(R.id.txt_PasswordL_et)

        val tilEmail : TextInputLayout = findViewById(R.id.txt_EmailL)
        val tilPassword : TextInputLayout = findViewById(R.id.txt_PasswordL)

        btnLogin.setOnClickListener {

            if(txtEmail.text.isNullOrBlank()) tilEmail.helperText="Introduce un Email" else tilEmail.helperText=null
            if(txtPassword.text.isNullOrBlank()) tilPassword.helperText="Introduce una contraseña" else tilPassword.helperText=null

            if(!txtEmail.text.isNullOrBlank() && !txtPassword.text.isNullOrBlank()) {

                FirebaseAuth.getInstance().signInWithEmailAndPassword(txtEmail.text.toString().trim(),txtPassword.text.toString()).addOnCompleteListener {
                    if(it.isSuccessful) {
                        if (Firebase.auth.currentUser!!.isEmailVerified){
                            showHome()
                        }else {
                            showAllert("Atención","Por favor verifica tu email")
                            Firebase.auth.signOut()
                        }
                    }else {
                        Log.w(TAG, "signInWithEmail:failure", it.exception)
                        showAllert("Error", "No se ha encontrado el email indicado. Es posible que no se encuentre registrado.")
                    }
                    txtEmail.setText("")
                    txtPassword.setText("")
                }
            }

        }

        btnLoginGoogle.setOnClickListener {
            val googleConf = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.web_client_id)).requestEmail().build()

            val googleClient = GoogleSignIn.getClient(this, googleConf)
            googleClient.signOut()

            startActivityForResult(googleClient.signInIntent, Google_Sign_In)
        }

        txtRegistro.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showAllert(titulo: String, texto: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(titulo)
        builder.setMessage(texto)
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun showHome(){
        val homeIntent = Intent(this, MapsActivity::class.java)
        startActivity(homeIntent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == Google_Sign_In){
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            try {
                val account = task.getResult(ApiException::class.java)
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)

                if(account != null){
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                    auth.signInWithCredential(credential).addOnCompleteListener { task ->
                        if (task.isSuccessful){
                            val preferences = getSharedPreferences("checkbox", MODE_PRIVATE)
                            val editor = preferences.edit()
                            editor.putString("remember", "true")
                            editor.apply()
                            val user = Firebase.auth.currentUser
                            user?.let {
                                for (profile in it.providerData) {
                                    val providerId = profile.providerId
                                    val name = profile.displayName
                                    val email = profile.email

                                    db.collection("users").document(email.toString()).set(
                                        hashMapOf(
                                            "Nombre" to name.toString(),
                                            "Email" to email.toString(),
                                            "Poveedor" to providerId,
                                            "Dueño de parqueadero" to false
                                        )
                                    )
                                }
                            }
                            showHome()
                        }else {
                            Log.d(TAG,"Ha ocurrido un error inesperado.")
                        }
                    }
                }
            } catch (e: ApiException){
                Log.w(TAG, "Google sign in failed", e)
            }
        }
    }
}