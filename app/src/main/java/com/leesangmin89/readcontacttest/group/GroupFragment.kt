package com.leesangmin89.readcontacttest.group

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.ui.state.ToggleableState
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.navigation.fragment.findNavController
import com.leesangmin89.readcontacttest.databinding.FragmentGroupBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GroupFragment : Fragment() {

    private val binding by lazy { FragmentGroupBinding.inflate(layoutInflater) }
    private val groupViewModel: GroupViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val adapter = GroupAdapter(requireContext())
        binding.groupRecyclerView.adapter = adapter

        // onCreateView 와 함께 리스트 업데이트
        groupViewModel.getGroupName()

        // 업데이트 리스트 recyclerView로 반환
        groupViewModel.groupInfo.observe(viewLifecycleOwner, {
            adapter.submitList(it)
        })

        groupViewModel.groupListEmptyEvent.observe(viewLifecycleOwner, {
            if (it) {
                // 그룹이 empty 인 경우, 확인창 띄우기
                val builder = AlertDialog.Builder(requireContext())
                builder.setPositiveButton("그룹추가") { _, _ ->
                    groupViewModel.groupListEmptyChecked()
                    val action = GroupFragmentDirections.actionGroupFragmentToListFragment()
                    findNavController().navigate(action)
                }
                builder.setTitle("그룹 없음")
                builder.setMessage("연락처에 지정된 그룹이 없습니다. \n 그룹을 추가하세요!")
                builder.create().show()
            }
        })
        return binding.root
    }
}