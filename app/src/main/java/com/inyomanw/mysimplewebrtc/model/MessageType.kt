package com.inyomanw.mysimplewebrtc.model

import com.google.gson.annotations.SerializedName

data class MessageType (
    @SerializedName("type")
    val type: String
)