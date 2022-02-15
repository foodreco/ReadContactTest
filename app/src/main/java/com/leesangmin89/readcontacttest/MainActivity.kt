package com.leesangmin89.readcontacttest

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.findNavController
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.leesangmin89.readcontacttest.callLog.CallLogViewModel
import com.leesangmin89.readcontacttest.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    // 권한 허용 리스트
    private val permissions = arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.CALL_PHONE, Manifest.permission.READ_CALL_LOG)

    // 권한 허용 코드
    private val CONTACT_AND_CALL_PERMISSION_CODE = 1

    private lateinit var appBarConfiguration: AppBarConfiguration

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        Log.i("수정","권한 초기 중복 허용 다듬을 것")
        Log.i("추가","통화별 메모기능으로 인한 보안 로그인 설정 추가 필요")
        Log.i("추가","각 Fragment back 버튼 시, 즉시 종료되야 함")

        // 권한 허용 체크
        checkAndStart()
        
        // Navigation 설정 코드
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.myHostFragment) as NavHostFragment
        val navController = navHostFragment.navController
        appBarConfiguration = AppBarConfiguration(binding.bottomNav.menu)

        // 바텀 네비게이션뷰 작동 코드
        findViewById<BottomNavigationView>(R.id.bottom_nav)
            .setupWithNavController(navController)

        // 액션바 작동 코드
        setupActionBarWithNavController(navController, appBarConfiguration)

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

    private fun checkAndStart() {
        // 권한 허용 여부 확인
        if (checkNeedPermission()) {
            // 허용 시
            Toast.makeText(this, "허용 되어있음", Toast.LENGTH_SHORT).show()
        } else {
            requestContactPermission()
        }
    }

    private fun checkNeedPermission(): Boolean {
        for (perm in permissions) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    perm
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    private fun requestContactPermission() {
        // READ_CONTACT 허용 요청 함수
        ActivityCompat.requestPermissions(
            this,
            permissions,
            CONTACT_AND_CALL_PERMISSION_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CONTACT_AND_CALL_PERMISSION_CODE) {
            var check = true

            for (grant in grantResults) {
                if (grant != PackageManager.PERMISSION_GRANTED) {
                    check = false
                    break
                }
            }
            if (check) Toast.makeText(this, "권한 지금 허용 됨", Toast.LENGTH_SHORT).show()
            else {
                Toast.makeText(this, "허용 거부되었습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
