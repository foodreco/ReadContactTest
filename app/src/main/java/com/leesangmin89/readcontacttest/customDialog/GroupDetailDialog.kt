package com.leesangmin89.readcontacttest.customDialog

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.leesangmin89.readcontacttest.R
import com.leesangmin89.readcontacttest.databinding.GroupdetailDialogBinding

class GroupDetailDialog : DialogFragment() {
    private var _binding: GroupdetailDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = GroupdetailDialogBinding.inflate(inflater, container, false)
        // dialog 레이아웃 배경색 지정
        // (중요) Dialog는 내부적으로 뒤에 흰 사각형 배경이 존재하므로, 배경을 투명하게 만들지 않으면
        // corner radius의 적용이 보이지 않는다.
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // 각 버튼 클릭 시 각각의 함수 호출
        binding.diaBtnCall.setOnClickListener {
            buttonClickListener.onButton1Clicked()
            dismiss()
        }
        binding.diaBtnCancel.setOnClickListener {
            buttonClickListener.onButton2Clicked()
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
        fun onButton1Clicked()
        fun onButton2Clicked()
    }

    // 클릭 이벤트 설정
    fun setButtonClickListener(buttonClickListener: OnButtonClickListener) {
        this.buttonClickListener = buttonClickListener
    }

    // 클릭 이벤트 실행
    private lateinit var buttonClickListener: OnButtonClickListener
}