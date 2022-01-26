package com.leesangmin89.readcontacttest

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.findNavController
import androidx.navigation.ui.navigateUp

class MainActivity : AppCompatActivity() {

    // 권한 허용 리스트
    val permissions = arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.CALL_PHONE)

    // 권한 허용 코드
    private val CONTACT_AND_CALL_PERMISSION_CODE = 1

    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 권한 허용 체크
        checkAndStart()

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.myHostFragment) as NavHostFragment
        val navController = navHostFragment.navController
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
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
