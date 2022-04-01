package com.leesangmin89.readcontacttest.group

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import com.google.android.material.transition.MaterialFadeThrough
import com.hieupt.android.standalonescrollbar.attachTo
import com.leesangmin89.readcontacttest.R
import com.leesangmin89.readcontacttest.data.entity.CallLogData
import com.leesangmin89.readcontacttest.databinding.FragmentGroupBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GroupFragment : Fragment() {

    private val binding by lazy { FragmentGroupBinding.inflate(layoutInflater) }
    private val groupViewModel: GroupViewModel by viewModels()

    private val sortNumber = MutableLiveData<Int>(0)

    private val SORT_BY_IMPORTANCE = 0
    private val SORT_BY_MEMBERS = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        Log.i("수정", "검색 후 알맞은 라이브러리 적용")

        val adapter = GroupAdapter()
        binding.groupRecyclerView.adapter = adapter

        with(groupViewModel) {
            // GroupData 형태로 가공하는 함수
            getGroupName()

            // recyclerView 로 출력
            groupFragmentList.observe(viewLifecycleOwner){
                adapter.submitList(it)
            }
        }

        // recyclerView 출력전 정렬을 위한 변환 코드
        sortNumber.observe(viewLifecycleOwner) { number ->
            when (number) {
                SORT_BY_IMPORTANCE -> {
                    groupViewModel.groupInfo.observe(viewLifecycleOwner) {
                        if (sortNumber.value == SORT_BY_IMPORTANCE) {
                            if (it == emptyList<List<GroupData>>()) {
                                popUpDialog()
                            } else {
                                groupViewModel.sortGroupByImportance(it)
                            }
                        }
                    }
                }
                SORT_BY_MEMBERS -> {
                    groupViewModel.groupInfo.observe(viewLifecycleOwner) {
                        if (sortNumber.value == SORT_BY_MEMBERS) {
                            if (it == emptyList<List<GroupData>>()) {
                                popUpDialog()
                            }
                            groupViewModel.sortGroupByMembers(it)
                        }
                    }
                }
            }
        }


        // scroll bar
        val colorThumb = ContextCompat.getColor(requireContext(), R.color.hau_dark_green)
        val colorTrack = ContextCompat.getColor(requireContext(), android.R.color.transparent)
        with(binding) {
            scrollBar.attachTo(binding.groupRecyclerView)
            scrollBar.defaultThumbTint = ColorStateList.valueOf(colorThumb)
            scrollBar.defaultTrackTint = ColorStateList.valueOf(colorTrack)
        }

        setHasOptionsMenu(true)
        return binding.root
    }

    // 그룹이 empty 인 경우, 확인창을 띄우는 코드
    private fun popUpDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("그룹추가") { dialog, _ ->
            Log.i("확인", "다이아로그 ok")
            dialog.dismiss()
            addGroup()
        }
        builder.setTitle("그룹 없음")
        builder.setMessage("연락처에 지정된 그룹이 없습니다. \n 그룹을 추가하세요!")
        builder.create().show()
    }

    // 메뉴 활성화
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.group_menu, menu)
    }

    // 메뉴 터치 시 작동
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_group_add -> addGroup()
            R.id.group_sort_by_members -> sortByMembers()
            R.id.group_sort_by_importance -> sortByImportance()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun sortByImportance() {
        sortNumber.value = SORT_BY_IMPORTANCE
    }

    private fun sortByMembers() {
        sortNumber.value = SORT_BY_MEMBERS
    }

    private fun addGroup() {
        // Dialog 를 거쳐서 GroupListAddFragment 까지 가야하므로, navigation 을 사용한다.
        findNavController().navigate(GroupFragmentDirections.actionGroupFragmentToGroupAddDialog())
    }

}