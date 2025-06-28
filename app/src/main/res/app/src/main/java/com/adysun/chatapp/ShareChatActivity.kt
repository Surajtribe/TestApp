package com.adysun.chatapp

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.adysun.chatapp.adapter.DeviceListAdapter
import com.adysun.chatapp.adapter.ShareChatAdapter
import com.adysun.chatapp.model.ChatMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket


class ShareChatActivity : AppCompatActivity() {

    private lateinit var deviceListAdapter: DeviceListAdapter
    private lateinit var shateChatAdapter: ShareChatAdapter
    private var pairedDeviceIp: String? = null // Store the paired IP
    var fileType = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share_chat)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        }

        val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager

        val devices = scanDevices(wifiManager)
        CoroutineScope(Dispatchers.IO).launch {
            runOnUiThread {
                deviceListAdapter.updateDevices(devices)
            }
        }

        // Initialize RecyclerView for device scanning
        val deviceRecyclerView = findViewById<RecyclerView>(R.id.deviceRecyclerView)
        deviceRecyclerView.layoutManager = LinearLayoutManager(this)
        deviceListAdapter = DeviceListAdapter(devices) { ip ->
            pairedDeviceIp = ip // Save the paired device IP

            pairWithDevice(ip) { success ->
                runOnUiThread {
                    if (success) {
                        Toast.makeText(this, "Successfully paired with $ip", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Failed to pair with $ip", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        deviceRecyclerView.adapter = deviceListAdapter

        val chatRecyclerView = findViewById<RecyclerView>(R.id.chatRecyclerView)
        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        shateChatAdapter = ShareChatAdapter(ArrayList())
        chatRecyclerView.adapter = shateChatAdapter

        startTextServerSocket()
        startFileServerSocket()

        findViewById<ImageView>(R.id.sendButton).setOnClickListener {
            fileType = true
            val message = findViewById<EditText>(R.id.messageInput).text.toString()
            if (message.isNotEmpty()) {
                sendMessage(message)
                findViewById<EditText>(R.id.messageInput).text.clear()
            } else {
                Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<ImageView>(R.id.selectFileButton).setOnClickListener {
            fileType = false
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*"
            startActivityForResult(intent, 1001) // Request code for file selection
        }
    }

    private fun startTextServerSocket() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val serverSocket = ServerSocket(8080)
                while (true) {
                    val clientSocket = serverSocket.accept()
                    clientSocket.setKeepAlive(true)
                    handleIncomingMessage(clientSocket)
                }
            } catch (e: Exception) {
                Log.e("ServerSocket", "Error: ${e.message}")
            }
        }
    }

    private fun startFileServerSocket() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val serverSocket = ServerSocket(8081) // Use port 8081 for file transfers
                while (true) {
                    val clientSocket = serverSocket.accept()
                    clientSocket.setKeepAlive(true)
                    handleIncomingFile(clientSocket)
                }
            } catch (e: Exception) {
                Log.e("ServerSocket", "Error: ${e.message}")
            }
        }
    }

    private fun scanDevices(wifiManager: WifiManager): List<String> {
        val devices = mutableListOf<String>()

        // Show the progress bar
        runOnUiThread {
            findViewById<ProgressBar>(R.id.scanProgressBar).visibility = View.VISIBLE
        }

        // Get the IP address of the current device
        val ipAddress = wifiManager.connectionInfo.ipAddress
        val formattedIpAddress = String.format(
            "%d.%d.%d.%d",
            (ipAddress and 0xff),
            (ipAddress shr 8 and 0xff),
            (ipAddress shr 16 and 0xff),
            (ipAddress shr 24 and 0xff)
        )

        // Extract the subnet (e.g., 192.168.1.)
        val subnet = formattedIpAddress.substringBeforeLast(".") + "."

        CoroutineScope(Dispatchers.IO).launch {
            // Ping each address in the subnet
            for (i in 1..254) {
                val testIp = "$subnet$i"
                try {
                    val reachable = InetAddress.getByName(testIp).isReachable(100)
                    if (reachable) {
                        devices.add(testIp)
                        Log.d("DeviceScanner", "Device found: $testIp")
                    }
                } catch (e: Exception) {
                    Log.e("DeviceScanner", "Error checking $testIp: ${e.message}")
                }
            }

            // Update UI with found devices and hide progress bar
            runOnUiThread {
                deviceListAdapter.updateDevices(devices)
                findViewById<ProgressBar>(R.id.scanProgressBar).visibility = View.GONE
            }
        }

        return devices
    }

    private fun pairWithDevice(ip: String, callback: (Boolean) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val socket = Socket(ip, 8080)
                socket.close()
                callback(true)
            } catch (e: Exception) {
                callback(false)
                Log.e("Pairing", "Failed to pair with $ip: ${e.message}")
            }
        }
    }

    private fun handleIncomingMessage(clientSocket: Socket) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val input = clientSocket.getInputStream().bufferedReader()
                val message = input.readLine()

                runOnUiThread {
                    val message = ChatMessage(text = "Received: $message", isImage = false, isSent = true)
                    shateChatAdapter.addMessage(message)

                    val chatRecyclerView = findViewById<RecyclerView>(R.id.chatRecyclerView)
                    chatRecyclerView.scrollToPosition(shateChatAdapter.itemCount - 1)
                }

                clientSocket.close()
            } catch (e: Exception) {
                Log.e("ServerSocket", "Error handling message: ${e.message}")
            }
        }
    }

    private fun sendMessage(message: String) {
        pairedDeviceIp?.let { ip ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val socket = Socket(ip, 8080) // Ensure port matches the server
                    val output = socket.getOutputStream().bufferedWriter()
                    output.write(message)
                    output.newLine()
                    output.flush()

                    runOnUiThread {
                        val message = ChatMessage(text = "Sent: $message", isImage = false, isSent = false)
                        shateChatAdapter.addMessage(message)
                        shateChatAdapter.notifyDataSetChanged()
                        val chatRecyclerView = findViewById<RecyclerView>(R.id.chatRecyclerView)
                        chatRecyclerView.scrollToPosition(shateChatAdapter.itemCount - 1)
                    }
                    socket.close()
                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(this@ShareChatActivity, "Failed to send: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } ?: run {
            Toast.makeText(this, "No paired device selected", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendFile(fileUri: Uri) {
        pairedDeviceIp?.let { ip ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val file = File(getFilePathFromUri(this@ShareChatActivity, uri = fileUri))
                    val socket = Socket(ip, 8081)
                    socket.setKeepAlive(true)
                    val outputStream = socket.getOutputStream()

                    val fileInputStream = FileInputStream(file)
                    val buffer = ByteArray(1024)
                    var bytesRead: Int
                    while (fileInputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                        Log.d("FileTransfer", "Bytes sent: $bytesRead")
                    }
                    outputStream.flush()
                    fileInputStream.close()
                    socket.close()

                    runOnUiThread {
                        Toast.makeText(this@ShareChatActivity, "File sent successfully", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(this@ShareChatActivity, "Failed to send file: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } ?: run {
            Toast.makeText(this, "No paired device selected", Toast.LENGTH_SHORT).show()
        }
    }

    fun getFilePathFromUri(context: Context, uri: Uri): String? {
        var filePath: String? = null
        val parcelFileDescriptor = context.contentResolver.openFileDescriptor(uri, "r")
        parcelFileDescriptor?.use {
            val file = File(context.filesDir, "received_image.jpg")
            val inputStream = FileInputStream(it.fileDescriptor)
            val outputStream = FileOutputStream(file)

            inputStream.copyTo(outputStream)
            outputStream.flush()
            filePath = file.absolutePath
        }
        return filePath
    }


    private fun handleIncomingFile(clientSocket: Socket) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val inputStream = clientSocket.getInputStream()
                val receivedFile = File(filesDir, "received_image.jpg")

                val fileOutputStream = FileOutputStream(receivedFile)
                val buffer = ByteArray(1024)
                var bytesRead: Int

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    fileOutputStream.write(buffer, 0, bytesRead)
                }

                fileOutputStream.flush()
                fileOutputStream.close()
                clientSocket.close()

                val bitmap = BitmapFactory.decodeFile(receivedFile.absolutePath)
                if (bitmap != null) {
                    val message = ChatMessage(image = bitmap, isImage = true, isSent = true)
                    runOnUiThread {
                        shateChatAdapter.addMessage(message)
                        val chatRecyclerView = findViewById<RecyclerView>(R.id.chatRecyclerView)
                        chatRecyclerView.scrollToPosition(shateChatAdapter.itemCount - 1)
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@ShareChatActivity, "Failed to decode image", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("FileTransfer", "Error receiving file: ${e.message}")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == Activity.RESULT_OK && data != null) {
            val fileUri = data.data
            if (fileUri != null) {
                sendFile(fileUri)

                val bitmap = BitmapFactory.decodeFile(getFilePathFromUri(this, fileUri))
                if (bitmap != null) {
                    val message = ChatMessage(image = bitmap, isImage = true, isSent = false)
                    shateChatAdapter.addMessage(message)
                    shateChatAdapter.notifyDataSetChanged()
                    val chatRecyclerView = findViewById<RecyclerView>(R.id.chatRecyclerView)
                    chatRecyclerView.scrollToPosition(shateChatAdapter.itemCount - 1)
                }
            }
        }
    }
}

