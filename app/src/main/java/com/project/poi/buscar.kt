package com.project.poi

import android.Manifest
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.OnProgressListener
import com.google.firebase.storage.UploadTask
import com.project.poi.adaptadores.ChatAdaptador
import com.project.poi.adaptadores.ChatUserAdapter
import com.project.poi.modelos.ChatMensaje
import kotlinx.android.synthetic.main.activity_chat_room.*
import kotlinx.android.synthetic.main.fragment_grupo.*
import kotlinx.android.synthetic.main.fragment_grupo.view.*
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [buscar.newInstance] factory method to
 * create an instance of this fragment.
 */
class buscar : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    val arreglo = mutableListOf<ChatMensaje>()
    private val storage = FirebaseStorage.getInstance().reference
    private lateinit var uri: Uri
    private var groupReference: String = ""
    lateinit var chatRef :DatabaseReference

    private lateinit var fusedLocationClient: FusedLocationProviderClient
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

        val view = inflater.inflate(R.layout.fragment_grupo, container, false)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        val bundle = this.arguments
        if (bundle != null) {
            val groupId = bundle.getString("groupId")
            val currentUser = bundle.getString("key")
            val database = FirebaseDatabase.getInstance()
            val adaptador: ChatAdaptador = ChatAdaptador(requireContext(), arreglo)
             chatRef = database.getReference("grupoChat")

            groupReference = groupId.toString()

            val llm = LinearLayoutManager(requireContext())
            llm.orientation = LinearLayoutManager.VERTICAL
            view.rvGrupoChat.setLayoutManager(llm)
            view.rvGrupoChat.setAdapter(adaptador)

            view.btn_archivo_grupo.setOnClickListener {
                val intent = Intent()
                intent.type = "application/*"
                intent.setAction(Intent.ACTION_GET_CONTENT)
                startActivityForResult(Intent.createChooser(intent,"Seleccionar Archivo"),1)
            }

            view.btn_imagen_grupo.setOnClickListener {
                val intent = Intent()
                intent.type = "image/*"
                intent.setAction(Intent.ACTION_GET_CONTENT)
                startActivityForResult(Intent.createChooser(intent,"Seleccionar Archivo"),2)
            }



            view.btnSendMessage.setOnClickListener {



                val mensaje = view.txtSendMessage.text.toString()
                if (mensaje.isNotEmpty()) {

                    view.txtSendMessage.text.clear()
                    enviarMensaje(ChatMensaje( mensaje, currentUser.toString(), ServerValue.TIMESTAMP,groupId.toString()),chatRef)
                }
            }

            view.btn_group_ubication.setOnClickListener {
                if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                    fusedLocationClient.lastLocation
                        .addOnSuccessListener { location : Location? ->
                            traducirCoordenadas(location?.latitude!!,location.longitude,currentUser.toString(),groupId.toString(),chatRef)
                        }

                }

            }


            chatRef.addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    arreglo.clear()

                    for (snap in snapshot.children) {

                        //UrlUser urlUser = new UrlUser(ds.getValue(String.class))

                        val mensaje: ChatMensaje = snap.getValue(ChatMensaje::class.java) as ChatMensaje
                        if (mensaje.usuario == currentUser && mensaje.mensaje != "" || mensaje.usuario == currentUser && mensaje.hayArchivo == true)
                            mensaje.EsMiMensaje = true
                        if(mensaje.to == groupId) {
                            arreglo.add(mensaje)
                        }
                        //}

                       // if( mensaje.usuario == groupId && mensaje.mensaje != "" && mensaje.to == currentUser){
                        //    mensaje.EsMiMensaje = false
                        //    arreglo.add(mensaje)
                       // }

                    }

                    adaptador.notifyDataSetChanged()
                    if (arreglo.size > 0) {
                        rvGrupoChat.smoothScrollToPosition(arreglo.size - 1)
                    }
                }

                override fun onCancelled(error: DatabaseError) {


                }
            })


        }

            return view
    }

    private fun enviarMensaje(mensaje: ChatMensaje,chatRef:DatabaseReference) {

        val mensajeFirebase = chatRef.push()
        //  mensaje.id = mensajeFirebase.key ?: ""
        mensajeFirebase.setValue(mensaje)
    }


    private fun traducirCoordenadas(latitude:Double,longitude:Double,currentUser: String,to: String,chatRef: DatabaseReference){

        val geocoder = Geocoder(requireContext(), Locale.getDefault())

        Thread{

            val direcciones = geocoder.getFromLocation(latitude, longitude, 1)
            if(direcciones.size > 0){


                val direccion = direcciones[0].getAddressLine(0)
                //runOnUiThread {

                    enviarMensaje(ChatMensaje( direccion, currentUser.toString(), ServerValue.TIMESTAMP,to.toString()),chatRef)
                //}
            }

        }.start()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if(resultCode == AppCompatActivity.RESULT_OK){
            if(requestCode == 1) {///Aplication
                uri = data!!.data!!
                saveFile()
            }
            if(requestCode == 2) {///Aplication
                uri = data!!.data!!
                saveFile()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }


    private  fun saveFile() {

        val pd: ProgressDialog = ProgressDialog(requireContext())
        pd.setTitle("Subiendo Archivo")
        pd.show()

        val sharedPreferences: SharedPreferences = requireContext().getSharedPreferences("SharedP", Context.MODE_PRIVATE)

        val key = sharedPreferences.getString("key", "")
        val uuid = UUID.randomUUID().toString()
        val storageRef = storage.child("filesPrivate/" + uuid)

        storageRef.putFile(uri)
                .addOnSuccessListener {
                    enviarMensaje(ChatMensaje("Archivo adjunto", key.toString(), ServerValue.TIMESTAMP, groupReference, false, true, uuid), chatRef)
                    Toast.makeText(requireContext(),"Archivo Subido", Toast.LENGTH_SHORT).show()
                    pd.dismiss()
                }
                .addOnFailureListener {
                    pd.dismiss()
                    Toast.makeText(requireContext(), "Error al subir archivo", Toast.LENGTH_LONG).show()
                }
                .addOnProgressListener(OnProgressListener<UploadTask.TaskSnapshot>() {

                    var progress: Double = 100.0 * (it.getBytesTransferred() / it.getTotalByteCount());
                    pd.setMessage("Porcentaje: " + progress.toInt() + "%")

                })
    }




        companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment buscar.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            buscar().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}