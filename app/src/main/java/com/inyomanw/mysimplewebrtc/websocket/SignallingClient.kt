package com.inyomanw.mysimplewebrtc.websocket

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.inyomanw.mysimplewebrtc.model.*
import com.inyomanw.mysimplewebrtc.ui.CallActivity.Companion.LOGNYO
import org.java_websocket.handshake.ServerHandshake
import org.json.JSONObject
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription
import java.net.URI

class SignallingClient {
    companion object {
        val instance by lazy {
            SignallingClient()
        }
    }

    interface SignallingCallback {
        fun onPeers(peers: MutableList<PeerData>)
        fun onOffer(data: MessageData)
    }

    interface IceCandidateCallback {
        fun onCandidateReceived(candidate: MessageIncomingCandidate?)
        fun onAnswer(data: MessageData)
        fun onHangup()
    }

    var iceCandidateCallback: IceCandidateCallback? = null
    val iceCandidates: MutableList<MessageIncomingCandidate> = mutableListOf()
    private lateinit var socket: AppWebSocket
    var userId: String = ""

    fun init(callback: SignallingCallback) {
        userId = (100000..999999).random().toString()
        socket = AppWebSocket(URI("wss://semut.baggrek.com:4443"), object : AppWebSocket.AppWebSocketCallback {
            override fun onOpen(handshakedata: ServerHandshake?) {
                Log.d(LOGNYO, "onOpen SignalingClient")
                createRoom()
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                Log.d(LOGNYO,"onClose SignalingClient code : $code ; reason : $reason")
            }

            override fun onMessage(message: String?) {
                Log.d(LOGNYO, "onMessage SignalingClient : $message")
                val gson = Gson()
                val messageType = gson.fromJson<MessageType>(message, MessageType::class.java)
                Log.d(LOGNYO, "onMessage SignalingClient : ${messageType.type}")
                when (messageType.type) {
                    "peers" -> {
                        val peers = gson.fromJson<Peer>(message, Peer::class.java)
                        callback.onPeers(peers.data.toMutableList())
                    }
                    "offer" -> {
                        val signalMessage = gson.fromJson<MessageOffer>(message, MessageOffer::class.java)
                        callback.onOffer(signalMessage.data)
                    }
                    "candidate" -> {
                        val candidate =
                            gson.fromJson<MessageIncomingCandidate>(message, MessageIncomingCandidate::class.java)
                        if(iceCandidateCallback != null){
                            iceCandidateCallback?.onCandidateReceived(candidate)
                        } else {
                            iceCandidates.add(candidate)
                        }
                    }
                    "answer" -> {
                        val signalMessage = gson.fromJson<MessageOffer>(message, MessageOffer::class.java)
                        iceCandidateCallback?.onAnswer(signalMessage.data)
                    }
                    "bye" -> {
                        iceCandidateCallback?.onHangup()
                    }
                }
            }

            override fun onError(ex: Exception?) {
                Log.d(LOGNYO, "onError : ${ex?.localizedMessage}")
                ex?.printStackTrace()
            }

        })
        socket.connect()
    }

    private fun createRoom() {
        val gson = Gson()
        val obj = Room("new", "Android/P", "MobileApp [$userId]", userId.toString())
        val jsonObject = JSONObject(gson.toJson(obj))
        socket.send(jsonObject.toString())
    }

    fun sendIceCandidate(iceCandidate: IceCandidate, from: Int, sessionId: String) {
        val obj = JsonObject()
        val ice = JsonObject()

        ice.addProperty("sdpMLineIndex", iceCandidate.sdpMLineIndex)
        ice.addProperty("sdpMid", iceCandidate.sdpMid)
        ice.addProperty("candidate", iceCandidate.sdp)

        obj.addProperty("type", "candidate")
        obj.addProperty("to", from)
        obj.addProperty("session_id", sessionId)
        obj.add("candidate", ice)

        Log.d(LOGNYO,"SignalingClient sendIceCandidate obj : $obj")
        socket.send(obj.toString())
    }

    fun sendOffer(messageOffer: MessageInvite) {
        val gson = Gson()
        socket.send(gson.toJson(messageOffer))
    }

    fun close(to: String, sessionId: String) {
        val obj = JsonObject()
        obj.addProperty("type", "bye")
        obj.addProperty("session_id", sessionId)
        obj.addProperty("from", userId)
        Log.d(LOGNYO,"SignalingClient close obj : $obj")
        socket.send(obj.toString())
        socket.close()
    }

    fun sendAnswer(to: String, sessionId: String, sessionDescription: SessionDescription) {
        Log.d(LOGNYO,"SignalingClient sendAnswer to: $to ; sessionId: $sessionId")
        val obj = JsonObject()
        val desc = JsonObject()
        desc.addProperty("sdp", sessionDescription.description)
        desc.addProperty("type","answer")

        obj.addProperty("type","answer")
        obj.addProperty("sdp", sessionDescription.description )
        obj.add("description", desc)
        obj.addProperty("to", to)
        obj.addProperty("session_id", sessionId)
        Log.d(LOGNYO,"SignalingClient sendAnswer obj : $obj")

        socket.send(obj.toString())
    }

    fun onDestroy() {
        if (::socket.isInitialized) {
            socket.close()
        }
    }
}