package com.example.galleonpepsi.data.outlet

data class OutletForSubmitData(
    val UserId: String,
    val OutletId: String,
    val OutletName: String,
    val Address: String,
    //val DistributorId: String,
    val LatValue: String,
    val LonValue: String,
    val Accuracy: String,
    val Remarks: String,
    val AppVersion: String,
    val OutletType:String,
    val ContactName: String,
    val ContactPersonMobile: String,
    val HasShopFascia: String,
    val Posm1HasLightBox: String,
    val Posm1BrandId:String,
    val Posm1DrinkId: String,

    val Posm2HasLightBox: String,
    val Posm2BrandId:String,
    val Posm2DrinkId: String,
)
