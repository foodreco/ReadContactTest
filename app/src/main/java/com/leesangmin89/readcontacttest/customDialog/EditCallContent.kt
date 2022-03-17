package com.leesangmin89.readcontacttest.customDialog

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.leesangmin89.readcontacttest.callLog.CallLogViewModel
import com.leesangmin89.readcontacttest.data.entity.CallLogData
import com.leesangmin89.readcontacttest.databinding.EditCallContentDialogBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditCallContent : DialogFragment() {

    private val callLogViewModel: CallLogViewModel by viewModels()
    private val binding by lazy { EditCallContentDialogBinding.inflate(layoutInflater) }

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

        val args = arguments?.getParcelable<CallLogData>("callLogData")
        binding.callContent.setText(args!!.callContent)
        binding.contentKeyword.setText(args.callKeyword)
        binding.checkBoxImportance.isChecked = args.importance!!

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
                binding.checkBoxImportance.isChecked,
                args.id
            )
            callLogViewModel.updateCallContent(updateCallLogData)
        }

        // updateCallContent(updateCallLogData) 작업 완료되면 dialog 를 종료하는 코드
        callLogViewModel.dialogDismissEvent.observe(viewLifecycleOwner) { finished ->
            if (finished) {
                dismiss()
                callLogViewModel.diaLogDismissDone()
            }
        }
        return binding.root
    }

}