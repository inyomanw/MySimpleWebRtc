package com.inyomanw.mysimplewebrtc.ui

import android.Manifest
import android.content.Context
import android.media.AudioManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.inyomanw.mysimplewebrtc.R
import com.inyomanw.mysimplewebrtc.model.MessageData
import com.inyomanw.mysimplewebrtc.model.MessageIncomingCandidate
import com.inyomanw.mysimplewebrtc.model.PeerData
import com.inyomanw.mysimplewebrtc.websocket.CustomPeerConnectionObserver
import com.inyomanw.mysimplewebrtc.websocket.CustomSdpObserver
import androidx.lifecycle.Observer
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_main.*
import org.webrtc.*
import java.util.*

class MainActivity : AppCompatActivity() {

    companion object {
        const val LOGNYO = "lognyo"
    }

    val viewModel: MainViewModel = MainViewModel()

    private val userId = (100000..999999).random()

    private lateinit var sdpConstraints: MediaConstraints
    private lateinit var localAudioTrack: AudioTrack
    private lateinit var audioSource: AudioSource
    private lateinit var audioConstraints: MediaConstraints
    private lateinit var peerConnectionFactory: PeerConnectionFactory

    private val remoteAudioTracks: MutableList<AudioTrack> = mutableListOf()

    private var localPeer: PeerConnection? = null
    private var peerIceServers: MutableList<PeerConnection.IceServer> = ArrayList()
    private var peerId: String? = null
    private var sessionId: String? = null
    private var retryAttempt = 0
    private var startTime: Date? = null
//    private var tutorData: CallData? = null

    var isOnCall: Boolean = false

    private val peers : MutableList<PeerData> = mutableListOf()
    private val peerAdapter by lazy {
        PeerAdapter(peers, userId) {
            //            doCall(it)
        }
    }

    private val audioManager: AudioManager by lazy {
        this.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    private var localId: Int? = null
    private var remoteId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Dexter.withActivity(this)
            .withPermissions(
                Manifest.permission.CAMERA,
                Manifest.permission.CAPTURE_AUDIO_OUTPUT,
                Manifest.permission.MEDIA_CONTENT_CONTROL)
            .withListener(object: MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {

                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
//                    this@MainActivity.shouldShowRequestPermissionRationale(token)
                }

            })
            .check()


        with(rv_peers) {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = peerAdapter
        }

        viewModel.initializeClient(userId)
        viewModel.getIceServers()

