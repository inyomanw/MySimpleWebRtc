package com.inyomanw.mysimplewebrtc.websocket

import android.util.Log
import com.inyomanw.mysimplewebrtc.ui.MainActivity.Companion.LOGNYO
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription

open class CustomSdpObserver(logTag: String) : SdpObserver {


    private var tag = "CustomSdpObserver"

    override fun onCreateSuccess(sessionDescription: SessionDescription) {
        Log.d(LOGNYO, "$tag onCreateSuccess() called with: sessionDescription = [$sessionDescription]")
    }

    override fun onSetSuccess() {
        Log.d(LOGNYO, "$tag onSetSuccess() called")
    }

    override fun onCreateFailure(s: String) {
        Log.d(LOGNYO, "$tag onCreateFailure() called with: s = [$s]")
    }

    override fun onSetFailure(s: String) {
        Log.d(LOGNYO, "$tag onSetFailure() called with: s = [$s]")
    }

}