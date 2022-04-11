package com.example.galleonpepsi.utils

class StaticTags {
    val dateFormatShow = "dd-MM-yyyy"
    companion object{
        const val EXECUTION_IMAGE_CAPTURE_CODE = 5
        const val RETAIL_IMAGE_CAPTURE_CODE = 6

        val GPS_REQUEST: Int = 101
        val LOCATION_REQUEST: Int = 100
        const val IMAGE_CAPTURE_CODE: Int = 1
        var BASE_URL = "https://isgalleon.com/"
        //var BASE_URL = "https://pepsi.imslpro.com/"
        var isBeta = false
        const val timeFormat = "HH:mm"
        val dateFormat = "yyyy-MM-dd"
        val dateFormatShow = "dd-MM-yyyy"
    }
}