package com.example.galleonpepsi.ui.attendance.attendanceresponse

data class Result(
    val AttendanceDate: String,
    val AttendanceId: String,
    val AttendanceStatusId: String,
    val AttendanceStatusName: String,
    val EMPLOYEE_NAME: String,
    val EmployeeId: String,
    val EmployeeTypeId: String,
    val InRemark: String,
    val InTime: String,
    val InTimeAccuracy: String,
    val InTimeLat: String,
    val InTimeLon: String,
    val InTimePictureName: String,
    val LeaveTypeId: String,
    val MeetingTypeId: String,
    val OutRemark: Any,
    val OutTime: String?,
    val OutTimeAccuracy: Any,
    val OutTimeLat: Any,
    val OutTimeLon: Any,
    val OutTimePictureName: Any
)