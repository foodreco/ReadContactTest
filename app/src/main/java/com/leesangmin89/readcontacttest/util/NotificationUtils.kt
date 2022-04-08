package com.leesangmin89.readcontacttest.util

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import com.leesangmin89.readcontacttest.MainActivity
import com.leesangmin89.readcontacttest.R

// Notification ID.
private val NOTIFICATION_ID = 0

// 알림을 보내기 위해 사용할 확장기능임
fun NotificationManager.sendNotification(messageBody: String, applicationContext: Context) {

    // 메인엑티비티로 안내하는 인텐트 코드
    val contentIntent = Intent(applicationContext, MainActivity::class.java)

    // 앱 외부에서 작동하게 하기 위한 팬딩인텐트
    val contentPendingIntent = PendingIntent.getActivity(
        applicationContext,
        NOTIFICATION_ID,
        contentIntent,
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    // 전화모양 스타일 넣기 BitmapFactory 사용
    val phoneCallImage = BitmapFactory.decodeResource(
        applicationContext.resources,
        R.drawable.phone_call_png
    )

    // Build the notification
    // 알림 빌더의 인스턴스를 가져오고 앱 컨텍스트와 채널 ID를 전달합니다. 채널 ID는 채널의 문자열 값입니다.
    val builder = NotificationCompat.Builder(
        applicationContext,
        applicationContext.getString(R.string.notification_channel_id)
    )

        // 앱을 나타내는 알림 아이콘, 제목 및 사용자에게 주고 싶은 메시지의 콘텐츠 텍스트를 설정합니다.
        // Codelab 에서 알림을 추가로 맞춤설정할 수 있는 더 많은 옵션이 표시되지만 이는 알림을 보내기 위해 설정해야 하는 최소 데이터 양입니다.
        .setSmallIcon(R.drawable.ic_round_call_50)
        .setContentTitle(applicationContext.getString(R.string.notification_title))
        .setContentText(messageBody)

//        // 콘텐트 설정(외부에서 터치 시 앱으로 이동 가능)
//        .setContentIntent(contentPendingIntent)
        // 알림 터치 시, 알림 자동으로 사라짐
        .setAutoCancel(true)

        // 아이콘 설정
        .setLargeIcon(phoneCallImage)

    // notify()알림에 대한 고유 ID와 Notification 빌더의 개체를 사용하여 호출해야 합니다.
    notify(NOTIFICATION_ID, builder.build())
    // 이 ID는 현재 알림 인스턴스를 나타내며 이 알림을 업데이트하거나 취소하는 데 필요합니다.
    // 앱에는 주어진 시간에 하나의 활성 알림만 있으므로 모든 알림에 동일한 ID를 사용할 수 있습니다.
    // NOTIFICATION_ID 에서 호출 된 이 목적을 위한 상수가 이미 주어졌습니다
    // NotificationUtils.kt. notify()동일한 클래스의 확장 함수에서 호출을 수행하고 있으므로 직접 호출할 수 있습니다 .

}


// 모든 활성 알림을 취소하는 함수
fun NotificationManager.cancelNotifications() {
    cancelAll()
}