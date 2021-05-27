package com.project.poi.modelos


class User(

    val userName: String ="",
    val email: String = "",
    val password: String = "",
    val carrera: String = "",
    val encrypt : Boolean = false,
    val avatar:String ="",
    var key: String = "",
    var status:String = "",
    var puntos:Int = 0
)