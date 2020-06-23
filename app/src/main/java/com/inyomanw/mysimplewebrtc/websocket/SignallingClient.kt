package com.inyomanw.mysimplewebrtc.websocket

import android.util.Log
import com.google.gson.Gson
import com.inyomanw.mysimplewebrtc.model.*
import org.java_websocket.handshake.ServerHandshake
import org.json.JSONObject
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription
import java.net.URI

class SignallingClient(private val userId: Int, private val callback: SignallingCallback) {

    interface SignallingCallback {
        fun onPeers(peers: MutableList<PeerData>)
        fun onOffer(data: MessageData)
        fun onCandidateReceived(candidate: MessageIncomingCandidate?)
        fun onAnswer(data: MessageData)
        fun onHangup()
        fun onOpen(userId: String)
    }

    private var socket: AppWebSocket


    init {
        //wss://rtc.tutore.id:5553
        //wss://semut.baggrek.com:4443
        socket = AppWebSocket(URI("wss://semut.baggrek.com:4443"), object : AppWebSocket.AppWebSocketCallback {
            override fun onOpen(handshakedata: ServerHandshake?) {
                Log.d("lognyo", "Signalling Client $userId")
                createRoom(userId.toString())
                callback.onOpen(userId.toString())
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                Log.d("lognyo","onClose SignalingClient code : $code ; reason : $reason")
            }

            override fun onMessage(message: String?) {
                Log.d("lognyo","onMessage SignallingClient message : $message")
                val gson = Gson()
                val messageType = gson.fromJson<MessageType>(message, MessageType::class.java)
                Log.d("lognyo", messageType.type)
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
                        callback.onCandidateReceived(candidate)
                    }
                    "answer" -> {
                        val signalMessage = gson.fromJson<MessageOffer>(message, MessageOffer::class.java)
                        callback.onAnswer(signalMessage.data)
                    }
                    "bye" -> {
                        callback.onHangup()
                    }
                }
            }

            override fun onError(ex: Exception?) {
                Log.d("lognyo","Signalling Clinet message : ${ex?.localizedMessage}")
                ex?.printStackTrace()
            }

        })
        socket.connect()
    }

    fun createRoom(userId: String) {
        val obj = Room("new", "Android/P", "MobileApp [$userId]", userId)
        sendMessage(obj)
    }

    fun sendIceCandidate(iceCandidate: IceCandidate, to: String?, sessionId: String?) {
        if (to == null || sessionId == null) {
            return
        }

        val candidate = Candidate(iceCandidate.sdp, iceCandidate.sdpMLineIndex, iceCandidate.sdpMid)
        val message = MessageSendIceCandidate("candidate", to, sessionId, candidate)
        sendMessage(message)
    }

    fun ping() {
        if (socket.isOpen) {
            sendMessage(MessageType("keepalive"))
        }
    }

    fun sendOffer(messageOffer: MessageInvite) {
        sendMessage(messageOffer)
    }

    fun close(sessionId: String?) {
        if (sessionId == null) {
            return
        }

        val message = MessageBye("bye", sessionId, userId.toString())
        sendMessage(message)
    }

    fun sendAnswer(to: String, sessionId: String, sessionDescription: SessionDescription) {
        val description = DataDescription("answer", sessionDescription.description)
        val message = MessageAnswer("answer", sessionDescription.description, to, sessionId, description)
        sendMessage(message)
    }

    fun destroy() {
        socket.close()
    }

    private fun sendMessage(message: Any) {
        if (!socket.isOpen) {
            return
        }
        val gson = Gson()
        val jsonObject = JSONObject(gson.toJson(message))
        socket.send(jsonObject.toString())
    }
}