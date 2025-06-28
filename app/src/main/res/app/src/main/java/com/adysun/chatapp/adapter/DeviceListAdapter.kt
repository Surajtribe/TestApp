package com.adysun.chatapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.adysun.chatapp.R

class DeviceListAdapter(
    private var devices: List<String>,
    private val onPairClick: (String) -> Unit // Callback for button click
) : RecyclerView.Adapter<DeviceListAdapter.DeviceViewHolder>() {

    inner class DeviceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val deviceName: TextView = view.findViewById(R.id.deviceName)
        val pairButton: Button = view.findViewById(R.id.pairButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.device_item, parent, false)
        return DeviceViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val device = devices[position]
        holder.deviceName.text = device
        holder.pairButton.setOnClickListener {
            onPairClick(device) // Trigger the callback
        }
    }

    override fun getItemCount(): Int = devices.size

    fun updateDevices(newDevices: List<String>) {
        devices = newDevices
        notifyDataSetChanged()
    }
}
