package com.leesangmin89.readcontacttest.group.groupList

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.leesangmin89.readcontacttest.R
import com.leesangmin89.readcontacttest.callLog.CallLogAdapter
import com.leesangmin89.readcontacttest.callLog.CallLogViewModel
import com.leesangmin89.readcontacttest.data.entity.CallLogData
import com.leesangmin89.readcontacttest.databinding.FragmentGroupDetailBinding
import com.leesangmin89.readcontacttest.databinding.FragmentGroupListBinding
import com.leesangmin89.readcontacttest.group.GroupFragmentDirections
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GroupDetailFragment : Fragment() {

    private val binding by lazy { FragmentGroupDetailBinding.inflate(layoutInflater) }
    private val args by navArgs<GroupDetailFragmentArgs>()
    private val callLogViewModel: CallLogViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        //CallLogAdapter 사용하여 해당 User 기록만 출력
        val adapter = CallLogAdapter()
        binding.groupDetailRecyclerView.adapter = adapter

        // 콜로그 뷰모델 사용하여, 넘어온 번호에 해당하는 기록만 출력
        callLogViewModel.findAndReturn(args.currentItem.number)
        callLogViewModel.logList.observe(viewLifecycleOwner, {
            adapter.submitList(it)
            if (it == emptyList<CallLogData>()) {
                Log.i("보완", "커스텀 다이아로그로 디자인 개선")
                // 통화기록이 없는 경우, 확인창 띄우기
                val builder = AlertDialog.Builder(requireContext())
                builder.setPositiveButton("바로 전화걸기") { _, _ ->
                        val uri = Uri.parse("tel:${args.currentItem.number}")
                        val intent = Intent(Intent.ACTION_CALL, uri)
                        requireContext().startActivity(intent)
                }
                builder.setNegativeButton("다음에") { _, _ -> }
                builder.setTitle("통화기록 없음")
                builder.setMessage("최근 통화기록이 없습니다.")
                builder.create().show()
            }
        })

        return binding.root
    }

}