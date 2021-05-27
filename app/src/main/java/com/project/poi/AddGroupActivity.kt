package com.project.poi

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.fcfm.poi.plantilla.models.Grupo
import com.google.firebase.database.FirebaseDatabase
import com.project.poi.modelos.User
import kotlinx.android.synthetic.main.activity_add_group.*
import kotlinx.android.synthetic.main.activity_home.*

class AddGroupActivity : AppCompatActivity() {
    private lateinit var nameGroup:String

    private val dataBase = FirebaseDatabase.getInstance()
    private val dbRef = dataBase.getReference("Grupos")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_group)




        val sharedPreferencesToName : SharedPreferences = getSharedPreferences("SharedP", Context.MODE_PRIVATE)
        val name =sharedPreferencesToName.getString("userName","")
        val carrera = sharedPreferencesToName.getString("carrera","")
        val email = sharedPreferencesToName.getString("email","")
        val key = sharedPreferencesToName.getString("key","")


        btnAddGroup.setOnClickListener{
            nameGroup = txtAddGroup.text.toString()
            if (!TextUtils.isEmpty(nameGroup)) {
                txtAddGroup.text.clear()
                setGroup(Grupo("",carrera.toString(),nameGroup), User(name.toString(),email.toString(),"",carrera.toString(),false,"",key.toString()))

            }else{
                Toast.makeText(this,"Agrega un nombre de grupo",Toast.LENGTH_LONG).show()
            }
        }


    }

    private fun setGroup(groupP: Grupo, usuario: User) {

        val grupo = dbRef.push()
        groupP.id = grupo.key ?:""
        grupo.setValue(groupP)
        val hijo = dbRef.child(groupP.id).child("Integrantes").push()
        hijo.setValue(usuario)
        finish()
    }




}