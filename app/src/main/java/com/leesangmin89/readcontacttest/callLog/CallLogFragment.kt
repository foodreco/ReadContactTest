package com.leesangmin89.readcontacttest.callLog

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.provider.CallLog
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.fragment.app.viewModels
import com.leesangmin89.readcontacttest.R
import com.leesangmin89.readcontacttest.data.entity.CallLogData
import com.leesangmin89.readcontacttest.databinding.FragmentCallLogBinding
import com.leesangmin89.readcontacttest.main.ContactSpl
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat

@AndroidEntryPoint
class CallLogFragment : Fragment() {

    private val binding by lazy { FragmentCallLogBinding.inflate(layoutInflater) }
    private val callLogViewModel: CallLogViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val adapter = CallLogAdapter()
        binding.callLogRecyclerView.adapter = adapter

        getCallLogInfo()

        callLogViewModel.callLogList.observe(viewLifecycleOwner, {
            adapter.submitList(it)
        })

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("Range", "SimpleDateFormat")
    fun getCallLogInfo() {

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
                contacts.getString(contacts.getColumnIndex(CallLog.Calls.NUMBER))
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