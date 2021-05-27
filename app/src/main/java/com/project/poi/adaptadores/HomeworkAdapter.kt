package com.project.poi.adaptadores

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.common.collect.Iterables.size
import com.google.common.collect.Iterators.size
import com.google.firebase.storage.FirebaseStorage
import com.project.poi.EntregarTarea
import com.project.poi.R
import com.project.poi.Tareas
import com.project.poi.modelos.ChatMensaje
import com.project.poi.modelos.Homework
import kotlinx.android.synthetic.main.item_homework.view.*
import okio.Utf8.size
import java.nio.file.Files.size

class HomeworkAdapter(private val context: Context, private val tareas:ArrayList<Homework>): RecyclerView.Adapter<HomeworkAdapter.ChatViewHolder>() {

    private val storage = FirebaseStorage.getInstance().reference

    class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeworkAdapter.ChatViewHolder {
        val inflate: View = LayoutInflater.from(context).inflate(
                R.layout.item_homework,
                parent,
                false
        )
        return HomeworkAdapter.ChatViewHolder(inflate)
    }

    override fun getItemCount(): Int {
        return tareas.size
    }


    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val item = tareas[position]

        holder.itemView.txtName.text = item.titulo
        holder.itemView.txtMessage.text = item.descripcionCorta
        if(item.entregado == true){
            holder.itemView.txtDate.text = "Entregado"
        }else{
            holder.itemView.txtDate.text = "Pendiente"
        }

        holder.itemView.button.setOnClickListener {
            val activity = Intent(context, EntregarTarea::class.java)
              activity.putExtra("tareaId",item.id)
            context.startActivity(activity)
        }
    }

}