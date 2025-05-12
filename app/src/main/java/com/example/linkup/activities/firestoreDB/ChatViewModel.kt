package com.example.linkup.activities.firestoreDB

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ChatViewModel : ViewModel() {
    private val _chats = MutableLiveData<List<Chat>>(emptyList())
    val chats: LiveData<List<Chat>> get() = _chats

    private val chatMap = mutableMapOf<String, Chat>()  //For efficient updates

    fun addOrUpdateChat(chat: Chat) {
        chatMap[chat.id] = chat
        _chats.value = chatMap.values.sortedByDescending { it.timestamp }
    }

    fun clearChats() {
        chatMap.clear()
        _chats.value = emptyList()
    }

    fun removeChat(chatId: String) {
        chatMap.remove(chatId)
        _chats.value = chatMap.values.sortedByDescending { it.timestamp }
    }
}