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
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.leesangmin89.readcontacttest.R
import com.leesangmin89.readcontacttest.callLog.CallLogAdapter
import com.leesangmin89.readcontacttest.callLog.CallLogViewModel
import com.leesangmin89.readcontacttest.customDialog.GroupDetailDialog
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
        val adapter = GroupDetailAdapter(childFragmentManager)
        binding.groupDetailRecyclerView.adapter = adapter

        Log.d("추가", "CallLogData 메모 기능 추가 / 간단하게는 키워드만 보이기")

        // 넘어온 number 에 해당하는 CallLogData 만 출력
        callLogViewModel.findAndReturnLive(args.phoneNumber)
        callLogViewModel.groupDetailList.observe(viewLifecycleOwner,{
            adapter.submitList(it)
            if (it == emptyList<CallLogData>()) {
                // 통화기록이 없는 경우, 확인창 띄우기
                val dialog = GroupDetailDialog()
                dialog.setButtonClickListener(object : GroupDetailDialog.OnButtonClickListener {
                    override fun onButton1Clicked() {
                        // diaBtnCall 클릭 시,
                        // 전화걸기
                        val uri = Uri.parse("tel:${args.phoneNumber}")
                        val intent = Intent(Intent.ACTION_CALL, uri)
                        requireContext().startActivity(intent)
                    }

                    override fun onButton2Clicked() {
                        // diaBtnCancel 클릭 시,
                    }
                })
                // 확인창 띄우기
                dialog.show(childFragmentManager, "CustomDialog")
            }
        })

        return binding.root
    }

}