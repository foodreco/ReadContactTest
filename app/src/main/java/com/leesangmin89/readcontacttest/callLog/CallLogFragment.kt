package com.leesangmin89.readcontacttest.callLog

import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import com.hieupt.android.standalonescrollbar.attachTo
import com.leesangmin89.readcontacttest.R
import com.leesangmin89.readcontacttest.data.entity.CallLogData
import com.leesangmin89.readcontacttest.databinding.FragmentCallLogBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CallLogFragment : Fragment() {

    private val binding by lazy { FragmentCallLogBinding.inflate(layoutInflater) }
    private val callLogViewModel: CallLogViewModel by viewModels()
    private val adapter by lazy { CallLogAdapter(requireContext() ,childFragmentManager) }

    private val sortNumber = MutableLiveData<Int>(0)

    private val SORT_NORMAL_STATE = 0
    private val SORT_BY_CONTACT = 1
    private val SORT_BY_IMPORTANCE = 2

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding.callLogRecyclerView.adapter = adapter

        showProgress(true)

        // 헤더뷰홀더 리싸이클러뷰 코드
        callLogViewModel.callLogItemData.observe(viewLifecycleOwner){
            adapter.submitList(it)
            showProgress(false)
        }

        sortNumber.observe(viewLifecycleOwner) { number ->
            when (number) {
                SORT_NORMAL_STATE -> {
                    callLogViewModel.sortByNormal().observe(viewLifecycleOwner) {
                        if (sortNumber.value == SORT_NORMAL_STATE) {
                            if (it == emptyList<List<CallLogData>>()) {
                                Toast.makeText(
                                    requireContext(),
                                    "최근 통화기록이 없습니다.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            callLogViewModel.makeList(it)
                        }
                    }
                }
                SORT_BY_CONTACT -> {
                    callLogViewModel.sortByContact().observe(viewLifecycleOwner) { list ->
                        list.let {
                            if (sortNumber.value == SORT_BY_CONTACT) {
                                if (it == emptyList<List<CallLogData>>()) {
                                    Toast.makeText(
                                        requireContext(),
                                        "연락처 지정된 통화기록이 없습니다.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                callLogViewModel.makeList(it)
                            }
                        }
                    }
                }
                SORT_BY_IMPORTANCE -> {
                    callLogViewModel.sortByImportance().observe(viewLifecycleOwner) { list ->
                        list.let {
                            if (sortNumber.value == SORT_BY_IMPORTANCE) {
                                if (it == emptyList<List<CallLogData>>()) {
                                    Toast.makeText(
                                        requireContext(),
                                        "중요 기록이 없습니다.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                callLogViewModel.makeList(it)
                            }
                        }
                    }
                }
            }
        }

        // 어뎁터 별(importance) 터치 작동
        adapter.callLogImportanceSetting.observe(viewLifecycleOwner) { callLog ->
            if (callLog != null) {
                val updateCallLogData = CallLogData(
                    callLog.name,
                    callLog.number,
                    callLog.date,
                    callLog.duration,
                    callLog.callType,
                    callLog.callContent,
                    callLog.callKeyword,
                    false,
                    callLog.id
                )
                callLogViewModel.updateCallContent(updateCallLogData)
                adapter.importanceReset()
            }
        }
        adapter.callLogImportanceRemoving.observe(viewLifecycleOwner) { callLog ->
            if (callLog != null) {
                val updateCallLogData = CallLogData(
                    callLog.name,
                    callLog.number,
                    callLog.date,
                    callLog.duration,
                    callLog.callType,
                    callLog.callContent,
                    callLog.callKeyword,
                    true,
                    callLog.id
                )
                callLogViewModel.updateCallContent(updateCallLogData)
                adapter.importanceReset()
            }
        }

        // scroll bar
        val colorThumb = ContextCompat.getColor(requireContext(), R.color.hau_dark_green)
        val colorTrack = ContextCompat.getColor(requireContext(), android.R.color.transparent)
        with(binding) {
            scrollBar.attachTo(binding.callLogRecyclerView)
            scrollBar.defaultThumbTint = ColorStateList.valueOf(colorThumb)
            scrollBar.defaultTrackTint = ColorStateList.valueOf(colorTrack)
            // 화살 버튼 클릭 시, 리싸이클러뷰 최상단 이동
            btnUpScroll.setOnClickListener {
                binding.callLogRecyclerView.scrollToPosition(0)
            }
        }

        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.call_log_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.sort_by_contact -> {
                sortByContact()
                true
            }
            R.id.sort_by_importance -> {
                sortByImportance()
                true
            }
            R.id.sort_by_date -> {
                sortByDate()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // recyclerView 를 최근 날짜순으로 정렬(기본정렬)
    private fun sortByDate() {
        sortNumber.value = SORT_NORMAL_STATE
        showProgress(true)
    }

    // recyclerView 를 연락처 있는 통화기록만 정렬
    private fun sortByContact() {
        sortNumber.value = SORT_BY_CONTACT
        showProgress(true)
    }

    // recyclerView 를 연락처 있는 통화기록만 정렬
    private fun sortByImportance() {
        sortNumber.value = SORT_BY_IMPORTANCE
        showProgress(true)
    }

    private fun showProgress(show: Boolean) {
        binding.callLogProgressbar.visibility = if (show) View.VISIBLE else View.GONE
    }

}