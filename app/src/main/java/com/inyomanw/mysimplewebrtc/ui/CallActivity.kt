package com.inyomanw.mysimplewebrtc.ui

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.inyomanw.mysimplewebrtc.R
import com.inyomanw.mysimplewebrtc.model.DataDescription
import com.inyomanw.mysimplewebrtc.model.MessageData
import com.inyomanw.mysimplewebrtc.model.MessageIncomingCandidate
import com.inyomanw.mysimplewebrtc.model.MessageInvite
import com.inyomanw.mysimplewebrtc.websocket.CustomPeerConnectionObserver
import com.inyomanw.mysimplewebrtc.websocket.CustomSdpObserver
import com.inyomanw.mysimplewebrtc.websocket.SignallingClient
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_call.*
import org.webrtc.*
import java.util.ArrayList

class CallActivity : AppCompatActivity(), SignallingClient.IceCandidateCallback {

    companion object {
        const val LOGNYO = "lognyo"
    }

    private val userId by lazy {
        intent.extras?.getString("ID")
    }
    private var sessionId: String? = null
    private val isOffer by lazy {
        intent.extras?.getBoolean("IS_OFFER")
    }
    private lateinit var peerConnectionFactory: PeerConnectionFactory
    private lateinit var audioConstraints: MediaConstraints
    private lateinit var videoConstraints: MediaConstraints
    private lateinit var sdpConstraints: MediaConstraints
    private lateinit var videoSource: VideoSource
    private lateinit var localVideoTrack: VideoTrack
    private lateinit var audioSource: AudioSource
    private lateinit var localAudioTrack: AudioTrack

