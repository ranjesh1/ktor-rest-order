package com.demo.app.models

import kotlinx.serialization.Serializable

@Serializable
data class PartialUserUpdate(
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val firstLineOfAddress: String? = null,
    val secondLineOfAddress: String? = null,
    val town: String? = null,
    val postCode: String? = null
)