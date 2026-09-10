package com.example.movilesa20262

import com.google.firebase.Timestamp

data class Post(
    val texto: String = "",
    val fecha: Timestamp? = null
)