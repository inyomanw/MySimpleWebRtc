package com.inyomanw.mysimplewebrtc.model

import com.google.gson.annotations.SerializedName

data class MessageAnswer (
    @SerializedName("type")
    val type: String,
    @SerializedName("sdp")
    val sdp: String,
    @SerializedName("to")
    val to: String,
    @SerializedName("session_id")
    val sessionId: String,
    @SerializedName("description")
    val description: DataDescription
)