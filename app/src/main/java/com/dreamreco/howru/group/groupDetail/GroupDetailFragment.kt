package com.dreamreco.howru.group.groupDetail

import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.navArgs
import com.dreamreco.howru.R
import com.dreamreco.howru.callLog.CallLogViewModel
import com.dreamreco.howru.customDialog.GroupDetailDialog
import com.dreamreco.howru.data.entity.CallLogData
import com.dreamreco.howru.databinding.FragmentGroupDetailBinding
import com.hieupt.android.standalonescrollbar.attachTo
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GroupDetailFragment : Fragment() {

    private val binding by lazy { FragmentGroupDetailBinding.inflate(layoutInflater) }
    private val args by navArgs<GroupDetailFragmentArgs>()
    private val callLogViewModel: CallLogViewModel by viewModels()
    private val adapter by lazy { GroupDetailAdapter(requireContext() ,childFragmentManager) }

    private val sortNumber = MutableLiveData<Int>(0)

    private val SORT_NORMAL_STATE = 0
    private val SORT_BY_IMPORTANCE = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding.groupDetailRecyclerView.adapter = adapter

        showProgress(true)

        // 헤더뷰홀더 리싸이클러뷰 코드
        callLogViewModel.callLogItemData.observe(viewLifecycleOwner){
            adapter.submitList(it)
            showProgress(false)
        }

        sortNumber.observe(viewLifecycleOwner) { number ->
            when (number) {
                SORT_NORMAL_STATE -> {
                    callLogViewModel.groupDetailList(args.phoneNumber).observe(viewLifecycleOwner) {
                        if (sortNumber.value == SORT_NORMAL_STATE) {
                            if (it == emptyList<List<CallLogData>>()) {
                                // 통화기록이 없는 경우, 확인창 띄우기
                                val dialog = GroupDetailDialog()
                                dialog.setButtonClickListener(object :
                                    GroupDetailDialog.OnButtonClickListener {
                                    // diaBtnCall 클릭 시,
                                    override fun onDiaBtnCallClicked() {
                                        // 전화걸기
                                        val uri = Uri.parse("tel:${args.phoneNumber}")
                                        val intent = Intent(Intent.ACTION_CALL, uri)
                                        requireContext().startActivity(intent)
                                    }

                                    // diaBtnCancel 클릭 시,
                                    override fun onDiaBtnCancelClicked() {
                                        // nothing
                                    }
                                })
                                // 확인창 띄우기
                                dialog.show(childFragmentManager, "CustomDialog")
                            }
                            callLogViewModel.makeList(it)
                        }
                    }
                }
                SORT_BY_IMPORTANCE -> {
                    callLogViewModel.groupDetailImportanceList(args.phoneNumber)
                        .observe(viewLifecycleOwner) { list ->
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
            scrollBar.attachTo(binding.groupDetailRecyclerView)
            scrollBar.defaultThumbTint = ColorStateList.valueOf(colorThumb)
            scrollBar.defaultTrackTint = ColorStateList.valueOf(colorTrack)
        }

        setHasOptionsMenu(true)
        return binding.root
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.group_detail_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
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
        showProgress(true)
        sortNumber.value = SORT_NORMAL_STATE
    }

    // recyclerView 를 연락처 있는 통화기록만 정렬
    private fun sortByImportance() {
        showProgress(true)
        sortNumber.value = SORT_BY_IMPORTANCE
    }

    private fun showProgress(show: Boolean) {
        binding.groupDetailProgressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

}