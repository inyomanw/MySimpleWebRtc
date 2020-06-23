package com.inyomanw.mysimplewebrtc.model

import com.google.gson.annotations.SerializedName

data class MessageBye(
    @SerializedName("type")
    val type: String,
    @SerializedName("session_id")
    val sessionId: String,
    @SerializedName("from")
    val from: String
)