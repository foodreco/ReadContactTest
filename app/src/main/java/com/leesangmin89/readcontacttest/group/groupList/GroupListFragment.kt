package com.leesangmin89.readcontacttest.group.groupList

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.leesangmin89.readcontacttest.databinding.FragmentGroupListBinding
import com.leesangmin89.readcontacttest.group.GroupViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GroupListFragment : Fragment() {

    private val binding by lazy { FragmentGroupListBinding.inflate(layoutInflater) }
    private val groupViewModel: GroupViewModel by viewModels()
    private val args by navArgs<GroupListFragmentArgs>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val adapter = GroupListAdapter(requireContext())
        binding.groupListRecyclerView.adapter = adapter

        // 넘어온 args를 매개로 GroupList 로부터 해당 그룹 정보를 가져오는 함수
        groupViewModel.getGroupListFromGroupList(args.groupName)

        // 기존 불러오려던 형태 작동코드
//        groupViewModel.getGroupListFromContactBase(args.groupName)
        // 기존 불러오려던 형태의 recyclerView
//        groupViewModel.groupList.observe(viewLifecycleOwner, {
//            adapter.submitList(it)
//        })

        // 신규 형태의 recyclerView
        groupViewModel.newGroupList.observe(viewLifecycleOwner,{
            adapter.submitList(it)
        })

        return binding.root
    }

}