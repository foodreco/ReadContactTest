package com.leesangmin89.readcontacttest.customDialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.leesangmin89.readcontacttest.convertLongToDateString
import com.leesangmin89.readcontacttest.convertLongToTimeString
import com.leesangmin89.readcontacttest.data.entity.Recommendation
import com.leesangmin89.readcontacttest.databinding.RecommendationDetailDialogBinding
import com.leesangmin89.readcontacttest.group.GroupViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RecommendationDetailDialog : DialogFragment() {

    private val binding by lazy { RecommendationDetailDialogBinding.inflate(layoutInflater) }

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

        val args = arguments?.getParcelable<Recommendation>("recommendation")

        with(binding) {
            name.text = args!!.name
            group.text = args.group
            if (args.recentContact != "") {
                recentCall.text = convertLongToDateString(args.recentContact!!.toLong())
                totalCallNumber.text = "${args.numberOfCalling} 회"
                avgCallTime.text = convertLongToTimeString(args.avgCallTime.toLong())
                callFrequency.text = "${(args.frequency?.toLong()?.div(1000) ?:0) / 60 / 60 / 24} 일"
            } else {
                recentCall.text = "-"
                totalCallNumber.text = "-"
                avgCallTime.text = "-"
                callFrequency.text = "-"
            }
        }

        binding.btnDismiss.setOnClickListener {
            dismiss()
        }

        return binding.root
    }

}