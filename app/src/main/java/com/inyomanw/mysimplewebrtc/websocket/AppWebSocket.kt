package com.inyomanw.mysimplewebrtc.websocket

import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

class AppWebSocket(uri: URI, private val callback: AppWebSocketCallback) : WebSocketClient(uri) {
    interface AppWebSocketCallback {
        fun onOpen(handshakedata: ServerHandshake?)
        fun onClose(code: Int, reason: String?, remote: Boolean)
        fun onMessage(message: String?)
        fun onError(ex: Exception?)
    }

    override fun onOpen(handshakedata: ServerHandshake?) {
        callback.onOpen(handshakedata)
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        callback.onClose(code, reason, remote)
    }

    override fun onMessage(message: String?) {
        callback.onMessage(message)
    }

    override fun onError(ex: Exception?) {
        callback.onError(ex)
    }

}