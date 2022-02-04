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

        groupViewModel.getGroupList(args.groupName)

        groupViewModel.groupList.observe(viewLifecycleOwner, {
            adapter.submitList(it)
        })

        return binding.root
    }

}