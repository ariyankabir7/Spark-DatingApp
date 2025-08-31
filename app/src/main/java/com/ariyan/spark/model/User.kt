package com.ariyan.spark.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class User(
  val uid: String = "",
  val name: String = "",
  val email: String = "",
  val photoUrl: String = "",
  val gender: String = "",
  val age: Int = 0,
  val interests: List<String> = emptyList(),
  val likes: List<String> = emptyList(),
  val dislikes: List<String> = emptyList(),
  @ServerTimestamp
  val lastSeen: Timestamp? = null
)