package com.example.galleonpepsi.ui.posmactivity.responses.outletlistapiresponse

data class Retail(
    val AreaName: String,
    val DEFAULT_LAT_VALUE: String,
    val DEFAULT_LON_VALUE: String,
    val DISTRIBUTOR_CODE: String,
    val DISTRIBUTOR_NAME: String,
    val ID: String,
    val RETAIL_CODE: String,
    val RETAIL_NAME: String,
    val RegionName: String,
    val TerritoryId: String
)
{
    override fun toString(): String {
        return "$RETAIL_NAME($RETAIL_CODE)"
    }
}