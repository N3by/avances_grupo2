package com.example.myparkea

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {

    private var TAG = "RegisterActivity"
    private lateinit var auth: FirebaseAuth
    private var db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        setUp()
    }

    private fun setUp() {
        val btnRegister : Button = findViewById(R.id.btn_Register)
        val txtName : TextInputEditText = findViewById(R.id.txt_Name_et)
        val txtEmail : TextInputEditText = findViewById(R.id.txt_Email_et)
        val txtPassword : TextInputEditText = findViewById(R.id.txt_Password_et)
        val txtCPassword : TextInputEditText = findViewById(R.id.txt_CPassword_et)
        val txtIngresar : TextView = findViewById(R.id.txt_Ingresar)

        val tilName : TextInputLayout = findViewById(R.id.txt_Name)
        val tilEmail : TextInputLayout = findViewById(R.id.txt_Email)
        val tilPassword : TextInputLayout = findViewById(R.id.txt_Password)
        val tilCPassword : TextInputLayout = findViewById(R.id.txt_CPassword)

        btnRegister.setOnClickListener {

            if(txtName.text.isNullOrBlank()) tilName.helperText="Campo Requerido*" else tilName.helperText=null
            if(txtEmail.text.isNullOrBlank()) tilEmail.helperText="Campo Requerido*" else tilEmail.helperText=null
            if(txtPassword.text.isNullOrBlank()) tilPassword.helperText="Campo Requerido*" else tilPassword.helperText=null
            if(txtCPassword.text.isNullOrBlank()) tilCPassword.helperText="Campo Requerido*" else tilCPassword.helperText=null

            if(!txtName.text.isNullOrBlank() && !txtEmail.text.isNullOrBlank() && !txtPassword.text.isNullOrBlank() && !txtCPassword.text.isNullOrBlank()) {
                if(txtPassword.text.toString().length < 6 && txtCPassword.text.toString().length < 6){
                    txtPassword.setText("")
                    txtCPassword.setText("")
                    tilPassword.helperText="La contraseña debe tener 6 o más caracteres"
                    tilCPassword.helperText="La contraseña debe tener 6 o más caracteres"
                }else if(txtPassword.text.toString() != txtCPassword.text.toString()) {
                    showAllert("Error", "Las contraseñas ingresadas no coinciden.")
                    txtPassword.setText("")
                    txtCPassword.setText("")
                }else {
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(txtEmail.text.toString(),txtPassword.text.toString()).addOnCompleteListener{ it ->
                        if (it.isSuccessful){
                            Firebase.auth.currentUser!!.sendEmailVerification().addOnCompleteListener { task ->
                                if(task.isSuccessful) {
                                    db.collection("users").document(txtEmail.text.toString()).set(
                                        hashMapOf(
                                            "Nombre" to txtName.text.toString(),
                                            "Email" to txtEmail.text.toString(),
                                            "proveedor" to "password",
                                            "Dueño de parqueadero" to false
                                        )
                                    )
                                    showAllert("Atención","Por favor verifica tu email")
                                }else {
                                    Log.w(TAG, "createUserWithEmail:failure", it.exception)
                                }
                            }
                        }else {
                            Log.w(TAG, "createUserWithEmail:failure", it.exception)
                            showAllert("Error","Se ha producido un error registrando al usuario. Es posible que ya se encuentre registrado.")
                            txtName.setText("")
                            txtEmail.setText("")
                            txtPassword.setText("")
                            txtCPassword.setText("")
                        }
                    }
                }
            }
        }

        txtIngresar.setOnClickListener {
            showHome()
        }
    }

    private fun showAllert(titulo: String, texto: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(titulo)
        builder.setMessage(texto)
        builder.setPositiveButton(R.string.Aceptar){ _, _ ->
            showHome()
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun showHome(){
        val homeIntent = Intent(this, MainActivity::class.java)
        startActivity(homeIntent)
    }
}