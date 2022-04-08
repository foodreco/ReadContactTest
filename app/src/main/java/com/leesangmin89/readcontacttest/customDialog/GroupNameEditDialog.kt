package com.leesangmin89.readcontacttest.customDialog

import android.app.Dialog
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.leesangmin89.readcontacttest.databinding.GroupNameEditDialogBinding
import com.leesangmin89.readcontacttest.group.GroupViewModel
import com.leesangmin89.readcontacttest.util.clearFocusAndHideKeyboard
import com.leesangmin89.readcontacttest.util.setFocusAndShowKeyboard

@AndroidEntryPoint
class GroupNameEditDialog : DialogFragment() {

    private val binding by lazy { GroupNameEditDialogBinding.inflate(layoutInflater) }
    private val args by navArgs<GroupNameEditDialogArgs>()
    private val groupViewModel: GroupViewModel by viewModels()

    // Dialog 배경 투명화 코드
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }

    // dialog 배경 match_parent 적용 코드
    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            dialog.window!!.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding.editGroupName.setText(args.groupName)

        // 키보드 show
        binding.editGroupName.setFocusAndShowKeyboard(requireContext())

        // 배경 터치 시, 키보드 내리고 이전으로 돌아가기
        binding.dialogConLayout.setOnClickListener {
            getBackToGroupFragment()
        }

        // 확인 버튼 터치 시, 그룹명 변경
        binding.btnEdit.setOnClickListener {
            groupNameEdit()
        }

        // 키보드 완료 버튼 시, 확인 버튼 터치와 동일 효과
        binding.editGroupName.setOnEditorActionListener { textView, action, event ->
            var handled = false
            if (action == EditorInfo.IME_ACTION_DONE) {
                groupNameEdit()
                handled = true
            }
            handled
        }

        // groupNameEdit 가 완료되면 이동
        groupViewModel.groupNameEditDone.observe(viewLifecycleOwner) { finished ->
            if (finished) {
                getBackToGroupFragment()
                groupViewModel.groupNameEditReset()
            }
        }

        return binding.root
    }

    private fun groupNameEdit() {
        when (val groupName = binding.editGroupName.text.toString()) {
            // 1. 입력값이 없을 때,
            "" -> Toast.makeText(requireContext(), "그룹명을 입력하세요", Toast.LENGTH_SHORT).show()
            // 2. 입력값 변화가 없을 때, 이전 fragment 로 회귀
            args.groupName -> {
                groupViewModel.groupNameEditFinished()
            }
            // 3. 그룹명을 변경했을 때,
            else -> {
                groupViewModel.groupNameEdit(args.groupName, groupName)
            }
        }
    }

    private fun getBackToGroupFragment() {
        binding.editGroupName.clearFocusAndHideKeyboard(requireContext())
        binding.editGroupName.postDelayed({
            dismiss()
            findNavController().navigate(
                GroupNameEditDialogDirections.actionGroupNameEditDialogToGroupFragment()
            )
        }, 50)
    }
}