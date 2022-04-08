package com.leesangmin89.readcontacttest

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.findNavController
import androidx.navigation.ui.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.leesangmin89.readcontacttest.databinding.ActivityMainBinding
import com.leesangmin89.readcontacttest.main.MainFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        Log.i("추가", "통화별 메모기능으로 인한 보안 로그인 설정 추가 필요")
        Log.i("수정", "권한 체크 세밀하게 가다듬을 것. 정식 가이드 대로...")

        // 신규 코드
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.myHostFragment) as NavHostFragment

        val navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration.Builder(
            R.id.mainFragment,
            R.id.listFragment,
            R.id.callLogFragment,
            R.id.groupFragment,
        ).build()

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)

        // 액션바 그림자 제거
        supportActionBar?.elevation = 0f

        val navView: BottomNavigationView =
            findViewById(R.id.bottom_nav)

        NavigationUI.setupWithNavController(navView, navController)

        // 시작점 Fragment 외에는 bottomNav 표시 안함
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.mainFragment || destination.id == R.id.listFragment || destination.id == R.id.callLogFragment || destination.id == R.id.groupFragment) {
                binding.bottomNav.visibility = View.VISIBLE
            } else {
                binding.bottomNav.visibility = View.GONE
            }
        }

        // 채널 생성 함수 작동
        createChannel(
            getString(R.string.notification_channel_id),
            getString(R.string.notification_channel_name)
        )
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.myHostFragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    // 알림 채널을 생성하는 함수
    // 개발자는 채널의 모든 알림에 적용할 초기 설정, 중요도 및 동작을 설정합니다. 초기 설정을 지정한 후 사용자는 이러한 설정을 재정의할 수 있습니다.
    // 채널 Id 와, 채널 이름을 매겨변수로 받는다.
    private fun createChannel(channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                // 알림 채널 중요도 설정
                NotificationManager.IMPORTANCE_HIGH
            )
                // 뱃지 설정 false
                .apply {
                    setShowBadge(false)
                }

            // 알람 표시될때 조명 활성화 여부
            notificationChannel.enableLights(true)
            // 표시등 색상?? 설정
            notificationChannel.lightColor = Color.GREEN
            // 진동 활성화 여부
            notificationChannel.enableVibration(true)
//            // 채널 설명 문구
//            notificationChannel.description = ""

            val notificationManager = this.getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

}
