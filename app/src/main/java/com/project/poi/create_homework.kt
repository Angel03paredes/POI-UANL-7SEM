package com.project.poi

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import com.fcfm.poi.plantilla.models.Grupo
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.project.poi.modelos.Homework
import kotlinx.android.synthetic.main.activity_create_homework.*

class create_homework : AppCompatActivity() {

    private var groupId = ""
    private val dataBase = FirebaseDatabase.getInstance()
    private val dbRef = dataBase.getReference("Grupos")
    private val dbRefHW = dataBase.getReference("Tareas")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_homework)

        if(intent.hasExtra("groupId")){
            groupId = intent.extras?.getString("groupId")!!
        }



                btn_crear_tarea.setOnClickListener {
                  creaTarea()
                }

    }

    private fun creaTarea() {
        val titulo = editTitulo.text
        val puntos = editPuntos.text
        val descripcionCorta = editDescCorta.text
        val descripcion = editDescLarga.text
        if(!titulo.isEmpty() && !puntos.isEmpty() && !descripcionCorta.isEmpty() && !descripcion.isEmpty()  ){

            dbRef.addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                }

                override fun onDataChange(snapshot: DataSnapshot) {

                    var key = ""
                    for (snap in snapshot.children) {
                        var childKey = snap.key.toString()

                        if(childKey == groupId){
                            var Personas = snap.child("Integrantes").children
                            for (persona in Personas) {
                                key = persona.child("key").value.toString()
                                val tareas = dbRefHW.push()
                                val homework:Homework = Homework(null,titulo.toString(),descripcionCorta.toString(),descripcion.toString(),Integer.parseInt(puntos.toString()),false,groupId,key)
                                homework.id = tareas.key ?:""
                                tareas.setValue(homework)
                                // val hijo = dbRef.child(groupP.id).child("Integrantes").push()
                                //hijo.setValue(usuario)
                            }
                            editTitulo.text.clear()
                            editPuntos.text.clear()
                            editDescCorta.text.clear()
                            editDescLarga.text.clear()
                            Toast.makeText(this@create_homework,"Tarea Creada",Toast.LENGTH_LONG).show()
                        }


                    }

                }
            })



        }else{
            Toast.makeText(this,"Llenar todos los campos",Toast.LENGTH_LONG).show()
        }
    }


}