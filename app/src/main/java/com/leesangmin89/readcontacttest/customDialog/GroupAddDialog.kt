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
import com.leesangmin89.readcontacttest.callLog.CallLogViewModel
import com.leesangmin89.readcontacttest.data.entity.CallLogData
import com.leesangmin89.readcontacttest.databinding.EditCallContentDialogBinding
import com.leesangmin89.readcontacttest.databinding.GroupAddDialogBinding
import com.leesangmin89.readcontacttest.group.GroupFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GroupAddDialog : DialogFragment() {

    private val binding by lazy { GroupAddDialogBinding.inflate(layoutInflater) }

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

        // 포커스 지정
        binding.addGroupName.requestFocus()
        // 키보드 올리기
        val mInputMethodManager =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        mInputMethodManager.showSoftInput(
            binding.addGroupName,
            InputMethodManager.SHOW_IMPLICIT
        )

        binding.btnInput.setOnClickListener {
            val groupName = binding.addGroupName.text.toString()
            if (groupName == "") {
                Toast.makeText(requireContext(), "그룹명을 입력하세요", Toast.LENGTH_SHORT).show()
            } else {
                dismiss()
                findNavController().navigate(
                    GroupAddDialogDirections.actionGroupAddDialogToGroupListAddFragment(
                        groupName
                    )
                )
            }
        }
        return binding.root
    }


}