package com.example.galleonpepsi.ui.dashboard.response

data class DashboardResponseBody(
    val dashboardData: DashboardData,
    val message: String,
    val success: Boolean
)