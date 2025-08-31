package com.ariyan.spark.model

data class MatchItem(
    val user: User,
    val lastMessage: String = "",
    val lastMessageTime: Long? = null,
    val lastSeen: Long? = null
)
