package com.example.phonecalldemo

import android.util.Log
import com.example.phonecalldemo.models.MessageModel
import com.example.phonecalldemo.utils.NewMessageInterface
import com.google.gson.Gson
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

class SocketRepository (private val messageInterface: NewMessageInterface) {
    private var webSocket: WebSocketClient? = null
    private var userName: String? = null
    private val TAG = "SocketRepository"
    private val gson = Gson()

    fun initSocket(username: String) {
        userName = username
        webSocket = object : WebSocketClient(URI("ws://10.0.2.2:5000")) {
            //        webSocket = object : WebSocketClient(URI("ws://192.168.1.3:3000")) {
            override fun onOpen(handshakedata: ServerHandshake?) {
                sendMessageToSocket(
                    MessageModel(
                        "store_user",username,null,null
                    )
                )
            }
            override fun onMessage(message: String?) {
                try {
                    messageInterface.onNewMessage(gson.fromJson(message,MessageModel::class.java))

                }catch (e:Exception){
                    e.printStackTrace()
                }
            }
            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                Log.d(TAG, "onClose: $reason")
            }
            override fun onError(ex: Exception?) {
                Log.d(TAG, "onError: $ex")
            }
        }
        webSocket?.connect()
    }

    fun sendMessageToSocket(message: MessageModel) {
        try {
            Log.d(TAG, "sendMessageToSocket: $message")
            webSocket?.send(Gson().toJson(message))
        } catch (e: Exception) {
            Log.d(TAG, "sendMessageToSocket: $e")
        }
    }
}