    private lateinit var localVideoView: SurfaceViewRenderer
    private lateinit var remoteVideoView: SurfaceViewRenderer
    private lateinit var rootEglBase: EglBase
    private var peerIceServers: MutableList<PeerConnection.IceServer> = ArrayList()
    private var localPeer: PeerConnection? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call)

        Dexter.withActivity(this)
            .withPermissions(
                Manifest.permission.CAMERA,
                Manifest.permission.CAPTURE_AUDIO_OUTPUT,
                Manifest.permission.MEDIA_CONTENT_CONTROL,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.MODIFY_AUDIO_SETTINGS
            )
            .withListener(object : MultiplePermissionsListener {
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

        initVideos()
        getIceServers()

        start()

        checkIceCandidates()

        end_call.setOnClickListener {
            hangup()
        }
        SignallingClient.instance.iceCandidateCallback = this
    }

    private fun initVideos() {
        rootEglBase = EglBase.create()
        localVideoView = local_gl_surface_view
        remoteVideoView = remote_gl_surface_view
        localVideoView.init(rootEglBase.eglBaseContext, null)
        remoteVideoView.init(rootEglBase.eglBaseContext, null)
        localVideoView.setZOrderMediaOverlay(true)
        remoteVideoView.setZOrderMediaOverlay(true)
    }

    private fun getIceServers() {
        val peerIceServer = PeerConnection.IceServer.builder("stun:stun.l.google.com:19302")
            .createIceServer()
        peerIceServers.add(peerIceServer)

        val peerIceUdp = PeerConnection.IceServer
            .builder("turn:13.250.13.83:3478?transport=udp")
            .setUsername("YzYNCouZM1mhqhmseWk6")
            .setPassword("YzYNCouZM1mhqhmseWk6")
            .createIceServer()
        peerIceServers.add(peerIceUdp)

        val peerIceTcp = PeerConnection.IceServer
            .builder("turn:192.158.29.39:3478?transport=tcp")
            .setUsername("28224511:1379330808")
            .setPassword("JZEOEt2V3Qb0y27GRntt2u2PAYA=")
            .createIceServer()
        peerIceServers.add(peerIceTcp)
    }

    private fun start() {
        //1. Create and initialize PeerConnectionFactory
        val initializationOptions = PeerConnectionFactory.InitializationOptions.builder(this)
            .setEnableVideoHwAcceleration(true)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(initializationOptions)

        val options = PeerConnectionFactory.Options()
        val defaultVideoEncoderFactory = DefaultVideoEncoderFactory(
            rootEglBase.eglBaseContext, /* enableIntelVp8Encoder */
            true, /* enableH264HighProfile */
            true
        )
        val defaultVideoDecoderFactory = DefaultVideoDecoderFactory(rootEglBase.eglBaseContext)
//        peerConnectionFactory = PeerConnectionFactory(options, defaultVideoEncoderFactory, defaultVideoDecoderFactory)
        peerConnectionFactory = PeerConnectionFactory
            .builder()
            .setOptions(options)
            .setVideoEncoderFactory(defaultVideoEncoderFactory)
            .setVideoDecoderFactory(defaultVideoDecoderFactory)
            .createPeerConnectionFactory()

        //Now create a VideoCapturer instance.
        //2. Create a VideoCapturer instance which uses the camera of the device
        val videoCapturerAndroid: VideoCapturer? = createCameraCapturer(Camera1Enumerator(false))


        //Create MediaConstraints - Will be useful for specifying video and audio constraints.
        audioConstraints = MediaConstraints()
        videoConstraints = MediaConstraints()

        //Create a VideoSource instance
        if (videoCapturerAndroid != null) {
            //3.Create a VideoSource from the Capturer
            videoSource = peerConnectionFactory.createVideoSource(videoCapturerAndroid)
            //4.Create a VideoTrack from the source
            localVideoTrack = peerConnectionFactory.createVideoTrack("100", videoSource)

        }

        //create an AudioSource instance
        audioSource = peerConnectionFactory.createAudioSource(audioConstraints)
        localAudioTrack = peerConnectionFactory.createAudioTrack("101", audioSource)


        videoCapturerAndroid?.startCapture(1024, 720, 30)
        localVideoView.visibility = View.VISIBLE
        // And finally, with our VideoRenderer ready, we
        // can add our renderer to the VideoTrack.
        //5. Create a video renderer using a SurfaceViewRenderer view and add it to the VideoTrack instance
        localVideoTrack.addSink(localVideoView)

        localVideoView.setMirror(true)
        remoteVideoView.setMirror(true)

        createPeerConnection()
        if (!isOffer!!) {
            doCall()
        } else {
            doAnswer()
        }
    }

    private fun createPeerConnection() {
        val rtcConfig = PeerConnection.RTCConfiguration(peerIceServers)
        // TCP candidates are only useful when connecting to a server that supports
        // ICE-TCP.
        rtcConfig.tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED
        rtcConfig.bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE
        rtcConfig.rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE
        rtcConfig.continualGatheringPolicy =
            PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
        // Use ECDSA encryption.
//        rtcConfig.keyType = PeerConnection.KeyType.ECDSA
        localPeer = peerConnectionFactory.createPeerConnection(
            rtcConfig,
            object : CustomPeerConnectionObserver("localPeerCreation") {
                override fun onIceCandidate(iceCandidate: IceCandidate) {
                    super.onIceCandidate(iceCandidate)
                    Log.d(LOGNYO, "CallAct createPeerConnection() onIceCandidate: $iceCandidate")
                    onIceCandidateReceived(iceCandidate)
                }

                override fun onAddStream(mediaStream: MediaStream) {
                    super.onAddStream(mediaStream)
                    Log.d(LOGNYO, "CallAct createPeerConnection() onAddStream: $mediaStream")
                    gotRemoteStream(mediaStream)
                }
            })

        addStreamToLocalPeer()
    }

    private fun checkIceCandidates() {
        SignallingClient.instance.iceCandidates.forEach {
            onCandidateReceived(it)
        }
    }

    private fun doAnswer() {
        Log.d(LOGNYO, "CallAct doAnswer")
        sessionId = "$userId-${SignallingClient.instance.userId}"
        localPeer?.setRemoteDescription(
            CustomSdpObserver("localSetRemote"),
            SessionDescription(SessionDescription.Type.OFFER, intent.extras?.getString("SDP"))
        )
        localPeer?.createAnswer(object : CustomSdpObserver("localCreateAnswer") {
            override fun onCreateSuccess(sessionDescription: SessionDescription) {
                super.onCreateSuccess(sessionDescription)
                localPeer?.setLocalDescription(
                    CustomSdpObserver("localSetLocal"),
                    sessionDescription
                )
                SignallingClient.instance.sendAnswer(userId!!, sessionId!!, sessionDescription)
            }
        }, MediaConstraints())
        updateVideoViews(true)
    }

    private fun doCall() {
        Log.d(LOGNYO, "CallAct doCall")
        sessionId = "${SignallingClient.instance.userId}-$userId"
        sdpConstraints = MediaConstraints()
        sdpConstraints.mandatory.add(
            MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true")
        )
        sdpConstraints.mandatory.add(
            MediaConstraints.KeyValuePair(
                "OfferToReceiveVideo", "true"
            )
        )
        localPeer?.createOffer(object : CustomSdpObserver("localCreateOffer") {
            override fun onCreateSuccess(sessionDescription: SessionDescription) {
                super.onCreateSuccess(sessionDescription)
                localPeer?.setLocalDescription(
                    CustomSdpObserver("localSetLocalDesc"),
                    sessionDescription
                )
                val dataDescription = DataDescription("offer", sessionDescription.description)
                val messageOffer =
                    MessageInvite("offer", userId!!, "video", dataDescription, sessionId!!)
                SignallingClient.instance.sendOffer(messageOffer)
            }
        }, sdpConstraints)
    }

    private fun createCameraCapturer(enumerator: Camera1Enumerator): VideoCapturer? {
        val deviceNames = enumerator.deviceNames

        // Trying to find a front facing camera!
        for (deviceName in deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                val videoCapturer = enumerator.createCapturer(deviceName, null)

                if (videoCapturer != null) {
                    return videoCapturer
                }
            }
        }

        // We were not able to find a front cam. Look for other cameras
        for (deviceName in deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                val videoCapturer = enumerator.createCapturer(deviceName, null)

                if (videoCapturer != null) {
                    return videoCapturer
                }
            }
        }

        return null
    }



    private fun addStreamToLocalPeer() {
        Log.d(LOGNYO, "CallAct addStreamToLocalPeer")
        val stream = peerConnectionFactory.createLocalMediaStream("102")
        stream.addTrack(localAudioTrack)
        stream.addTrack(localVideoTrack)
        localPeer?.addStream(stream)
    }

    private fun gotRemoteStream(mediaStream: MediaStream) {
        Log.d(LOGNYO, "CallAct gotRemoteStream")
        val videoTrack = mediaStream.videoTracks[0]
        runOnUiThread {
            try {
                remoteVideoView.visibility = View.VISIBLE
                videoTrack.addSink(remoteVideoView)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun onIceCandidateReceived(iceCandidate: IceCandidate) {
        Log.d(LOGNYO, "CallAct onIceCandidateReceived iceCandidate: $iceCandidate")
        SignallingClient.instance.sendIceCandidate(iceCandidate, userId!!.toInt(), sessionId!!)
    }

    private fun updateVideoViews(remoteVisible: Boolean) {
        runOnUiThread {
            var params = localVideoView.layoutParams
            if (remoteVisible) {
                params.height = dpToPx(150)
                params.width = dpToPx(150)
            } else {
                params =
                    FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            }
            localVideoView.layoutParams = params
        }

    }

    private fun dpToPx(dp: Int): Int {
        val displayMetrics = resources.displayMetrics
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT))
    }

    private fun hangup() {
        Log.d(LOGNYO, "CallAct hangup")
        localPeer?.close()
        localPeer = null
        SignallingClient.instance.close(userId!!, sessionId!!)
        updateVideoViews(false)
        finish()
    }

    override fun onCandidateReceived(candidate: MessageIncomingCandidate?) {
        val data = candidate?.data?.candidate
        Log.d(LOGNYO, "CallAct onCandidateReceived candidate data: $data")
        data?.let {
            if (it.sdpMid != null) {
                localPeer?.addIceCandidate(IceCandidate(it.sdpMid, it.sdpMLineIndex, it.candidate))
            }
        }
    }

    override fun onAnswer(data: MessageData) {
        Log.d(LOGNYO, "CallAct onAnswer data: $data")
        localPeer?.setRemoteDescription(
            CustomSdpObserver("localSetRemote"),
            SessionDescription(
                SessionDescription.Type.fromCanonicalForm(data.description.type),
                data.description.sdp
            )
        )
        updateVideoViews(true)
    }

    override fun onHangup() {
        runOnUiThread {
            hangup()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        SignallingClient.instance.onDestroy()
    }
}
