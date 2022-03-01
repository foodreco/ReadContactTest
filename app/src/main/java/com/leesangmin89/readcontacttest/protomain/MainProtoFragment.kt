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
import androidx.viewpager2.widget.ViewPager2
import com.leesangmin89.readcontacttest.RecommendationSpl
import com.leesangmin89.readcontacttest.databinding.FragmentMainProtoBinding
import com.leesangmin89.readcontacttest.recommendationLogic.RecommendationViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainProtoFragment : Fragment() {

    private val binding by lazy { FragmentMainProtoBinding.inflate(layoutInflater) }
    private val recoViewModel: RecommendationViewModel by viewModels()
    private val adapter : CallRecommendAdapter by lazy { CallRecommendAdapter(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // 실행 시 매번 업데이트 되어야 함
        getPhoneInfo()

        showProgress(true)

        //ViewPager2 적용
        binding.recommendationViewPager.adapter = adapter
        binding.recommendationViewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        recoViewModel.progressBarEventFinished.observe(viewLifecycleOwner, {
            if (it) {
                showProgress(false)
                recoViewModel.progressBarEventDone()
            }
        })

        //ViewPager2 적용
        recoViewModel.testList.observe(viewLifecycleOwner, {
            adapter.submitList(it)
            Log.i("확인","$it")
        })
        return binding.root
    }

    // CallLogCalls 데이터를 순회하며, Recommendation 정보를 가져오는 함수
    @SuppressLint("Range")
    fun getPhoneInfo() {
//        showProgress(true)
        Log.d("수정", "최초 앱 빌드 시, The application may be doing too much work on its main thread.")

        // contactInfo 전화 통계 데이터 갱신
        // 실행 시 매번 업데이트 되어야 함
        recoViewModel.arrangeRecommendation()
    }

    private fun showProgress(show: Boolean) {
        binding.progressBarMainProto.visibility = if (show) View.VISIBLE else View.GONE
    }
}