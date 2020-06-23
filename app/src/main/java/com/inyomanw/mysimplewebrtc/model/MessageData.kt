package com.inyomanw.mysimplewebrtc.model

import com.google.gson.annotations.SerializedName

data class MessageData (
    @SerializedName("to")
    var to: Int,
    @SerializedName("from")
    var from: Int,
    @SerializedName("media")
    var media: String,
    @SerializedName("session_id")
    var sessionId: String?,
    @SerializedName("description")
    var description: DataDescription
)
