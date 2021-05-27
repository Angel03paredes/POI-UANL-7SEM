package com.project.poi.modelos

import android.media.Image
import com.google.firebase.database.Exclude



class MuestraUser (
    var id: String = "",
    var usuario: String = "",

) {
    @Exclude
    val icon: Int = 0;
}
