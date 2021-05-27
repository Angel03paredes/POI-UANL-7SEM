package com.project.poi.adaptadores

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.project.poi.Home
import com.project.poi.MuestraU
import com.project.poi.R
import com.project.poi.fragment1
import com.project.poi.modelos.ChatMensaje
import com.project.poi.modelos.MuestraUser
import com.project.poi.modelos.User
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_user.view.*

class MuestraUserAdaptador(private val context: Context,private var ListaUsuario:ArrayList<User>,private val  currentUser:String,private val groupId:String) : RecyclerView.Adapter<MuestraUserAdaptador.UserViewHolder>() {
    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val inflate: View = LayoutInflater.from(context).inflate(
                R.layout.item_user,
                parent,
                false
        )
        return MuestraUserAdaptador.UserViewHolder(inflate)
    }

    override fun getItemCount(): Int {
        return ListaUsuario.size
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val nombre = holder.itemView.findViewById<TextView>(R.id.txtNameItemUser)
        val email = holder.itemView.findViewById<TextView>(R.id.txtEmailUser)


        if(ListaUsuario[position].status == "true") {
            holder.itemView.txtStatus.text = "Conectado"
        }
        else {
            holder.itemView.txtStatus.text = "Desconectado"
        }
        nombre.text = ListaUsuario[position].userName
        email.text = ListaUsuario[position].email
        val storage = FirebaseStorage.getInstance().reference
        val fotoLoad = storage.child("avatarUsuario/" + ListaUsuario[position].avatar)
        fotoLoad.downloadUrl.addOnSuccessListener {
            val imgProfile = holder.itemView.findViewById<ImageView>(R.id.imgProfileU)
            Picasso.get().load(it).into(imgProfile)
        }.addOnFailureListener {
            Log.e("firebase", "Error avatar", )
        }


        val card = holder.itemView.findViewById<CardView>(R.id.cardUserItem)

        card.setOnClickListener {

            if(groupId == ""){
                val mensaje: ChatMensaje = ChatMensaje("", currentUser, ServerValue.TIMESTAMP, ListaUsuario[position].key)
                val mensaje2: ChatMensaje = ChatMensaje("", ListaUsuario[position].key, ServerValue.TIMESTAMP, currentUser)
                val database = FirebaseDatabase.getInstance()
                val chatRef = database.getReference("private")
                val mensajeFirebase2 = chatRef.push()
                val mensajeFirebase = chatRef.push()
                // mensaje2.id= mensajeFirebase2.key ?: ""
                // mensaje.id = mensajeFirebase.key ?: ""
                mensajeFirebase.setValue(mensaje)
                mensajeFirebase2.setValue(mensaje2)
                    }else{

                 val dataBase = FirebaseDatabase.getInstance()
                 val dbRef = dataBase.getReference("Grupos")
                val user = dbRef.child(groupId).child("Integrantes").push()
                user.setValue(ListaUsuario[position])

                    }


           // val activity = Intent(context, Home::class.java)
           //  activity.putExtra("name",ListaUsuario[position].userName)
          // activity.putExtra("to", ListaUsuario[position].email)

            (context as Activity).finish()

           // context.startActivity(activity)
        }


    }
/*
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence?): FilterResults {

                //Obtenemos la cadena
                val filterResults = Filter.FilterResults()
                filterResults.values = if (charSequence == null || charSequence.isEmpty()) {

                    ListaUsuario

                } else {
                    val queryString = charSequence?.toString()?.toLowerCase()



                    ListaUsuario.filter { user ->

                        user.userName!!.toLowerCase().contains(queryString) || user.userName!!.toLowerCase().contains(queryString)
                    }
                }

                return filterResults
            }

            override fun publishResults(p0: CharSequence?, results: FilterResults?) {
                ListaUsuario = results?.values as ArrayList<User>
                notifyDataSetChanged()
            }
        }
    }*/
}