package com.leesangmin89.readcontacttest.customDialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.room.PrimaryKey
import com.leesangmin89.readcontacttest.callLog.CallLogViewModel
import com.leesangmin89.readcontacttest.data.entity.CallLogData
import com.leesangmin89.readcontacttest.databinding.EditCallContentDialogBinding
import com.leesangmin89.readcontacttest.group.groupList.GroupDetailFragment
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class EditCallContent : DialogFragment() {

    private val callLogViewModel: CallLogViewModel by viewModels()
    private val binding by lazy { EditCallContentDialogBinding.inflate(layoutInflater) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val args = arguments?.getParcelable<CallLogData>("callLogData")
        binding.callContent.setText(args!!.callContent)
        binding.contentKeyword.setText(args.callKeyword)
        binding.btnContentSave.setOnClickListener {
            val updateText = binding.callContent.text.toString()
            val updateKeyword = binding.contentKeyword.text.toString()
            val updateCallLogData = CallLogData(
                args.name,
                args.number,
                args.date,
                args.duration,
                args.callType,
                updateText,
                updateKeyword,
                args.id
            )
            callLogViewModel.updateCallContent(updateCallLogData)
        }

        // updateCallContent(updateCallLogData) 작업 완료되면 dialog 를 종료하는 코드
        callLogViewModel.dialogDismissEvent.observe(viewLifecycleOwner, { finished ->
            if (finished) {
                dismiss()
                callLogViewModel.diaLogDismissDone()
            }
        })
        return binding.root
    }
}