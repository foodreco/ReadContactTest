package com.leesangmin89.readcontacttest.group

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
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

        //
        groupViewModel.groupInfo.observe(viewLifecycleOwner, {
            adapter.submitList(it)
        })

        groupViewModel.groupListEmptyEvent.observe(viewLifecycleOwner, {
            if (it) {
                val builder = AlertDialog.Builder(requireContext())
                builder.setPositiveButton("확인") { _, _ ->
                    groupViewModel.groupListEmptyChecked()
                }
                builder.setTitle("그룹 없음")
                builder.setMessage("연락처에 지정된 그룹이 없습니다.")
                builder.create().show()
            }
        })


        return binding.root
    }

}