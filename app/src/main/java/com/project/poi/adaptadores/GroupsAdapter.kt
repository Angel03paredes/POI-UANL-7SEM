package com.project.poi.adaptadores

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.fcfm.poi.plantilla.models.Grupo
import com.project.poi.Home
import com.project.poi.R
import com.project.poi.buscar


class GroupsAdapter(private val context: Context, private val ListGroups: List<Grupo>):
    RecyclerView.Adapter<ChatUserAdapter.ChatViewHolder>() {
    class ChatViewHolder(view: View): RecyclerView.ViewHolder(view){
    }

    override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
    ): ChatUserAdapter.ChatViewHolder {
        val inflate: View = LayoutInflater.from(context).inflate(
                R.layout.item_group,
                parent,
                false
        )
        return ChatUserAdapter.ChatViewHolder(inflate)
    }

    override fun onBindViewHolder(holder: ChatUserAdapter.ChatViewHolder, position: Int) {

        val card = holder.itemView.findViewById<CardView>(R.id.cardGroups)

        card.setOnClickListener {

            val intent = Intent(context, Home::class.java)
            intent.putExtra("groupName", ListGroups[position].Nombre)
            intent.putExtra("groupId", ListGroups[position].id)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return ListGroups.size
    }
}