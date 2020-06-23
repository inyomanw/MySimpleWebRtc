package com.inyomanw.mysimplewebrtc.model

import com.google.gson.annotations.SerializedName

data class MessageSendIceCandidate (
    @SerializedName("type")
    val type: String,
    @SerializedName("to")
    val to: String,
    @SerializedName("session_id")
    val sessionId: String,
    @SerializedName("candidate")
    val candidate: Candidate
)