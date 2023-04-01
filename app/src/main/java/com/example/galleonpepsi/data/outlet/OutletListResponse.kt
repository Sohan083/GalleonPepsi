package com.example.galleonpepsi.data.outlet

data class OutletListResponse(
    val message: String,
    val outletResult: List<OutletResult>,
    val success: Boolean
)