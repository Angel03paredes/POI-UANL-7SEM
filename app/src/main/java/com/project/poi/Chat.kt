package com.project.poi

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.*
import com.project.poi.adaptadores.ChatUserAdapter
import com.project.poi.modelos.ChatMensaje
import kotlinx.android.synthetic.main.activity_chat_room.*
import kotlinx.android.synthetic.main.fragment_chat.*
import kotlinx.android.synthetic.main.fragment_chat.view.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [fragment1.newInstance] factory method to
 * create an instance of this fragment.
 */
class fragment1 : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }



    }



    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val vista = inflater.inflate(R.layout.fragment_chat, container, false)

        val bundle = this.arguments
        if (bundle != null) {
            val to = bundle.getString("to")


        }

        val arreglo = arrayListOf<ChatMensaje>()

        val sharedPreferences : SharedPreferences = requireContext().getSharedPreferences("SharedP", Context.MODE_PRIVATE)
        val currentUser = sharedPreferences.getString("key", "")


        val database = FirebaseDatabase.getInstance()
        val chatRef : DatabaseReference = database.getReference("private")
        val adaptador: ChatUserAdapter = ChatUserAdapter(requireContext(), arreglo)
        val contUsers = arrayListOf<String>()

        chatRef.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                arreglo.clear()
                contUsers.clear()

                for (snap in snapshot.children) {
                    val mensaje: ChatMensaje = snap.getValue(ChatMensaje::class.java) as ChatMensaje

                    var band = false
                    for(item in contUsers){
                        if(item == mensaje.usuario)
                            band= true
                    }

                    if (mensaje.mensaje == "" && mensaje.usuario == currentUser && !band) {
                        arreglo.add(mensaje)
                        contUsers.add(mensaje.usuario)
                    }
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
        vista.rvPrivateChat.setLayoutManager(llm)
        vista.rvPrivateChat.setAdapter(adaptador)


        vista.botonusuarios.setOnClickListener{
            val intent = Intent(activity, MuestraU::class.java)

            startActivity(intent)
        }




        return vista
        }


    override fun onResume() {
        super.onResume()
    }

    override fun onStop() {
        super.onStop()
    }


    }



