package com.project.poi

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.fcfm.poi.plantilla.models.Grupo
import com.google.firebase.database.*
import com.project.poi.adaptadores.ChatUserAdapter
import com.project.poi.adaptadores.HomeworkAdapter
import com.project.poi.modelos.ChatMensaje
import com.project.poi.modelos.Homework
import kotlinx.android.synthetic.main.fragmentot_tareas.view.*

class Tareas : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                          savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragmentot_tareas, container, false)

        val arreglo = arrayListOf<Homework>()

        val sharedPreferences : SharedPreferences = requireContext().getSharedPreferences("SharedP", Context.MODE_PRIVATE)
        val key = sharedPreferences.getString("key", "")


        val database = FirebaseDatabase.getInstance()
        val chatRef : DatabaseReference = database.getReference("Tareas")
        val adaptador: HomeworkAdapter = HomeworkAdapter(requireContext(), arreglo)

        chatRef.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                arreglo.clear()

                for (snap in snapshot.children) {

                    val homework: Homework = snap.getValue(Homework::class.java) as Homework
                    if (homework.usuario == key )
                        arreglo.add(homework)

                }

                adaptador.notifyDataSetChanged()

            }

            override fun onCancelled(error: DatabaseError) {

                Toast.makeText(requireContext(), "Error al leer contenido", Toast.LENGTH_SHORT).show()
            }
        })


        //vista.rvPrivateChat.adapter = adaptador;
        val llm = LinearLayoutManager(requireContext())
        llm.orientation = LinearLayoutManager.VERTICAL
        view.rvTareas.setLayoutManager(llm)
        view.rvTareas.setAdapter(adaptador)


        return view


    }
}