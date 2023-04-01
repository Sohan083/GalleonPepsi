package com.example.galleonpepsi.data.outlet.distribution

data class DistributionResponse(
    val message: String,
    val outletResult: List<OutletResult>,
    val success: Boolean
)