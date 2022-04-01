package com.leesangmin89.readcontacttest.customDialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import com.hieupt.android.standalonescrollbar.attachTo
import com.leesangmin89.readcontacttest.R
import com.leesangmin89.readcontacttest.databinding.RecommendationListShowDialogBinding
import com.leesangmin89.readcontacttest.main.RecommendationListShowAdapter
import com.leesangmin89.readcontacttest.recommendationLogic.RecommendationViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RecommendationListShowDialog : DialogFragment() {

    private val binding by lazy { RecommendationListShowDialogBinding.inflate(layoutInflater) }
    private val recoViewModel: RecommendationViewModel by viewModels()


    // Dialog 배경 투명하게 하는 코드??
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val adapter = RecommendationListShowAdapter(requireContext())
        binding.recyclerViewRecommendationList.adapter = adapter

        // recommendation DB 를 불러오는 코드
        recoViewModel.getRecommendationMinimalLiveList().observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        // 확인 버튼 터치 시, dismiss
        binding.btnDismiss.setOnClickListener {
            dismiss()
        }

        // ? 버튼 터치 시, 추천 목록에 대한 설명 팝업
        binding.btnExplanation.setOnClickListener {
            Toast.makeText(context, "통화 대상을 추천 목록 중에서 선정합니다.\n추천 설정은 그룹 리스트에서 할 수 있습니다.", Toast.LENGTH_LONG).show()
        }

        // scroll bar
        val colorThumb = ContextCompat.getColor(requireContext(), R.color.hau_dark_green)
        val colorTrack = ContextCompat.getColor(requireContext(), android.R.color.transparent)
        with(binding) {
            scrollBar.attachTo(binding.recyclerViewRecommendationList)
            scrollBar.defaultThumbTint = ColorStateList.valueOf(colorThumb)
            scrollBar.defaultTrackTint = ColorStateList.valueOf(colorTrack)
        }

        return binding.root
    }

}