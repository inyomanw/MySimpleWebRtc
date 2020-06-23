package com.inyomanw.mysimplewebrtc.websocket

import android.util.Log
import com.inyomanw.mysimplewebrtc.ui.MainActivity.Companion.LOGNYO
import org.webrtc.*

open class CustomPeerConnectionObserver(value: String) : PeerConnection.Observer {
    private val className = "CustomPeerConnectionObserver"

    override fun onSignalingChange(signalingState: PeerConnection.SignalingState) {
        Log.d(LOGNYO, "$className onSignalingChange() called with: signalingState = [$signalingState]")
    }

    override fun onIceConnectionChange(iceConnectionState: PeerConnection.IceConnectionState) {
        Log.d(LOGNYO, "$className onIceConnectionChange() called with: iceConnectionState = [$iceConnectionState]")
    }

    override fun onIceConnectionReceivingChange(b: Boolean) {
        Log.d(LOGNYO, "$className onIceConnectionReceivingChange() called with: b = [$b]")
    }

    override fun onIceGatheringChange(iceGatheringState: PeerConnection.IceGatheringState) {
        Log.d(LOGNYO, "$className onIceGatheringChange() called with: iceGatheringState = [$iceGatheringState]")
    }

    override fun onIceCandidate(iceCandidate: IceCandidate) {
        Log.d(LOGNYO, "$className onIceCandidate() called with: iceCandidate = [$iceCandidate]")
    }

    override fun onIceCandidatesRemoved(iceCandidates: Array<IceCandidate>) {
        Log.d(LOGNYO, "$className onIceCandidatesRemoved() called with: iceCandidates = [$iceCandidates]")
    }

    override fun onAddStream(mediaStream: MediaStream) {
        Log.d(LOGNYO, "$className onAddStream() called with: mediaStream = [$mediaStream]")
    }

    override fun onRemoveStream(mediaStream: MediaStream) {
        Log.d(LOGNYO, "onRemoveStream() called with: mediaStream = [$mediaStream]")
    }

    override fun onDataChannel(dataChannel: DataChannel) {
        Log.d(LOGNYO, "$className onDataChannel() called with: dataChannel = [$dataChannel]")
    }

    override fun onRenegotiationNeeded() {
        Log.d(LOGNYO, "$className onRenegotiationNeeded() called")
    }

    override fun onAddTrack(rtpReceiver: RtpReceiver, mediaStreams: Array<MediaStream>) {
        Log.d(LOGNYO, "$className onAddTrack() called with: rtpReceiver = [$rtpReceiver], mediaStreams = [$mediaStreams]")
    }
}