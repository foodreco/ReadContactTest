package com.leesangmin89.readcontacttest

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.findNavController
import androidx.navigation.ui.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.leesangmin89.readcontacttest.databinding.ActivityMainBinding
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
        Log.i("수정", "ViewModelScope 내부 반복문 적용 시 ViewModelScope 로 2중 감싸주기")

        // 권한 허용 체크
//        checkAndStart()

        // 기존 코드
//        // Navigation 설정 코드
//        val navHostFragment =
//            supportFragmentManager.findFragmentById(R.id.myHostFragment) as NavHostFragment
//        val navController = navHostFragment.navController
//        appBarConfiguration = AppBarConfiguration(binding.bottomNav.menu)
//
//        // 바텀 네비게이션뷰 작동 코드
//        findViewById<BottomNavigationView>(R.id.bottom_nav)
//            .setupWithNavController(navController)
//
//        // 액션바 작동 코드
//        setupActionBarWithNavController(navController, appBarConfiguration)


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

//        supportActionBar!!.setBackgroundDrawable(ColorDrawable(R.color.hau_white_green))

//        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
//        toolbar.setupWithNavController(navController, appBarConfiguration)

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

    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.myHostFragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

}
