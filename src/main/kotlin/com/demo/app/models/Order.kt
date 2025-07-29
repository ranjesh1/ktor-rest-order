package com.demo.app.models

import kotlinx.serialization.Serializable

@Serializable
data class Order(
    val id: Long? = null,
    val description: String,
    val priceInPence: Long,
    val completedStatus: Boolean = false,
    val userId: Long
)