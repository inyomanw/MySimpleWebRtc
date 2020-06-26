package com.inyomanw.mysimplewebrtc.websocket

import android.util.Log
import org.webrtc.*

open class CustomPeerConnectionObserver(value: String) : PeerConnection.Observer {
    private val logCutomPeer = "logCustomPeer"
    private val className = "CustomPeerConnectionObserver"

    override fun onSignalingChange(signalingState: PeerConnection.SignalingState) {
        Log.d(logCutomPeer, "$className onSignalingChange() called with: signalingState = [$signalingState]")
    }

    override fun onIceConnectionChange(iceConnectionState: PeerConnection.IceConnectionState) {
        Log.d(logCutomPeer, "$className onIceConnectionChange() called with: iceConnectionState = [$iceConnectionState]")
    }

    override fun onIceConnectionReceivingChange(b: Boolean) {
        Log.d(logCutomPeer, "$className onIceConnectionReceivingChange() called with: b = [$b]")
    }

    override fun onIceGatheringChange(iceGatheringState: PeerConnection.IceGatheringState) {
        Log.d(logCutomPeer, "$className onIceGatheringChange() called with: iceGatheringState = [$iceGatheringState]")
    }

    override fun onIceCandidate(iceCandidate: IceCandidate) {
        Log.d(logCutomPeer, "$className onIceCandidate() called with: iceCandidate = [$iceCandidate]")
    }

    override fun onIceCandidatesRemoved(iceCandidates: Array<IceCandidate>) {
        Log.d(logCutomPeer, "$className onIceCandidatesRemoved() called with: iceCandidates = [$iceCandidates]")
    }

    override fun onAddStream(mediaStream: MediaStream) {
        Log.d(logCutomPeer, "$className onAddStream() called with: mediaStream = [$mediaStream]")
    }

    override fun onRemoveStream(mediaStream: MediaStream) {
        Log.d(logCutomPeer, "onRemoveStream() called with: mediaStream = [$mediaStream]")
    }

    override fun onDataChannel(dataChannel: DataChannel) {
        Log.d(logCutomPeer, "$className onDataChannel() called with: dataChannel = [$dataChannel]")
    }

    override fun onRenegotiationNeeded() {
        Log.d(logCutomPeer, "$className onRenegotiationNeeded() called")
    }

    override fun onAddTrack(rtpReceiver: RtpReceiver, mediaStreams: Array<MediaStream>) {
        Log.d(logCutomPeer, "$className onAddTrack() called with: rtpReceiver = [$rtpReceiver], mediaStreams = [$mediaStreams]")
    }
}