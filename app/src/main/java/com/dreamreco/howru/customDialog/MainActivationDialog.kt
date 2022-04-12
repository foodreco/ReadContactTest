package com.dreamreco.howru.customDialog

import android.app.Dialog
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.navArgs
import com.dreamreco.howru.R
import com.dreamreco.howru.databinding.FragmentMainActivationBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivationDialog : DialogFragment() {

    private val binding by lazy { FragmentMainActivationBinding.inflate(layoutInflater) }
    private val args by navArgs<MainActivationDialogArgs>()

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

//        여기에 설명 넣기 - 그래프가 무엇을 의미하는지
//        활성화도는 어떻게 계산되는지?

        // 다이어그램 변경 안되므로, 대입인자들 args 로 정하고 넘어와야 됨
        binding.diagram.valueList = arrayListOf(
            args.countNumbers.contactNumber.toFloat(),
            args.countNumbers.groupNumber.toFloat(),
            args.countNumbers.recoNumber.toFloat(),
            args.countNumbers.recoActivatedNumber.toFloat()
        )

        val customColorList = arrayListOf(
            // 바깥부터 순서대로 색 채워짐
            ContextCompat.getColor(requireContext(), R.color.diagram_color_1),
            ContextCompat.getColor(requireContext(), R.color.diagram_color_2),
            ContextCompat.getColor(requireContext(), R.color.diagram_color_3),
            ContextCompat.getColor(requireContext(), R.color.diagram_color_4)
        )

        binding.diagram.colorList = customColorList
        binding.diagram.showRawData = false

        with(binding) {
            imgDot1.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.diagram_color_1
                )
            )
            imgDot2.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.diagram_color_2
                )
            )
            imgDot3.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.diagram_color_3
                )
            )
            imgDot4.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.diagram_color_4
                )
            )
            textGroup1.text = "총 연락처 수 : ${args.countNumbers.contactNumber}개"
            textGroup2.text = "그룹 등록 수 : ${args.countNumbers.groupNumber}개"
            textGroup3.text = "알림 설정 수 : ${args.countNumbers.recoNumber}개"
            textGroup4.text = "통화 추천 수 : ${args.countNumbers.recoActivatedNumber}개"
        }

        // 확인 버튼 터치 시, dismiss
        binding.btnDismiss.setOnClickListener {
            dismiss()
        }

        // 물음표 버튼 터치 시
        binding.btnExplanation.setOnClickListener {
            Toast.makeText(context, "알림 상대와 활발하게 교류하세요.\n통화 추천 수가 낮을수록 활성화도가 올라갑니다.", Toast.LENGTH_LONG).show()
        }

        return binding.root
    }

}