package com.inyomanw.mysimplewebrtc.model

import com.google.gson.annotations.SerializedName

data class DataDescription(
    @SerializedName("type")
    var type: String,
    @SerializedName("sdp")
    var sdp: String
)
