package com.example.galleonpepsi.data.api

import com.example.galleonpepsi.data.outlet.OutletForSubmitData
import com.example.galleonpepsi.data.outlet.OutletListResponse
import com.example.galleonpepsi.data.outlet.OutletSubmitResponse
import com.example.galleonpepsi.data.outlet.distribution.DistributionResponse
import com.example.galleonpepsi.presentation.view.outletregistration.response.brand.BrandListResponse
import com.example.galleonpepsi.presentation.view.outletregistration.response.drink.DrinkListResponse
import retrofit2.http.*

interface OutletsApi {
/*
    @GET("outlets")
    suspend fun getOutlets(@Query("search") search: String): OutletListResponse
*/

    @FormUrlEncoded
    @POST("outlet/insert_outlet.php")
   suspend fun createOutlet(
        @Field("UserId") UserId: String,
        @Field("OutletName") OutletName: String,
        @Field("Address") Address: String,
        @Field("DistributorId") DistributorId: String,
        @Field("LatValue") LatValue: String,
        @Field("LonValue") LonValue: String,
        @Field("Accuracy") Accuracy: String,
        @Field("Remarks") Remarks: String,
        @Field("AppVersion") AppVersion: String,
    ): OutletSubmitResponse


    @FormUrlEncoded
    @POST("outlet/get_outlet.php")
    fun getOutletList(
        @Field("UserId") UserId: String,
        @Field("TargetUserId") TargetUserId : String,
        ): retrofit2.Call<OutletListResponse>

    @FormUrlEncoded
    @POST("brand/get_brand.php")
    fun getBrandList(
        @Field("UserId") UserId: String,

    ): retrofit2.Call<BrandListResponse>

    @FormUrlEncoded
    @POST("drink/get_drink.php")
    fun getDrinkList(
        @Field("UserId") UserId: String,

        ): retrofit2.Call<DrinkListResponse>

    @PUT("outlets/{id}")
    suspend fun updateOutlet(@Path("id") id: Int, @Body outletForSubmitData: OutletForSubmitData): OutletForSubmitData

    @DELETE("outlets/{id}")
    suspend fun deleteOutlet(@Path("id") id: Int)

    @FormUrlEncoded
    @POST("distributor/get_distributor.php")
    fun getDistributionList(
        @Field("UserId") UserId: String,
        @Field("ResponseType") ResponseType: String
    ): retrofit2.Call<DistributionResponse>

    @FormUrlEncoded
    @POST("outlet/get_outlet.php")
    fun getOutlet(
        @Field("UserId") UserId: String,
        @Field("OutletCode") OutletCode: String
    ): retrofit2.Call<OutletListResponse>
}
