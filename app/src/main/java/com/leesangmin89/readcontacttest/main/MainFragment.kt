package com.leesangmin89.readcontacttest.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.CallLog
import android.provider.ContactsContract
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.leesangmin89.readcontacttest.R
import com.leesangmin89.readcontacttest.callLog.CallLogViewModel
import com.leesangmin89.readcontacttest.data.entity.CallLogData
import com.leesangmin89.readcontacttest.data.entity.ContactInfo
import com.leesangmin89.readcontacttest.databinding.FragmentMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainFragment : Fragment() {

    private val binding by lazy { FragmentMainBinding.inflate(layoutInflater) }
    private val mainViewModel: MainViewModel by viewModels()
    private val callLogViewModel: CallLogViewModel by viewModels()

    // 권한 허용 리스트
    val permissions = arrayOf(Manifest.permission.READ_CALL_LOG)

    // 권한 허용 코드
    private val CONTACT_AND_CALL_PERMISSION_CODE = 1

    // 총 통화량 집계 변수(맵)
    private val contactMap = mutableMapOf<String, Int>()

    private var contactNumbers: Int = 0

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        checkAndStart()

        // 활성 통화 횟수 및 마지막 통화 표현 코드
        mainViewModel.infoData.observe(viewLifecycleOwner, Observer {
            binding.apply {
                if (it == null) {
//                    textContactNumber.text = getString(R.string.contact_number, 0)
                    textContactActivated.text = getString(R.string.contact_activated, 0)
                    textRecentContact.text = getString(R.string.recent_contact, "해당없음")
                    MostContactNumber.text = getString(R.string.most_contact_name, "해당없음")
                    MostContactDuration.text = getString(R.string.most_contact_duration, 0,0)

                } else {
//                    textContactNumber.text = getString(R.string.contact_number, it.contactNumber)
                    textContactActivated.text =
                        getString(R.string.contact_activated, it.activatedContact)
                    textRecentContact.text =
                        getString(R.string.recent_contact, it.mostRecentContact)
                    MostContactNumber.text =
                        getString(R.string.most_contact_name, it.mostContactName)

                    val minutes = it.mostContactTimes!!.toLong() / 60
                    val seconds = it.mostContactTimes.toLong() % 60
                    MostContactDuration.text =
                        getString(R.string.most_contact_duration, minutes, seconds)
                }
            }
        })

        // 연락처 개수 표현 코드
        mainViewModel.contactNumbers.observe(viewLifecycleOwner, {
            if (it == null) {
                binding.textContactNumber.text = getString(R.string.contact_phone_numbers, 0)
            } else {
                binding.textContactNumber.text = getString(R.string.contact_phone_numbers, it)
            }
        })

        binding.btnToList.setOnClickListener {
            val action = MainFragmentDirections.actionMainFragmentToListFragment()
            findNavController().navigate(action)
        }

        binding.btnGroup.setOnClickListener {
            val action = MainFragmentDirections.actionMainFragmentToGroupFragment()
            findNavController().navigate(action)
        }

        binding.btnFinish.setOnClickListener {
            activity?.finishAndRemoveTask()
        }

        binding.btnToCallLog.setOnClickListener {
            val action = MainFragmentDirections.actionMainFragmentToCallLogFragment()
            findNavController().navigate(action)
        }

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun checkAndStart() {
        // 권한 허용 여부 확인
        if (checkNeedPermission()) {
            // 허용 시
            getPhoneInfo()
            printKingContact()
            countContactNumbers()
            // 통화기록 가져오기
            getCallLogInfo()
            Log.i("보완","비슷한 콘텐츠 리졸버 함수 통합하기")
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
                countContactNumbers()
                Toast.makeText(context, "권한 지금 허용 됨", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "허용 거부되었습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 전화 통계를 계산하는 함수(ContactInfo)
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

        // 가장 최근 통화 대상(최근 대상부터 while 반복됨)
        val mostRecentContact = list[0].name
        // 가장 최장 통화시간
        val mapMaxValue = contactMap.maxOf { it.value }
        // 가장 최장 통화대상 번호
        val mapMaxKey = contactMap.filterValues { it == mapMaxValue }.keys.first()

        // contactInfo 에 data 입력
        mainViewModel.insertInfo(
            callCountNum,
            activatedContact,
            mostRecentContact,
            mapMaxKey,
            mapMaxValue
        )

        contacts.close()
    }

    // 전체 연락처 갯수를 알려주는 함수
    @SuppressLint("Range")
    fun countContactNumbers() {
        contactNumbers = 0

        val contacts = requireActivity().contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            null
        )

        while (contacts!!.moveToNext()) {
            contactNumbers++
        }
        mainViewModel._contactNumbers.value = contactNumbers
        contacts.close()
    }

    // 최대 통화 번호와 시간을 알려주는 코드
    @SuppressLint("SetTextI18n")
    private fun printKingContact() {

    }

    // 통화기록을 가져오는 함수(CallLogData)
    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("Range")
    fun getCallLogInfo() {

        Log.i("확인"," 리스트 뷰모델 : getCallLogInfo()")

        // 전화 로그 가져오는 uri
        val callLogUri = CallLog.Calls.CONTENT_URI

        // 통화 총 횟수 카운드 변수
        var callCountNum = 0
        var activatedContact = 0

        val proj = arrayOf(
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.NUMBER,
            CallLog.Calls.DATE,
            CallLog.Calls.DURATION,
            CallLog.Calls.TYPE
        )

        val contacts = requireActivity().contentResolver.query(
            callLogUri,
            null,
            null,
            null,
            null
        )

        // 데이터 중첩을 막기 위해, 기존 데이터 삭제
        callLogViewModel.clear()

        // 반복 작업 구간
        while (contacts!!.moveToNext()) {
            var name =
                contacts.getString(contacts.getColumnIndex(CallLog.Calls.CACHED_NAME))
            if (name == null) {
                name = "발신자불명"
            }
            val number =
                contacts.getString(contacts.getColumnIndex(CallLog.Calls.NUMBER)).replace("-","")
            val date =
                contacts.getString(contacts.getColumnIndex(CallLog.Calls.DATE))
            val duration =
                contacts.getString(contacts.getColumnIndex(CallLog.Calls.DURATION))
            val callType =
                contacts.getString(contacts.getColumnIndex(CallLog.Calls.TYPE))


            val listChild = CallLogData(name, number, date, duration, callType)

            callLogViewModel.insert(listChild)
        }
        contacts.close()
    }

}

data class ContactSpl(
    val id: String,
    val name: String,
    val number: String,
    val duration: String
)