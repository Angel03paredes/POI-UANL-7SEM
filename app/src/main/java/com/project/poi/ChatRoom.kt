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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.*
import com.google.firebase.database.DatabaseReference
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.OnProgressListener
import com.google.firebase.storage.UploadTask
import com.google.type.LatLng
import com.project.poi.adaptadores.ChatAdaptador
import com.project.poi.modelos.ChatMensaje
import com.project.poi.modelos.CifradoTools
import kotlinx.android.synthetic.main.activity_chat_room.*
import java.io.File
import java.util.*


class ChatRoom : AppCompatActivity() {

   val listaMensajes = mutableListOf<ChatMensaje>()

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var uri:Uri
    //private lateinit var chatAdaptador:ChatAdaptador

    private val adaptador = ChatAdaptador(this, listaMensajes)

    private val storage = FirebaseStorage.getInstance().reference


   // private val adaptador = chatAdaptador(listaMensajes)

   // val adaptador = ChatAdaptador(this, ListaMensajes)

    val database = FirebaseDatabase.getInstance() //Referencia a nuestra base de datos
    var to:String = ""
    lateinit var chatRef :DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_room)

        val claseChat = intent.extras?.getString("chatClase")

        to = intent.extras?.getString("to").toString()

        //GEOLOCALIZACION

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val sharedPreferences : SharedPreferences = getSharedPreferences("SharedP", Context.MODE_PRIVATE)

        val currentUser = sharedPreferences.getString("key","")
        val encryptUser =  sharedPreferences.getBoolean("encrypt",false)





        if(claseChat=="grupo")
            chatRef = database.getReference("chats")
        else
            chatRef = database.getReference("private")


       // val nickname= intent.extras?.getString("Llavenickname")


        btn_archivo_privado.setOnClickListener {
            val intent = Intent()
            intent.type = "application/*"
            intent.setAction(Intent.ACTION_GET_CONTENT)
            startActivityForResult(Intent.createChooser(intent,"Seleccionar Archivo"),1)
        }

        btn_imagenes_private.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.setAction(Intent.ACTION_GET_CONTENT)
            startActivityForResult(Intent.createChooser(intent,"Seleccionar Archivo"),2)
        }


        btn_geo.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.lastLocation
                        .addOnSuccessListener { location : Location? ->
                            traducirCoordenadas(location?.latitude!!,location.longitude,currentUser.toString(),to.toString(),chatRef,encryptUser)
                        }

            }

        }

        rv_mensajes.adapter = adaptador

        iv_enviar.setOnClickListener {



            val mensaje = txtMensaje.text.toString()
            if (mensaje.isNotEmpty()) {

               txtMensaje.text.clear()
               enviarMensaje(ChatMensaje( mensaje, currentUser.toString(), ServerValue.TIMESTAMP,to.toString()),chatRef,encryptUser)
            }
        }
        recibirMensajes(chatRef,currentUser.toString(),to.toString())


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if(resultCode == RESULT_OK){
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

    private fun traducirCoordenadas(latitude:Double,longitude:Double,currentUser: String,to: String,chatRef: DatabaseReference,encryptUser:Boolean){

        val geocoder = Geocoder(this, Locale.getDefault())

        Thread{

            val direcciones = geocoder.getFromLocation(latitude, longitude, 1)
            if(direcciones.size > 0){


                val direccion = direcciones[0].getAddressLine(0)
                runOnUiThread {
                    enviarMensaje(ChatMensaje( direccion, currentUser.toString(), ServerValue.TIMESTAMP,to.toString()),chatRef,encryptUser)
                }
            }

        }.start()
    }



    //Implemnts functions firebase

    private fun enviarMensaje(mensaje: ChatMensaje,chatRef:DatabaseReference,ecrytUser:Boolean) {



        val mensajeFirebase = chatRef.push()
        if(ecrytUser){
            mensaje.encrypt = true
            val textoCifrado = CifradoTools.cifrar(mensaje.mensaje,  "encrypt")
            mensaje.mensaje = textoCifrado
        }

      //  mensaje.id = mensajeFirebase.key ?: ""
        mensajeFirebase.setValue(mensaje)
    }

    private fun recibirMensajes(chatRef:DatabaseReference,currentUser:String,to:String) {




        chatRef.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                listaMensajes.clear()

                for (snap in snapshot.children) {

                    //UrlUser urlUser = new UrlUser(ds.getValue(String.class))

                    val mensaje: ChatMensaje = snap.getValue(ChatMensaje::class.java) as ChatMensaje
                    if (mensaje.usuario == currentUser && mensaje.mensaje != "" && mensaje.to == to ||
                        mensaje.usuario == currentUser && mensaje.hayArchivo == true && mensaje.to == to  ){
                        mensaje.EsMiMensaje = true

                        if(mensaje.encrypt){

                            val texto = CifradoTools.descifrar(mensaje.mensaje,  "encrypt")
                            mensaje.mensaje = texto
                        }

                        listaMensajes.add(mensaje)
                    }

                    if( mensaje.usuario == to && mensaje.mensaje != "" && mensaje.to == currentUser ||
                        mensaje.usuario == to && mensaje.hayArchivo == true && mensaje.to == currentUser){
                        mensaje.EsMiMensaje = false

                        if(mensaje.encrypt){

                            val texto = CifradoTools.descifrar(mensaje.mensaje,  "encrypt")
                            mensaje.mensaje = texto
                        }

                        listaMensajes.add(mensaje)
                    }

                }

                adaptador.notifyDataSetChanged()
                if (listaMensajes.size > 0) {
                    rv_mensajes.smoothScrollToPosition(listaMensajes.size - 1)
                }
            }

            override fun onCancelled(error: DatabaseError) {

                Toast.makeText(this@ChatRoom, "Error al leer mensajes", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private  fun saveFile(){

        val pd : ProgressDialog = ProgressDialog(this)
        pd.setTitle("Subiendo Archivo")
        pd.show()

        val sharedPreferences : SharedPreferences = getSharedPreferences("SharedP", Context.MODE_PRIVATE)

        val key = sharedPreferences.getString("key", "")
        val uuid  = UUID.randomUUID().toString()
        val storageRef = storage.child("filesPrivate/" + uuid)

        storageRef.putFile(uri)
            .addOnSuccessListener {
                enviarMensaje(ChatMensaje( "Archivo adjunto", key.toString(), ServerValue.TIMESTAMP,to,false,true,uuid),chatRef,false)
                Toast.makeText(this,"Archivo Subido",Toast.LENGTH_SHORT).show()
                pd.dismiss()
            }
            .addOnFailureListener{
                pd.dismiss()
                Toast.makeText(this,"Error al subir imagen",Toast.LENGTH_LONG).show()
            }
            .addOnProgressListener(OnProgressListener<UploadTask.TaskSnapshot>() {

                var progress:Double = 100.0 * (it.getBytesTransferred() / it.getTotalByteCount());
                pd.setMessage("Porcentaje: " + progress.toInt() + "%")

            })


    }







    override fun onResume() {
        super.onResume()
        val sharedPreferences : SharedPreferences = getSharedPreferences("SharedP", Context.MODE_PRIVATE)
        val changeStatus = sharedPreferences.getString("status","")
        val key = sharedPreferences.getString("key","")
        if(changeStatus == "false"){
            database.getReference("users/" + key + "/status").setValue("false")
        }
        else{
            database.getReference("users/" + key + "/status").setValue("true")
        }
    }


    override fun onPause() {
        super.onPause()
        val sharedPreferences : SharedPreferences = getSharedPreferences("SharedP", Context.MODE_PRIVATE)

        val key = sharedPreferences.getString("key","")
        database.getReference("users/" + key + "/status").setValue("false")
    }


}