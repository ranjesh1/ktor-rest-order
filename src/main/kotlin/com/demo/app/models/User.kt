package com.demo.app.models

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Long? = null,
    val firstName: String,
    val lastName: String,
    val email: String,
    val firstLineOfAddress: String,
    val secondLineOfAddress: String? = null,
    val town: String,
    val postCode: String
)