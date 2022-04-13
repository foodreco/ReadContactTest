package com.dreamreco.howru.group

import android.app.AlertDialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import com.dreamreco.howru.R
import com.dreamreco.howru.databinding.FragmentGroupBinding
import com.hieupt.android.standalonescrollbar.attachTo
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

        val adapter = GroupAdapter()
        binding.groupRecyclerView.adapter = adapter

        with(groupViewModel) {
            // GroupData 형태로 가공하는 함수
            getGroupName()

            // recyclerView 로 출력
            groupFragmentList.observe(viewLifecycleOwner) {
                adapter.addHeaderAndSubmitList(it)
            }
        }

        // recyclerView 출력전 정렬을 위한 변환 코드
        sortNumber.observe(viewLifecycleOwner) { number ->
            when (number) {
                SORT_BY_IMPORTANCE -> {
                    groupViewModel.groupInfo.observe(viewLifecycleOwner) {
                        if (sortNumber.value == SORT_BY_IMPORTANCE) {
                            groupViewModel.sortGroupByImportance(it)
                        }
                    }
                }
                SORT_BY_MEMBERS -> {
                    groupViewModel.groupInfo.observe(viewLifecycleOwner) {
                        if (sortNumber.value == SORT_BY_MEMBERS) {
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


    // 메뉴 활성화
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.group_menu, menu)
    }

    // 메뉴 터치 시 작동
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_group_add ->
                groupViewModel.isContactBaseEmpty().observe(viewLifecycleOwner) {
                    if (it == null) {
                        Toast.makeText(requireContext(), "연락처가 없습니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        addGroup()
                    }
                }
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