package com.example.phonecalldemo.utils

import com.example.phonecalldemo.models.MessageModel

interface NewMessageInterface {
    fun onNewMessage(message: MessageModel)
}