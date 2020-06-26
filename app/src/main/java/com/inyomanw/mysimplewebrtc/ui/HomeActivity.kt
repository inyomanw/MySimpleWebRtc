package com.inyomanw.mysimplewebrtc.ui

import android.Manifest
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.inyomanw.mysimplewebrtc.NetworkConfig
import com.inyomanw.mysimplewebrtc.R
import com.inyomanw.mysimplewebrtc.model.MessageData
import com.inyomanw.mysimplewebrtc.model.PeerData
import com.inyomanw.mysimplewebrtc.websocket.SignallingClient
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_home.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeActivity : AppCompatActivity(), SignallingClient.SignallingCallback {

    private val userId = (100000..999999).random()
    private val peers : MutableList<PeerData> = mutableListOf()
    private val peerAdapter by lazy {
        PeerAdapter(peers, userId) {
            doCall(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

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
                }

            })
            .check()

        with(rvPeers){
            layoutManager = LinearLayoutManager(this@HomeActivity)
            adapter = peerAdapter
        }

        SignallingClient.instance.init(this)
    }

    private fun doCall(data: PeerData) {
        startActivity(Intent(this, CallActivity::class.java).apply {
            putExtra("ID", data.id)
            putExtra("IS_OFFER", false)
        })
    }

    override fun onPeers(peers: MutableList<PeerData>) {
        runOnUiThread {
            this.peers.clear()
            this.peers.addAll(peers)
            this.peerAdapter.notifyDataSetChanged()
        }
    }

    override fun onOffer(data: MessageData) {
        runOnUiThread {
            startActivity(Intent(this, CallActivity::class.java).apply {
                putExtra("ID", data.from.toString())
                putExtra("IS_OFFER", true)
                putExtra("SDP", data.description.sdp)
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        SignallingClient.instance.onDestroy()
    }
}
