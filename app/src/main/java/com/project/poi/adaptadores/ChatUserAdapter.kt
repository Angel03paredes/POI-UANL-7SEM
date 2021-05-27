package com.project.poi.adaptadores

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.project.poi.ChatRoom
import com.project.poi.R
import com.project.poi.modelos.ChatMensaje
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_chat_user.view.*
import kotlinx.android.synthetic.main.item_user.view.*

class ChatUserAdapter(private val context: Context, private val ListaUsuario:ArrayList<ChatMensaje>):RecyclerView.Adapter<ChatUserAdapter.ChatViewHolder>() {
    private val storage = FirebaseStorage.getInstance().reference
    class ChatViewHolder(view: View):RecyclerView.ViewHolder(view){
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val inflate: View = LayoutInflater.from(context).inflate(
                R.layout.item_chat_user,
                parent,
                false
        )
        return ChatUserAdapter.ChatViewHolder(inflate)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val nombre = holder.itemView.findViewById<TextView>(R.id.txtName)
        val message = holder.itemView.findViewById<TextView>(R.id.txtMessage)
        val date = holder.itemView.findViewById<TextView>(R.id.txtDate)
        val keyTo = ListaUsuario[position].to



        val database = FirebaseDatabase.getInstance()
        val ref = database.getReference("users/" + keyTo + "/avatar")
        ref.get().addOnSuccessListener {
            val fotoLoad = storage.child("avatarUsuario/" + it.value)
            fotoLoad.downloadUrl.addOnSuccessListener {
                val imgProfile =  holder.itemView.findViewById<ImageView>(R.id.imgProfilechat)
                Picasso.get().load(it).into(imgProfile)
            }.addOnFailureListener {
                Log.e("firebase", "Error avatar", )
            }
        }.addOnFailureListener {
            Log.e("firebase", "Error getting data", )
        }
        val refNombre = database.getReference("users/" + keyTo + "/userName")
        refNombre.get().addOnSuccessListener {
            nombre.text = it.value.toString()
        }

        val refSatus = database.getReference("users/" + keyTo + "/status")
        refSatus.get().addOnSuccessListener {
            if(it?.value.toString() == "true") {
                holder.itemView.status.text = "Conectado"
            }
            else {
                holder.itemView.status.text = "Desconectado"
            }
        }

        val card = holder.itemView.findViewById<CardView>(R.id.chat_user)

        card.setOnClickListener{
            val intent = Intent(context,ChatRoom::class.java)
            intent.putExtra("to",ListaUsuario[position].to)
            intent.putExtra("chatClase","private")
            context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int {
        return ListaUsuario.size
    }


}