package com.leesangmin89.readcontacttest.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.CallLog
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.leesangmin89.readcontacttest.R
import com.leesangmin89.readcontacttest.data.ContactDatabase
import com.leesangmin89.readcontacttest.data.ContactInfo
import com.leesangmin89.readcontacttest.databinding.FragmentListBinding
import com.leesangmin89.readcontacttest.databinding.FragmentMainBinding
import com.leesangmin89.readcontacttest.list.ListViewModel
import com.leesangmin89.readcontacttest.list.ListViewModelFactory

class MainFragment : Fragment() {

    private val binding by lazy { FragmentMainBinding.inflate(layoutInflater) }
    private lateinit var mainViewModel: MainViewModel

    // 권한 허용 리스트
    val permissions = arrayOf(Manifest.permission.READ_CALL_LOG)

    // 권한 허용 코드
    private val CONTACT_AND_CALL_PERMISSION_CODE = 1

    // 총 통화량 집계 변수(맵)
    private val contactMap = mutableMapOf<String, Int>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // 뷰모델팩토리 세팅
        val application = requireNotNull(this.activity).application
        val dataSource = ContactDatabase.getInstance(application).contactInfoDao
        val viewModelFactory = MainViewModelFactory(dataSource, application)
        // 뷰모델 초기화
        mainViewModel = ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)

        checkAndStart()

        mainViewModel.infoData.observe(viewLifecycleOwner, Observer {
            binding.apply {
                if (it == null) {
                    textContactNumber.text = getString(R.string.contact_number, 0)
                    textContactActivated.text = getString(R.string.contact_activated, 0)
                    textRecentContact.text = getString(R.string.recent_contact, "해당없음")
                } else {
                    textContactNumber.text = getString(R.string.contact_number, it.contactNumber)
                    textContactActivated.text = getString(R.string.contact_activated,it.activatedContact)
                    textRecentContact.text = getString(R.string.recent_contact,it.mostRecentContact)
                }
            }
        })

        binding.btnToList.setOnClickListener {
            val action = MainFragmentDirections.actionMainFragmentToListFragment()
            findNavController().navigate(action)
        }

        binding.btnFinish.setOnClickListener {
            activity?.finishAndRemoveTask()
        }

        return binding.root
    }

    private fun checkAndStart() {
        // 권한 허용 여부 확인
        if (checkNeedPermission()) {
            // 허용 시
            getPhoneInfo()
            printKingContact()
        } else {
            requestContactPermission()
        }
    }

    private fun checkNeedPermission(): Boolean {
        for (perm in permissions) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
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
            requireActivity(),
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
            if (check) {
                getPhoneInfo()
                printKingContact()
                Toast.makeText(context, "권한 지금 허용 됨", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "허용 거부되었습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("Range")
    fun getPhoneInfo() {
        val list = mutableListOf<ContactSpl>()
        // 전화 로그 가져오는 uri
        val callLogUri = CallLog.Calls.CONTENT_URI

        // 통화 총 횟수 카운드 변수
        var callCountNum = 0
        var activatedContact = 0

        val proj = arrayOf(
            CallLog.Calls.PHONE_ACCOUNT_ID,
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.NUMBER,
            CallLog.Calls.DURATION
        )

        val contacts = requireActivity().contentResolver.query(
            callLogUri,
            null,
            null,
            null,
            null
        )

        // 데이터 중첩을 막기 위해, 기존 데이터 삭제
        mainViewModel.clear()
        contactMap.clear()

        // 반복 작업 구간
        while (contacts!!.moveToNext()) {
            val id =
                contacts.getString(contacts.getColumnIndex(CallLog.Calls.PHONE_ACCOUNT_ID))
            var name =
                contacts.getString(contacts.getColumnIndex(CallLog.Calls.CACHED_NAME))
            if (name == null) {
                name = "발신자 불명"
            }
            val number =
                contacts.getString(contacts.getColumnIndex(CallLog.Calls.NUMBER))
            val duration =
                contacts.getString(contacts.getColumnIndex(CallLog.Calls.DURATION))

            if (duration.toInt() > 59) {
                activatedContact++
            }
            callCountNum++
            val listChild = ContactSpl(id, name, number, duration)
            list.add(listChild)

            // 번호와 누적 통화량을 기록하는 코드
            if (number in contactMap) {
                val preValue = contactMap[number]
                if (preValue != null) {
                    contactMap[number] = preValue + duration.toInt()
                }
            } else {
                contactMap[number] = duration.toInt()
            }
        }

        val mostRecentContact = list[0].name
        val contactInfo = ContactInfo(callCountNum, activatedContact, mostRecentContact, 0)

        mainViewModel.insertInfo(contactInfo)

        contacts.close()
    }

    @SuppressLint("SetTextI18n")
    private fun printKingContact() {
        val mapMaxValue = contactMap.maxOf { it.value }
        val mapMaxKey = contactMap.filterValues { it == mapMaxValue }.keys.first()
        binding.MostContactNumber.text = getString(R.string.most_contact_number, mapMaxKey)
        binding.MostContactDuration.text = getString(R.string.most_contact_duration, mapMaxValue)
    }

}

data class ContactSpl(
    val id: String,
    val name: String,
    val number: String,
    val duration: String
)