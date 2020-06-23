package com.inyomanw.mysimplewebrtc.model

import com.google.gson.annotations.SerializedName

data class MessageOffer(
    @SerializedName("type")
    val type: String,
    @SerializedName("data")
    val data: MessageData
)