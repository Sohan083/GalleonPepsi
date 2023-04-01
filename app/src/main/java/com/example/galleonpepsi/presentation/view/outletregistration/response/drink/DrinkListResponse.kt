package com.example.galleonpepsi.presentation.view.outletregistration.response.drink

data class DrinkListResponse(
    val message: String,
    val resultList: List<Result>,
    val success: Boolean
)