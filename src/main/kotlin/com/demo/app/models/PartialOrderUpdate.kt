package com.demo.app.models

import kotlinx.serialization.Serializable

@Serializable
data class PartialOrderUpdate(
    val description: String? = null,
    val priceInPence: Long? = null,
    val completedStatus: Boolean? = false,
//    val userId: Long? = null
)

