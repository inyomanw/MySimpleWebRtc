package com.inyomanw.mysimplewebrtc.model
import com.google.gson.annotations.SerializedName


data class MessageIncomingCandidate(
    @SerializedName("data")
    val `data`: Data,
    @SerializedName("type")
    val type: String
)

data class Data(
    @SerializedName("candidate")
    val candidate: Candidate,
    @SerializedName("from")
    val from: String,
    @SerializedName("to")
    val to: String
)

data class Candidate(
    @SerializedName("candidate")
    val candidate: String,
    @SerializedName("sdpMLineIndex")
    val sdpMLineIndex: Int,
    @SerializedName("sdpMid")
    val sdpMid: String
)