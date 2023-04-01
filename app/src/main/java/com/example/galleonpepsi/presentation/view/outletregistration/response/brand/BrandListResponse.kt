package com.example.galleonpepsi.presentation.view.outletregistration.response.brand

data class BrandListResponse(
    val message: String,
    val resultList: List<Result>,
    val success: Boolean
)