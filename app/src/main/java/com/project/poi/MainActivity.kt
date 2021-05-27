package com.project.poi

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences


    private lateinit var firebaseAuth: FirebaseAuth



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val activity_reg = Intent(this, Registro::class.java)
        firebaseAuth = FirebaseAuth.getInstance()




        btn_signIn.setOnClickListener {

            loginUser(txt_email.text.toString(),txt_password.text.toString())
        }

        lbl_inicio.setOnClickListener {
            startActivity(activity_reg)
        }



}


    private fun loginUser(email:String,password:String){



        if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){
            firebaseAuth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(this){
                    task->
                    if(task.isSuccessful){
                        action(email,password)
                    }else{
                        Toast.makeText(this,"Error en la autentificacion",Toast.LENGTH_LONG).show()
                    }
                }
        }
        else{
            Toast.makeText(this,"Ingrese los campos",Toast.LENGTH_LONG).show()
        }
    }

    fun action(email:String,password: String){
        val activity_home = Intent(this, Home::class.java)

        val database = FirebaseDatabase.getInstance()
        val ref = database.getReference("users")
        ref.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                for (snap in snapshot.children) {
                    val si : String = snap.child("email").toString()
                    if(snap.child("email").value.toString() == email && snap.child("password").value.toString() == password ) {
                        sharedPreferences = getSharedPreferences("SharedP", Context.MODE_PRIVATE)
                        val editor:SharedPreferences.Editor = sharedPreferences.edit()
                        editor.putString("email",email)
                        editor.putString("password",password)
                        editor.putString("userName",snap.child("userName").value.toString())
                        editor.putString("carrera",snap.child("carrera").value.toString())
                        editor.putString("status", "true")
                        editor.putString("key",snap.key.toString())
                        editor.apply()
                    }
                }
            }




            override fun onCancelled(error: DatabaseError) {
                Log.e("si","si")
            }
        })



        startActivity(activity_home)
    }

}
