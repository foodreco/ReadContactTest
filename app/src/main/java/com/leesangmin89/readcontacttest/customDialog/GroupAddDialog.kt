package com.leesangmin89.readcontacttest.customDialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.leesangmin89.readcontacttest.databinding.GroupAddDialogBinding
import com.leesangmin89.readcontacttest.group.groupListAdd.GroupListAddViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GroupAddDialog() : DialogFragment() {

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

//        groupListAddViewModel.keyboardEvent.observe(viewLifecycleOwner,{
//            if (it) {
//                activeKeyboard()
//                groupListAddViewModel.inActiveKeyboard()
//                Log.i("확인","keyboardEvent")
//            }
//        })

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

    private fun activeKeyboard() {
        // 포커스 지정
        binding.addGroupName.requestFocus()
        binding.addGroupName.setSelection(binding.addGroupName.text.length)
        // 키보드 올리기
        Log.i("수정","키보드 작동 코드 개선")
//        val mInputMethodManager =
//            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//        mInputMethodManager.showSoftInput(
//            binding.addGroupName,
//            InputMethodManager.SHOW_IMPLICIT
//        )
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
    }


}