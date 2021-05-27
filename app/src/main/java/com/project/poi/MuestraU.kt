package com.project.poi

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.SharedMemory
import android.util.Log
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.PropertyName
import com.project.poi.adaptadores.MuestraUserAdaptador
import com.project.poi.modelos.ChatMensaje
import com.project.poi.modelos.MuestraUser
import com.project.poi.modelos.User
import kotlinx.android.synthetic.main.activity_chat_room.*
import kotlinx.android.synthetic.main.activity_usuarios.*

class MuestraU : AppCompatActivity() {

    val arreglo = arrayListOf<User>()
    val lista = mutableListOf<User>()
    val database = FirebaseDatabase.getInstance()
    private lateinit var userInstance : User
    private lateinit var muestraUadaptador:MuestraUserAdaptador

    private lateinit var currentgroup: String
    private var groupId = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_usuarios)


        if(intent.hasExtra("groupId")){
            groupId = intent.extras?.getString("groupId")!!
        }




        database.getReference("users").addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                arreglo.clear()

                for (snap in snapshot.children) {

                    //UrlUser urlUser = new UrlUser(ds.getValue(String.class))

                    val usuario: User = snap.getValue(User::class.java) as User

                        arreglo.add(usuario)



                }
                val sharedPreferences : SharedPreferences = getSharedPreferences("SharedP", Context.MODE_PRIVATE)

                val currentUser = sharedPreferences.getString("key","")

                muestraUadaptador = MuestraUserAdaptador(this@MuestraU,arreglo,currentUser.toString(),groupId)
                rvUsers.adapter = muestraUadaptador
                muestraUadaptador.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }


            // svUsuariosChat.setOnQueryTextListener(this)

    })




    }


    override fun onResume() {
        super.onResume()
        val sharedPreferences : SharedPreferences = getSharedPreferences("SharedP", Context.MODE_PRIVATE)
        val changeStatus = sharedPreferences.getString("status","")
        val key = sharedPreferences.getString("key","")
        if(changeStatus == "false"){
            database.getReference("users/" + key + "/status").setValue("false")
        }
        else{
            database.getReference("users/" + key + "/status").setValue("true")
        }
    }


    override fun onPause() {
        super.onPause()
        val sharedPreferences : SharedPreferences = getSharedPreferences("SharedP", Context.MODE_PRIVATE)

        val key = sharedPreferences.getString("key","")
        database.getReference("users/" + key + "/status").setValue("false")
    }

}