        observeData()
    }

    private fun observeData() {
        with(viewModel) {
            observeIceServers().onResult {
                it.forEach { ice ->
                    Log.d("lognyo", "observeIceServers : $ice")
                    val peerIceServer = PeerConnection.IceServer.builder(ice)
                        .createIceServer()
                    peerIceServers.add(peerIceServer)
                }
                initPeerConnectionFactory()
            }
            observeIceCandidate().onResult {
                addIceCandidate(it)
            }
            observeOfferData().onResult {

            }
            observePeers().onResult {
                runOnUiThread {
                    this@MainActivity.peers.clear()
                    this@MainActivity.peers.addAll(peers)
                    this@MainActivity.peerAdapter.notifyDataSetChanged()
                }
            }
            observeAnswerData().onResult {

            }
        }
    }

    private fun addIceCandidate(candidate: MessageIncomingCandidate) {
        val data = candidate.data.candidate
        localPeer?.addIceCandidate(IceCandidate(data.sdpMid, data.sdpMLineIndex, data.candidate))
    }

    private fun initPeerConnectionFactory() {
        val initializationOptions = PeerConnectionFactory.InitializationOptions.builder(this)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(initializationOptions)

        val options = PeerConnectionFactory.Options()

        peerConnectionFactory =
            PeerConnectionFactory.builder().setOptions(options).createPeerConnectionFactory()
        audioConstraints = MediaConstraints()

        audioSource = peerConnectionFactory.createAudioSource(audioConstraints)
        localAudioTrack = peerConnectionFactory.createAudioTrack(
            "101",
            audioSource
        )

        createPeerConnection()
    }

    private fun createPeerConnection() {
        val rtcConfig = PeerConnection.RTCConfiguration(peerIceServers)
        rtcConfig.tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED
        rtcConfig.bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE
        rtcConfig.rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE
        rtcConfig.continualGatheringPolicy =
            PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY

        localPeer = peerConnectionFactory.createPeerConnection(
            rtcConfig, object : CustomPeerConnectionObserver("localPeerCreation") {
                override fun onIceCandidate(iceCandidate: IceCandidate) {
                    super.onIceCandidate(iceCandidate)
                    onIceCandidateReceived(iceCandidate)
                }

                override fun onAddStream(mediaStream: MediaStream) {
                    remoteAudioTracks.addAll(mediaStream.audioTracks)
                    adjustVolume()
                    super.onAddStream(mediaStream)
                }

                override fun onIceConnectionChange(iceConnectionState: PeerConnection.IceConnectionState) {
                    super.onIceConnectionChange(iceConnectionState)
                    when (iceConnectionState.name) {
                        "FAILED" -> {
                            runOnUiThread {
                                retryAttempt++
                                if (retryAttempt > 3) {
                                    doHangUp()
                                    return@runOnUiThread
                                }

                                doCall(peerId!!, sessionId!!)
                            }
                        }
                        "CONNECTED" -> runOnUiThread {
                            retryAttempt = 0
                            viewModel.isConnected(true)
                            startTime = Calendar.getInstance().time
                        }
                        "CLOSED", "DISCONNECTED" -> runOnUiThread { doHangUp() }
                    }
                }
            })

        addStreamToLocalPeer()
    }

    private fun addStreamToLocalPeer() {
        val stream =
            peerConnectionFactory.createLocalMediaStream("102")
        stream.addTrack(localAudioTrack)
        localPeer?.addStream(stream)
    }

    fun doCall(peer: String, session: String) {
        peerId = peer
        sessionId = session
        sdpConstraints = MediaConstraints()
        sdpConstraints.mandatory.add(
            MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true")
        )
        sdpConstraints.mandatory.add(
            MediaConstraints.KeyValuePair(
                "OfferToReceiveVideo", "false"
            )
        )
        localPeer?.createOffer(object : CustomSdpObserver("localCreateOffer") {
            override fun onCreateSuccess(sessionDescription: SessionDescription) {
                super.onCreateSuccess(sessionDescription)
                localPeer?.setLocalDescription(
                    CustomSdpObserver("localSetLocalDesc"),
                    sessionDescription
                )
                viewModel.doCall(peer, session, sessionDescription.description)
            }

            override fun onSetFailure(s: String) {
                super.onSetFailure(s)
                Log.d(LOGNYO, "onSetFailure : $s")
            }

            override fun onCreateFailure(s: String) {
                super.onCreateFailure(s)
                Log.d(LOGNYO, "onCreateFailure : $s")
            }
        }, sdpConstraints)
    }

    private fun onAnswer(data: MessageData) {
        localPeer?.setRemoteDescription(
            CustomSdpObserver("localSetRemote"),
            SessionDescription(
                SessionDescription.Type.fromCanonicalForm(data.description.type),
                data.description.sdp
            )
        )
    }

    fun doHangUp() {
        if (localPeer == null) {
            return
        }
        isOnCall = false
        clearConnection()
    }

    private fun adjustVolume() {
        try {
            remoteAudioTracks.forEach {
                val volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toDouble()
                it.setVolume(if (volume > 0.0) volume else 5.0)
            }
        } catch (e: Exception) {
        }
    }

    private fun clearConnection() {
        sessionId?.let {
//            viewModel.doHangUp(it)
        }
        try {
            localAudioTrack.dispose()
            localPeer?.dispose()
        } catch (e: Exception) {
        }
        localPeer = null
        remoteAudioTracks.clear()
    }

    private fun onIceCandidateReceived(iceCandidate: IceCandidate) {
        peerId?.let {
            viewModel.sendIceCandidate(iceCandidate, it, sessionId!!)
        }
    }


    fun <T> LiveData<T>.onResult(action: (T) -> Unit) {
        observe(this@MainActivity, Observer { data -> data?.let(action) })
    }
}
