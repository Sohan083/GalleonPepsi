package com.example.galleonpepsi.ui.posmactivity.responses.outletlistapiresponse

data class OutletListResponseBody(
    val message: String,
    val retailList: List<Retail>,
    val success: Boolean
)