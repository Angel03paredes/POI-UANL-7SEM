package com.project.poi

import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_add_homework.*
import kotlinx.android.synthetic.main.layout_ver_puntos.*

class EntregarTarea: AppCompatActivity()  {
    lateinit var dialog: Dialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_homework)

        val sharedPreferences : SharedPreferences = getSharedPreferences("SharedP", Context.MODE_PRIVATE)

        dialog = Dialog(this)

        val key = sharedPreferences.getString("key","")

        val tareaId = intent.extras?.getString("tareaId")
        var puntos:Int = 0
        var puntosUser:Int = 0
        var mostrar: String = "false"
        val database = FirebaseDatabase.getInstance()

        val ref = database.getReference("Tareas/" + tareaId  )
        ref.get().addOnSuccessListener {
            textView.text  = it.child("titulo").value.toString()
            textView5.text  = it.child("descripcion").value.toString()
            txtPuntos.text  = it.child("puntos").value.toString() + " Puntos"
            puntos = Integer.parseInt(it.child("puntos").value.toString())
            mostrar = it.child("entregado").value.toString()
            if(mostrar == "true"){
                button3.visibility = View.GONE
                button4.visibility = View.GONE
            }
        }.addOnFailureListener {
            Log.e("firebase", "Error getting data", )
        }



        val refUser = database.getReference("users/" + key + "/puntos"  )
        refUser.get().addOnSuccessListener {
           puntosUser = Integer.parseInt(it.value.toString())

        }.addOnFailureListener {
            Log.e("firebase", "Error getting data", )
        }

                button3.setOnClickListener {

                    var acomulate = puntos + puntosUser

                    database.getReference("Tareas/" + tareaId + "/entregado").setValue(true)
                    database.getReference("users/" + key + "/puntos").setValue(acomulate)

                    showPuntuacion(puntos.toString())

                }

        }



    private fun showPuntuacion(puntos:String) {
        dialog.setContentView(R.layout.layout_ver_puntos)
        dialog.show()

        dialog.btn_close.setOnClickListener {
            dialog.dismiss()
        }

        dialog.btn_aceptar.setOnClickListener {
            dialog.dismiss()
        }

            dialog.txt_puntos.text = "Ganaste " + puntos  + " puntos . Felicidades"
        button3.visibility = View.GONE
        button4.visibility = View.GONE

    }

}