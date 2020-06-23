package com.inyomanw.mysimplewebrtc.model

import com.google.gson.annotations.SerializedName

class MessageCandidate(
    @field:SerializedName("type")
    var type: String, @field:SerializedName("to")
    var to: Int, @field:SerializedName("candidate")
    var candidate: String, @field:SerializedName("session_id")
    var sessionId: String
)