package com.leesangmin89.readcontacttest.group.groupList

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.leesangmin89.readcontacttest.R
import com.leesangmin89.readcontacttest.callLog.CallLogAdapter
import com.leesangmin89.readcontacttest.callLog.CallLogViewModel
import com.leesangmin89.readcontacttest.databinding.FragmentGroupDetailBinding
import com.leesangmin89.readcontacttest.databinding.FragmentGroupListBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GroupDetailFragment : Fragment() {

    private val binding by lazy { FragmentGroupDetailBinding.inflate(layoutInflater) }
    private val args by navArgs<GroupDetailFragmentArgs>()
    private val callLogViewModel : CallLogViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        //CallLogAdapter 사용하여 해당 User 기록만 출력
        val adapter = CallLogAdapter()
        binding.groupDetailRecyclerView.adapter = adapter

        // 콜로그 뷰모델 사용하여, 넘어온 번호에 해당하는 기록만 출력
        Log.i("확인","넘어온 번호 : ${args.currentItem.number}")
        callLogViewModel.findAndReturn(args.currentItem.number)
        callLogViewModel.logList.observe(viewLifecycleOwner, {
            adapter.submitList(it)
        })

        return binding.root
    }

}