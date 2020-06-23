package com.inyomanw.mysimplewebrtc.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Room(
    @field:SerializedName("type")
    @field:Expose
    internal var type: String, @field:SerializedName("user_agent")
    @field:Expose
    internal var userAgent: String, @field:SerializedName("name")
    @field:Expose
    internal var name: String, @field:SerializedName("id")
    @field:Expose
    internal var id: String
)