package com.inyomanw.mysimplewebrtc.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.inyomanw.mysimplewebrtc.model.*
import com.inyomanw.mysimplewebrtc.ui.MainActivity.Companion.LOGNYO
import com.inyomanw.mysimplewebrtc.websocket.SignallingClient
import org.webrtc.IceCandidate

class MainViewModel : ViewModel() {
    private lateinit var client: SignallingClient

    private val iceServers: MutableLiveData<List<String>> = MutableLiveData()
    private val answerData: MutableLiveData<MessageData> = MutableLiveData()
    private val offerData: MutableLiveData<MessageData> = MutableLiveData()
    private val peerList: MutableLiveData<List<PeerData>> = MutableLiveData()
    private val iceCandidates: MutableLiveData<MessageIncomingCandidate> = MutableLiveData()

    private val isConnected = MutableLiveData<Boolean>()

    private val isSpeakerActive: MutableLiveData<Boolean> = MutableLiveData()
    private val isMuteActive: MutableLiveData<Boolean> = MutableLiveData()

    private var localId: Int? = null
    private var remoteId: Int? = null
    private var sessionId: String? = null


    fun initializeClient(userId: Int) {
        localId = userId
        client = SignallingClient(userId, object : SignallingClient.SignallingCallback {
            override fun onPeers(peers: MutableList<PeerData>) {
                peers.forEachIndexed { index, peerData ->
                    Log.d(LOGNYO, "onpeers index $index : ${peerData.id}")
                }
                peerList.postValue(peers)
            }

            override fun onOffer(data: MessageData) {
                Log.d(LOGNYO, "onOffer")
                offerData.postValue(data)
            }

            override fun onCandidateReceived(candidate: MessageIncomingCandidate?) {
                Log.d(LOGNYO, "onCandidateReceived")
                candidate?.let {
                    remoteId = candidate.data.from.toInt()
                    sessionId = "${candidate.data.from}-$localId"
                    iceCandidates.postValue(it)
                }
            }

            override fun onAnswer(data: MessageData) {
                Log.d(LOGNYO, "onAnswer data : $data")
                answerData.postValue(data)
            }

            override fun onHangup() {
                Log.d(LOGNYO, "onHangup")
            }

            override fun onOpen(userId: String) {
                Log.d(LOGNYO, "onHangup")
            }

        })
    }

    fun observeIceServers(): LiveData<List<String>> = iceServers
    fun observeIceCandidate(): LiveData<MessageIncomingCandidate> = iceCandidates
    fun observeAnswerData(): LiveData<MessageData> = answerData
    fun observeOfferData(): LiveData<MessageData> = offerData
    fun observePeers(): LiveData<List<PeerData>> = peerList

    fun observeIsConnected(): LiveData<Boolean> = isConnected

    fun observeIsSpeakerActive(): LiveData<Boolean> = isSpeakerActive
    fun observeIsMuteActive(): LiveData<Boolean> = isMuteActive

    fun getIceServers() {
        iceServers.value = listOf("stun:stun.l.google.com:19302")
    }

    fun sendIceCandidate(iceCandidate: IceCandidate, to: String, sessionId: String) {
        Log.d(LOGNYO, "sendCandidate $iceCandidate | to $to | sessionId $sessionId")
        client.sendIceCandidate(iceCandidate, to, sessionId)
    }

    fun doCall(peerId: String, sessionId: String, description: String?) {
        description?.let {
            val data = DataDescription("offer", it)
            val messageOffer =
                MessageInvite("offer", peerId, "video", data, sessionId)
            client.sendOffer(messageOffer)
        }
    }

    fun doHangUp(sessionId: String) {
        if (!::client.isInitialized) {
            return
        }

        client.close(sessionId)
        client.destroy()
    }

    fun isConnected(connected: Boolean) {
        isConnected.postValue(connected)
    }

    fun speakerStatus(speakerphoneOn: Boolean) {
        isSpeakerActive.postValue(speakerphoneOn)
    }

    fun muteStatus(enabled: Boolean) {
        isMuteActive.postValue(enabled)
    }
}