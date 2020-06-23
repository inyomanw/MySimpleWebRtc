package com.inyomanw.mysimplewebrtc.model

import com.google.gson.annotations.SerializedName

data class Peer(
    @SerializedName("data")
    val `data`: List<PeerData>,
    @SerializedName("type")
    val type: String
)

data class PeerData(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("user_agent")
    val userAgent: String
)