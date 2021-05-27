package com.project.poi.modelos

import android.media.Image
import com.google.firebase.database.Exclude



class MuestraGroup (
    var id: String = "",
    var nombregrupo: String = "",

    ) {
    @Exclude
    val icon: Int = 0;
}