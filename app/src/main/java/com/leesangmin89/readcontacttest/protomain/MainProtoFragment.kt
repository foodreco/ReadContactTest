package com.leesangmin89.readcontacttest.protomain

import android.annotation.SuppressLint
import android.os.Bundle
import android.provider.CallLog
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.leesangmin89.readcontacttest.RecommendationSpl
import com.leesangmin89.readcontacttest.databinding.FragmentMainProtoBinding
import com.leesangmin89.readcontacttest.recommendationLogic.RecommendationViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainProtoFragment : Fragment() {

    private val binding by lazy { FragmentMainProtoBinding.inflate(layoutInflater) }
    private val recoViewModel: RecommendationViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        getPhoneInfo()

        recoViewModel.progressBarEventFinished.observe(viewLifecycleOwner, {
            if (it) {
                showProgress(false)
                recoViewModel.progressBarEventDone()
            }
        })

        recoViewModel.testList.observe(viewLifecycleOwner,{
            Log.i("확인","옵저버 : $it")
        })
        return binding.root
    }

    // CallLogCalls 데이터를 순회하며, Recommendation 정보를 가져오는 함수
    @SuppressLint("Range")
    fun getPhoneInfo() {
        Log.d("확인", "MainProtoFragment, getPhoneInfo 작동 ")

//        showProgress(true)
        Log.d("수정", "최초 앱 빌드 시, The application may be doing too much work on its main thread.")
        // contactInfo 전화 통계 데이터 갱신
        recoViewModel.arrangeRecommendation()
    }

    fun showProgress(show: Boolean) {
        binding.progressBarMainProto.visibility = if (show) View.VISIBLE else View.GONE
    }
}