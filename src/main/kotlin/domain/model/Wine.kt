package com.august.domain.model

data class Wine(
    val id: String,
    val wineryName: String,
    val countryCode: String,
    val vintage: Int,
    val price: Int,
    val quantity: Int,
)