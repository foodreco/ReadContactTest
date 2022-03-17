package com.leesangmin89.readcontacttest.customDialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.leesangmin89.readcontacttest.databinding.GroupdetailDialogBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GroupDetailDialog : DialogFragment() {

    private var _binding: GroupdetailDialogBinding? = null
    private val binding get() = _binding!!

    // 클릭 이벤트 실행
    private lateinit var buttonClickListener: OnButtonClickListener

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        _binding = GroupdetailDialogBinding.inflate(inflater, container, false)

        // 각 버튼 클릭 시 각각의 함수 호출
        binding.diaBtnCall.setOnClickListener {
            buttonClickListener.onDiaBtnCallClicked()
            dismiss()
        }
        binding.diaBtnCancel.setOnClickListener {
            buttonClickListener.onDiaBtnCancelClicked()
            dismiss()
        }
        return binding.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // 인터페이스
    interface OnButtonClickListener {
        fun onDiaBtnCallClicked()
        fun onDiaBtnCancelClicked()
    }

    // 클릭 이벤트 설정
    fun setButtonClickListener(buttonClickListener: OnButtonClickListener) {
        this.buttonClickListener = buttonClickListener
    }

}