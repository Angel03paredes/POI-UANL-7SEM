package com.project.poi.modelos

import com.google.firebase.database.Exclude



class ChatMensaje (
   // var id: String = "",
    var mensaje: String = "",
    var usuario: String = "",
    val horaEnvio: Any? = null,
    var to:String="",
    var encrypt:Boolean = false,
    var hayArchivo:Boolean = false,
    var uriArchivo:String? =""
) {
    @Exclude
    var EsMiMensaje: Boolean = false
}
