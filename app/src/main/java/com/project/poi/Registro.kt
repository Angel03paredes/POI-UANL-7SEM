package com.project.poi

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.project.poi.modelos.User
import kotlinx.android.synthetic.main.activity_main.lbl_inicio
import kotlinx.android.synthetic.main.activity_registro.*


class Registro : AppCompatActivity() {


   // private lateinit var databaseReference: DatabaseReference
    //private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var firebaseAuth: FirebaseAuth
    val database = FirebaseDatabase.getInstance()

    private lateinit var userInstance : User
    private var carrera: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)
        val activity = Intent(this, MainActivity::class.java)

        lbl_inicio.setOnClickListener{startActivity(activity)}


        firebaseAuth = FirebaseAuth.getInstance()

        var spinnerCarrera = findViewById(R.id.spinnerCarrera) as Spinner
        val carrerasList = arrayOf("Selecciona una carrera","LA","LCC","LM","LCTI","LF","LMAD")

        spinnerCarrera.adapter = ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,carrerasList)

        spinnerCarrera.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(item: AdapterView<*>?) {
                carrera = carrerasList.get(1)
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                carrera = carrerasList.get(p2)
            }
        }
        btn_register.setOnClickListener { createNewUser() }

    }

    private fun createNewUser(){
        val userName = txt_nameuser_r.text.toString()
        val email = txt_email_r.text.toString()
        val pass = txt_password_r.text.toString()

        if(!TextUtils.isEmpty(userName) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(pass) ){
            firebaseAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(this){ task->
                if(task.isComplete()){
                  val currentUser:FirebaseUser?=firebaseAuth.currentUser
                    verifyEmail(currentUser)

                    userInstance = User(userName,email,pass,carrera)

                    val userRef = database.getReference("users")
                    val userFireBase = userRef.push()
                    userInstance.key = userFireBase.key ?: ""
                    userFireBase.setValue(userInstance)

                    }

                action()

                }
            }
        else{
            Toast.makeText(this,"LLenar todos los campos para registarse",Toast.LENGTH_LONG).show()
        }

    }

    private fun verifyEmail(user: FirebaseUser?){
        user?.sendEmailVerification()?.addOnCompleteListener(this){ task->

            if(task.isComplete){
                Toast.makeText(this, "Registrado!!", Toast.LENGTH_LONG).show()
            }else{
                Toast.makeText(this, "Error al registrar", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun action(){

        startActivity(Intent(this, MainActivity::class.java))
    }

}