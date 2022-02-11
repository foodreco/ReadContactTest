package com.leesangmin89.readcontacttest

import android.annotation.SuppressLint
import java.text.SimpleDateFormat


@SuppressLint("SimpleDateFormat")
fun convertLongToDateString(systemTime: Long): String {
    val simpleDateFormat = SimpleDateFormat("yyyy년MM월dd일")
    return simpleDateFormat.format(systemTime)
}

fun convertLongToTimeString(systemTime: Long): String {
    val minutes = systemTime / 60
    val seconds = systemTime % 60
    // 통화시간 1분 미만은 초 단위로만 출력
    return when (systemTime) {
        in 0..59 -> "${seconds}초"
        else -> "${minutes}분 ${seconds}초"
    }
}

fun convertCallTypeToString(callType: Int): String {
    return when (callType) {
        1 -> "수신"
        2 -> "발신"
        3 -> "부재중"
        else -> ""
    }
}
