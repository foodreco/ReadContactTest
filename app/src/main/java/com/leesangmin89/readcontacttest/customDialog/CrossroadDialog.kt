package com.leesangmin89.readcontacttest.customDialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.leesangmin89.readcontacttest.callLog.CallLogViewModel
import com.leesangmin89.readcontacttest.data.entity.CallLogData
import com.leesangmin89.readcontacttest.data.entity.ContactBase
import com.leesangmin89.readcontacttest.data.entity.GroupList
import com.leesangmin89.readcontacttest.databinding.CrossroadDialogBinding
import com.leesangmin89.readcontacttest.databinding.EditCallContentDialogBinding
import com.leesangmin89.readcontacttest.databinding.GroupAddDialogBinding
import com.leesangmin89.readcontacttest.group.GroupFragment
import com.leesangmin89.readcontacttest.group.GroupViewModel
import com.leesangmin89.readcontacttest.group.groupDetail.GroupDetailFragmentArgs
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CrossroadDialog : DialogFragment() {

    private val binding by lazy { CrossroadDialogBinding.inflate(layoutInflater) }
    private val groupViewModel: GroupViewModel by viewModels()
    private val args by navArgs<CrossroadDialogArgs>()

    // Dialog 배경 투명하게 하는 코드??
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding.textName.text = args.currentItem.name

        binding.callLogList.setOnClickListener {
            val action =
                CrossroadDialogDirections.actionCrossroadDialogToGroupDetailFragment(args.currentItem.number)
            findNavController().navigate(action)
        }

        when (args.currentItem.recommendation) {
            true -> {
                binding.alarmSetting.text = "알람 해제 하기"
                binding.alarmSetting.setOnClickListener {
                    // GroupList DB 업데이트
                    groupViewModel.findAndUpdate(
                        args.currentItem.name,
                        args.currentItem.number,
                        args.currentItem.group,
                        false
                    )
                    // 알림 false 설정 시, Reco DB 에서 해당 data 를 삭제하는 코드
                    // recommendation 이 해체되면, Recommendation DB 에서도 삭제되어야 함
                    groupViewModel.dataRecoDeleteByNumber(args.currentItem.number)
                }
            }
            false -> {
                binding.alarmSetting.text = "알람 설정 하기"
                binding.alarmSetting.setOnClickListener {
                    // GroupList DB 업데이트
                    groupViewModel.findAndUpdate(
                        args.currentItem.name,
                        args.currentItem.number,
                        args.currentItem.group,
                        true
                    )
                    groupViewModel.updateDialogDone()
                }
            }
        }

        // 알람설정 작업이 완료되면, dialog 종료
        groupViewModel.updateDialogDone.observe(viewLifecycleOwner, {
            if (it) {
//                val action = CrossroadDialogDirections.actionCrossroadDialogToGroupListFragment(args.currentItem.group)
//                findNavController().navigate(action)
                dismiss()
                groupViewModel.updateDialogDoneFinished()
            }
        })
        return binding.root
    }


}