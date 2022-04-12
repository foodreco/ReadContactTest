package com.dreamreco.howru.customDialog

import android.app.Dialog
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.dreamreco.howru.databinding.GroupAddDialogBinding
import dagger.hilt.android.AndroidEntryPoint
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import com.dreamreco.howru.util.clearFocusAndHideKeyboard
import com.dreamreco.howru.util.setFocusAndShowKeyboard

@AndroidEntryPoint
class GroupAddDialog : DialogFragment() {

    private val binding by lazy { GroupAddDialogBinding.inflate(layoutInflater) }

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

        // 키보드 show
        binding.addGroupName.setFocusAndShowKeyboard(requireContext())

        // 배경 터치 시, 키보드 내리고 이전으로 돌아가기
        binding.dialogConLayout.setOnClickListener {
            binding.addGroupName.clearFocusAndHideKeyboard(requireContext())
            binding.addGroupName.postDelayed({
                dismiss()
                findNavController().navigate(
                    GroupAddDialogDirections.actionGroupAddDialogToGroupFragment()
                )
            }, 50)
        }

        // 확인 버튼 터치 시,
        binding.btnInput.setOnClickListener {
            addGroupTask()
        }

        // 키보드 완료 버튼 시, 확인 버튼 터치와 동일 효과
        binding.addGroupName.setOnEditorActionListener { textView, action, event ->
            var handled = false
            if (action == EditorInfo.IME_ACTION_DONE) {
                addGroupTask()
                handled = true
            }
            handled
        }

        return binding.root
    }

    private fun addGroupTask() {
        val groupName = binding.addGroupName.text.toString()
        if (groupName == "") {
            Toast.makeText(requireContext(), "그룹명을 입력하세요", Toast.LENGTH_SHORT).show()
        } else {
            // addGroupName 에 지정된 포커스 제거 및 키보드 내리기
            binding.addGroupName.clearFocusAndHideKeyboard(requireContext())
            // 시간을 두고 dismiss 후 Fragment 이동
            binding.btnInput.postDelayed({
                dismiss()
                findNavController().navigate(
                    GroupAddDialogDirections.actionGroupAddDialogToGroupListAddFragment(
                        groupName
                    )
                )
            }, 50)
        }
    }
}