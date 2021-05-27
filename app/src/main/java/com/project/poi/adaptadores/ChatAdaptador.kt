package com.project.poi.adaptadores

import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.util.Log
import android.view.Gravity
import android.view.Gravity.END
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.DownloadListener
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.content.ContextCompat.getSystemService
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.project.poi.R
import com.project.poi.modelos.ChatMensaje
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_chatbox.view.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.jar.Manifest


class ChatAdaptador(private val context: Context, private val ListaMensajes: List<ChatMensaje>) : RecyclerView.Adapter<ChatAdaptador.ChatViewHolder>(),ActivityCompat.OnRequestPermissionsResultCallback{
    class ChatViewHolder(view: View):RecyclerView.ViewHolder(view){
    }
    private val storage = FirebaseStorage.getInstance().reference
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val vistaInflada: View
     if (viewType == 0){
          vistaInflada =LayoutInflater.from(context).inflate(
                 R.layout.activity_chatbox2,
                 parent,
                 false
         )
     }else{
         vistaInflada = LayoutInflater.from(context).inflate(
                 R.layout.activity_chatbox,
                 parent,
                 false
         )
     }
        return ChatViewHolder(vistaInflada)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {

      val nombre = holder.itemView.findViewById<TextView>(R.id.tvNombre)
        val hora = holder.itemView.findViewById<TextView>(R.id.tvMensajeHora)
        val mensaje = holder.itemView.findViewById<TextView>(R.id.tvMensaje)

        val database = FirebaseDatabase.getInstance()
        val refNombre = database.getReference("users/" + ListaMensajes[position].usuario + "/userName")
        refNombre.get().addOnSuccessListener {
            nombre.text = it.value.toString()
        }

        if( ListaMensajes[position].hayArchivo == true){
            holder.itemView.btn_download.visibility = View.VISIBLE
        }

        holder.itemView.btn_download.setOnClickListener {
            val fotoLoad = storage.child("filesPrivate/" + ListaMensajes[position].uriArchivo )
            fotoLoad.downloadUrl.addOnSuccessListener {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if (ContextCompat.checkSelfPermission(context,android.Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                            PackageManager.PERMISSION_DENIED){
                        //permission denied
                        val permissions = arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        //show popup to request runtime permission

                        requestPermissions(context as Activity,permissions, 1000)
                    }else{
                        val request = DownloadManager.Request(it)
                        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                        request.allowScanningByMediaScanner()
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,"${System.currentTimeMillis()}")

                        //downloadReference = downloadManager.enqueue(request)
                        val manager:DownloadManager = context.getSystemService(  Context.DOWNLOAD_SERVICE) as DownloadManager
                        manager.enqueue(request)
                    }
                }

            }.addOnFailureListener {
                Log.e("firebase", "Error file", )
            }
        }




        hora.text = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(
            ListaMensajes[position].horaEnvio
        )
        mensaje.text = ListaMensajes[position].mensaje

        val parametrosDelContenedor =  holder.itemView.contenedorMensaje.layoutParams


        if(ListaMensajes[position].EsMiMensaje){ //si es mi mensaje lo ponemos a la derecha//




            val nuevosParametros = FrameLayout.LayoutParams(

                parametrosDelContenedor.width,
                parametrosDelContenedor.height,
                Gravity.END

            )


            //contenedor.layoutParams = nuevosParametros
            holder.itemView.contenedorMensaje.layoutParams =  nuevosParametros

        }else{ //si no es mi mensaje lo ponemos a la izquierda//



            val nuevosParametros = FrameLayout.LayoutParams(

                parametrosDelContenedor.width,
                parametrosDelContenedor.height,
                Gravity.START

            )

            holder.itemView.contenedorMensaje.layoutParams =  nuevosParametros

        }

    }

    override fun getItemCount(): Int {
        return  ListaMensajes.size
    }

    override fun getItemViewType(position: Int): Int {

        if(ListaMensajes[position].EsMiMensaje){
            return 1
        }else{
                return 0
        }

        return super.getItemViewType(position)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode){
            1000-> if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){

            }else{
                Toast.makeText(context,"Permiso denegsdo",Toast.LENGTH_LONG).show()
            }
        }
    }



}


/*
1.- Como se va a ver un elemento
2.- Cuantos elementos va a tener una lista
3.- La informacion de cada elemento
*/