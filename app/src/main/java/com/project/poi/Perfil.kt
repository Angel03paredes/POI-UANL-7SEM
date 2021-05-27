package com.project.poi

import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Switch
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.github.drjacky.imagepicker.ImagePicker
import com.github.drjacky.imagepicker.constant.ImageProvider
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.OnProgressListener
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_perfil.view.*
import kotlinx.android.synthetic.main.layout_ver_puntos.*
import java.io.File
import java.util.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [fragment2.newInstance] factory method to
 * create an instance of this fragment.
 */
class fragment2 : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var childKey:String

    private val storage = FirebaseStorage.getInstance().reference
    lateinit var dialog:Dialog

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

        val view = inflater.inflate(R.layout.fragment_perfil, container, false)
        dialog = Dialog(requireContext())


        var sharedPreferences : SharedPreferences = requireContext().getSharedPreferences("SharedP", Context.MODE_PRIVATE)
        val email = sharedPreferences.getString("email", "")
        val name = sharedPreferences.getString("userName", "")
        val key = sharedPreferences.getString("key", "")
        val changeStatus = sharedPreferences.getString("status","" )
        val encrypt = view.findViewById<Switch>(R.id.encryptSwitch)
        val statusS = view.findViewById<Switch>(R.id.statusSwitch)
        val database = FirebaseDatabase.getInstance()
        val ref = database.getReference("users/" + key + "/encrypt")


        ref.get().addOnSuccessListener {
            val itValue:String = it.value.toString()
            encrypt.setChecked(itValue.toBoolean())

            sharedPreferences = requireContext().getSharedPreferences("SharedP", Context.MODE_PRIVATE)
            val editor:SharedPreferences.Editor = sharedPreferences.edit()
            editor.putBoolean("encrypt",itValue.toBoolean())
            editor.apply()

        }.addOnFailureListener {
            print("Error")
        }



        
        encrypt.setOnCheckedChangeListener { compoundButton, b ->
            database.getReference("users/" + key + "/encrypt").setValue(b)
            sharedPreferences = requireContext().getSharedPreferences("SharedP", Context.MODE_PRIVATE)
            val editor:SharedPreferences.Editor = sharedPreferences.edit()
            editor.putBoolean("encrypt",b)
            editor.apply()

        }


        //Set Online
        statusS.setChecked(true)







        statusS.setOnCheckedChangeListener { compoundButton, b ->

           // sharedPreferences = requireContext().getSharedPreferences("SharedP", Context.MODE_PRIVATE)
            val editor:SharedPreferences.Editor = sharedPreferences.edit()
            if(b) {
               database.getReference("users/" + key + "/status").setValue("true")
                editor.putString("status", "true")
                editor.apply()
            }
            else {
               database.getReference("users/" + key + "/status").setValue("false")
                editor.putString("status", "false")
                editor.apply()
            }


        }

        //view.btnVerPts.setOnClickListener {

          //  var changeStatus: String? = sharedPreferences.getString("status","")
           // Toast.makeText(requireContext(),changeStatus + " : status",Toast.LENGTH_LONG).show()
        //}



        view.txtPerfilEmail.text = email
        view.txtPerfilName.text = name

        getUserAvatar()


        view.btnSignOut.setOnClickListener {
            val sharedPreferences : SharedPreferences = requireContext().getSharedPreferences(
                "SharedP",
                Context.MODE_PRIVATE
            )
            val editor: SharedPreferences.Editor = sharedPreferences.edit()
            editor.putString("email", "")
            editor.putString("password", "")
            editor.apply()
            val intent = Intent(activity, MainActivity::class.java)
            startActivity(intent)

        }

        view.btnImageUpload.setOnClickListener {
            val intentGal = ImagePicker.with(requireActivity())
                .crop(1f, 1f)        //Crop image(Optional), Check Customization for more option
                .cropOval()    //Allow dimmed layer to have a circle inside
                .provider(ImageProvider.GALLERY)
                .maxResultSize(1080, 1080) //Final image resolution will be less than 1080 x 1080(Optional)
                .createIntent()
            startActivityForResult(intentGal, 1)
        }

        view.btnVerPts.setOnClickListener {
            showPuntuacion(key.toString())
        }


        return  view
    }

    private fun showPuntuacion(key:String) {
        dialog.setContentView(R.layout.layout_ver_puntos)
        dialog.show()

        dialog.btn_close.setOnClickListener {
            dialog.dismiss()
        }

        dialog.btn_aceptar.setOnClickListener {
            dialog.dismiss()
        }
        val database = FirebaseDatabase.getInstance()
        val refNombre = database.getReference("users/" + key + "/puntos")
        refNombre.get().addOnSuccessListener {
            dialog.txt_puntos.text = it.value.toString() + " Puntos"
        }

    }

    private fun getUserAvatar(){
        val database = FirebaseDatabase.getInstance()
        var uuid:String ="";
        val sharedPreferences : SharedPreferences = requireContext().getSharedPreferences("SharedP", Context.MODE_PRIVATE)
        val currentUser = sharedPreferences.getString("email", "")
        val key = sharedPreferences.getString("key", "")

        val ref = database.getReference("users/" + key + "/avatar")
        ref.get().addOnSuccessListener {
            val fotoLoad = storage.child("avatarUsuario/" + it.value)
            fotoLoad.downloadUrl.addOnSuccessListener {
                val imgProfile = view?.findViewById<ImageView>(R.id.imgProfile)
                Picasso.get().load(it).into(imgProfile)
            }.addOnFailureListener {
                Log.e("firebase", "Error avatar", )
            }
        }.addOnFailureListener {
            Log.e("firebase", "Error getting data", )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            //Image Uri will not be null for RESULT_OK
            val fileUri = data?.data

            view?.findViewById<ImageView>(R.id.imgProfile)?.setImageURI(fileUri)

            //You can get File object from intent
            val file: File = ImagePicker.getFile(data)!!
            saveImage(file)
            //You can also get File Path from intent
            val filePath:String = ImagePicker.getFilePath(data)!!
        } else if (resultCode == ImagePicker.RESULT_ERROR) {

        } else {

        }
    }

    private  fun saveImage(file:File){
        val database = FirebaseDatabase.getInstance()
        val pd : ProgressDialog = ProgressDialog(requireActivity())
        pd.setTitle("Subiendo Imagen")
        pd.show()

        val sharedPreferences : SharedPreferences = requireContext().getSharedPreferences("SharedP", Context.MODE_PRIVATE)

        val key = sharedPreferences.getString("key", "")
        val uuid  = UUID.randomUUID().toString()
        val storageRef = storage.child("avatarUsuario/" + uuid)

        storageRef.putFile(Uri.fromFile(file))
            .addOnSuccessListener {
                database.getReference("users/" + key + "/avatar").setValue(uuid)


                Toast.makeText(requireActivity(),"Subiendo imagen",Toast.LENGTH_SHORT).show()
                pd.dismiss()
            }
            .addOnFailureListener{
                pd.dismiss()
                Toast.makeText(requireActivity(),"Error al subir imagen",Toast.LENGTH_LONG).show()
            }
            .addOnProgressListener(OnProgressListener<UploadTask.TaskSnapshot>() {

                var progress:Double = 100.0 * (it.getBytesTransferred() / it.getTotalByteCount());
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
         * @return A new instance of fragment fragment2.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            fragment2().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onStop() {
        super.onStop()
    }

}