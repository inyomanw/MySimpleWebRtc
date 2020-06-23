package com.inyomanw.mysimplewebrtc.model

import com.google.gson.annotations.SerializedName

data class MessageInvite(
    @field:SerializedName("type")
    var type: String, @field:SerializedName("to")
    var id: String, @field:SerializedName("media")
    var media: String, @field:SerializedName("description")
    var description: DataDescription, @field:SerializedName("session_id")
    var sessionId: String
)
