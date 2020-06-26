package com.inyomanw.mysimplewebrtc.ui

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.inyomanw.mysimplewebrtc.R
import com.inyomanw.mysimplewebrtc.model.PeerData
import com.inyomanw.mysimplewebrtc.websocket.SignallingClient
import kotlinx.android.synthetic.main.peer_item.view.*

class PeerAdapter(private val peers: MutableList<PeerData>, private val userId: Int, private val callback: (data:PeerData) -> Unit) : RecyclerView.Adapter<PeerAdapter.PeerViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, p1: Int): PeerViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.peer_item, viewGroup, false);
        return PeerViewHolder(
            view,
            userId
        )
    }

    override fun getItemCount() = peers.size

    override fun onBindViewHolder(viewHolder: PeerViewHolder, position: Int) {
        viewHolder.bind(peers[position], callback)
    }

    class PeerViewHolder(private val view: View, private val userId: Int) : RecyclerView.ViewHolder(view) {

        fun bind(peer: PeerData, callback: (data:PeerData) -> Unit) {
            view.tvPeerName.text = "${peer.name} / [${peer.userAgent}]"
            view.tvPeerId.text = peer.id
            Log.d("logadapter","peer.id : ${peer.id}")
            Log.d("logadapter","userId : $userId")
            view.btCall.visibility = if(peer.id == SignallingClient.instance.userId.toString()) View.GONE else View.VISIBLE
            view.btCall.setOnClickListener {
                callback(peer)
            }
        }
    }

}