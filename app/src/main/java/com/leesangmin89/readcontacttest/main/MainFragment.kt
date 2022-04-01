package com.leesangmin89.readcontacttest.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.leesangmin89.readcontacttest.CUSTOM_CHART_COLORS
import com.leesangmin89.readcontacttest.R
import com.leesangmin89.readcontacttest.customDialog.RecommendationListShowDialog
import com.leesangmin89.readcontacttest.data.entity.Recommendation
import com.leesangmin89.readcontacttest.data.entity.Tendency
import com.leesangmin89.readcontacttest.databinding.FragmentMainBinding
import com.leesangmin89.readcontacttest.recommendationLogic.NumberForStatusProgressbar
import com.leesangmin89.readcontacttest.recommendationLogic.RecommendationViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.roundToInt
import kotlin.random.Random

@AndroidEntryPoint
class MainFragment : Fragment() {
    // 요청 권한 리스트
    companion object {
        val PERMISSIONS = arrayOf(
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_CALL_LOG
        )
    }

    private val binding by lazy { FragmentMainBinding.inflate(layoutInflater) }
    private val recoViewModel: RecommendationViewModel by viewModels()
    private val mainViewModel: MainViewModel by viewModels()
    private val adapter: CallRecommendAdapter by lazy {
        CallRecommendAdapter(
            requireContext(),
            childFragmentManager
        )
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        Log.d("보완", "ClickablePieChart 그룹 비율 보여주기 - https://android-arsenal.com/details/1/8168")
        Log.d("개선", "가끔 MainFragment 먹통 - 함수가 무거워서??")

//        그룹 비율 보여주기?

        showProgressInRecommendation(true)
        showProgressInTendency(true)
        showStatusProgressbar(true)

        // 허용 체크 후, 기초 정보 구축하기
        checkPermissionsAndStart(PERMISSIONS)

        mainViewModel.makeRecommendationInfoEvent.observe(viewLifecycleOwner) { ready ->
            if (ready) {
                // 실행 시 매번 업데이트 되어야 함
                makeRecommendationInfo()
                mainViewModel.makeRecommendationInfoEventDone()
            }
        }

        recoViewModel.getRecommendationLiveList().observe(viewLifecycleOwner) {
            if (it == emptyList<Recommendation>()) {
                Log.d("보완", "testList null 일 때, '관계 유지가 잘 되고 있습니다.' view 띄우기")
                Toast.makeText(
                    requireContext(),
                    "관계 유지가 잘 되고 있습니다. \n 알림친구가 없다면 설정하세요.",
                    Toast.LENGTH_SHORT
                ).show()
                showProgressInRecommendation(false)
            } else {
                // call 추천
                // indicator 실시간 반영 때문에 옵저버 적용
                // ViewPager2 사용
                binding.recommendationViewPager.adapter = adapter
                binding.recommendationViewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
                // spring indicator 실시간 적용
                binding.dotsIndicator.setViewPager2(binding.recommendationViewPager)
                // 갯수 제한하기? 5명
                adapter.submitList(it.take(5))
                showProgressInRecommendation(false)

                // 유지상태 프로그래스바 활성화 코드
                recoViewModel.makeDataForStatusProgressbar(it)
            }
        }

        // StatusProgressbar 활성화 코드
        recoViewModel.mainStatusProgressbar.observe(viewLifecycleOwner) {
            activateStatusProgressBar(it)
        }

        // 경향 LiveData 출력 코드
        recoViewModel.getTendencyLive()?.observe(viewLifecycleOwner) { tendency ->
            if (tendency != null) {
                getBindTextView(tendency)
            }
        }

        // 정보 detail 을 보여주는 코드
        binding.tendencyLayout.setOnClickListener {
            val action = MainFragmentDirections.actionMainFragmentToMainSubFragment()
            it.findNavController().navigate(action)
        }

        // Recommendation List 를 보여주는 버튼
        binding.btnShowRecoList.setOnClickListener {
            val dialog = RecommendationListShowDialog()
            dialog.show(childFragmentManager, "RecommendationListShowDialog")
        }

        // PieChart 데이터 생성 코드
        mainViewModel.getGroupNameListDataLive().observe(viewLifecycleOwner){
            mainViewModel.getGroupChartData(it)
        }
        // PieChart 데이터 출력 코드
        mainViewModel.mainGroupChartData.observe(viewLifecycleOwner) {
            if (it != null) {
                activateGroupPieChart(it)
                showStatusProgressbar(false)
            }
        }

        // 기본세팅 : 180 이 max scale 인 프로그래스바
        with(binding.statusProgressbar) {
            // 배경 bar 두께
            setProgressThickness(20f)
            // 진행 bar 두께
            setForeGroundProgressThickness(30f)
            setRoundedCorner(true)
        }

        return binding.root
    }

    private fun activateGroupPieChart(groupChartList: List<MainGroupChartData>) {
        // MP 차트 관련 코드
        with(binding.mpChart) {
            // 데이터 없을 때, 텍스트 생략
            setNoDataText("")
            // 퍼센트값 적용
            setUsePercentValues(true)
            description.isEnabled = false
            isRotationEnabled = false
            setExtraOffsets(0f, 0f, 0f, 0f)
            // 그래프 아이템 이름색상
            setEntryLabelColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            // 그래프 아이템 이름 크기, 글자유형
            setEntryLabelTextSize(16f)
            setEntryLabelTypeface(android.graphics.Typeface.DEFAULT_BOLD)
            // 가운데 구멍 생성 여부
            isDrawHoleEnabled = true
            // 가운데 구멍 색상
            setHoleColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            // 가운데 불투명써클 크기
            transparentCircleRadius = 0f
            // 차트 범례 표현 여부
            legend.isEnabled = false
            // 중앙 텍스트 및 크기, 색상, 글자유형
            centerText = "비율\n(%)"
            setCenterTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.hau_dark_green
                )
            )
            setCenterTextSize(24f)
            setCenterTextTypeface(android.graphics.Typeface.DEFAULT_BOLD)

            // 그래프 애니메이션 (https://superkts.com/jquery/@easingEffects)
            // ★ 이게 있어야 실시간으로 업데이트 됨
             animateY(10, Easing.EaseInCubic)

            // data set - 데이터 넣어주기
            // 인수가 많은 집합부터 불러오기(사람수 많은 그룹부터 불러오기)
            // 데이터 넣기, 조건부로 null 일 때 넣을 리스트 따로 만들기
            val entries = ArrayList<PieEntry>()
            if (groupChartList == emptyList<MainGroupChartData>()) {
                entries.add(PieEntry(100f, "그룹없음"))
            } else {
                for (list in groupChartList) {
                    entries.add(PieEntry(list.groupNumber, list.groupName))
                }
            }

            // 데이터 관련 옵션 정하기
            val dataSet = PieDataSet(entries, "")
            with(dataSet) {
                // 그래프 사이 간격
                sliceSpace = 1f
                colors = CUSTOM_CHART_COLORS
            }

            // 차트 제목 텍스트?
            val pieData = PieData(dataSet)
            with(pieData) {
                // 그래프 value(수치) 크기
                setValueTextSize(16f)
                // value 색상
                setValueTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
            }
            data = pieData
        }
    }

    private fun activateStatusProgressBar(it: NumberForStatusProgressbar) {
        // 활성화도 : 추천대상인 중, 교류하고 있는 사람 비율(높을수록 교류 양호)
        val rateText =
            ((it.allNumber - it.recommendedNumber).toDouble()) / (it.allNumber.toDouble()) * 100
        // bar 지수 (180을 기준으로 함)
        val barState = rateText / 100 * 180
        binding.textStatusProgressbar.text =
            getString(R.string.recommendation_active_rate, rateText.toInt())
        binding.statusProgressbar.setProgressWithAnimation(barState.toFloat(), 3000)
        when (barState.toFloat()) {
            in 0f..59f -> {
                // 색상 적용
                binding.statusProgressbar.setForegroundProgressColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.light_red
                    )
                )
                // 멘트 적용
                binding.textTotalActivation.text = "# 교류가 부족해요."
            }
            in 60f..119f -> {
                // 색상 적용
                binding.statusProgressbar.setForegroundProgressColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.light_orange
                    )
                )
                // 멘트 적용
                binding.textTotalActivation.text = "# 적당히 연락하고 있어요."
            }
            in 120f..180f -> {
                // 색상 적용
                binding.statusProgressbar.setForegroundProgressColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.hau_green
                    )
                )
                // 멘트 적용
                binding.textTotalActivation.text = "# 활발하게 교류하고 있어요."
            }
        }
    }

    // CallLogCalls 데이터를 순회하며, Recommendation 정보를 가져오는 함수
    @SuppressLint("Range")
    fun makeRecommendationInfo() {
        showProgressInRecommendation(true)
        Log.d("수정", "최초 앱 빌드 시, The application may be doing too much work on its main thread.")
        Log.d("수정", "매번 업데이트 해야 하는 함수를 가볍게 개선 -> 세부사항 눌렀을 때만 로드 걸리도록")
        // contactInfo 전화 통계 데이터 갱신
        // 실행 시 매번 업데이트 되어야 함
        recoViewModel.arrangeRecommendation()
    }

    private fun showProgressInRecommendation(show: Boolean) {
        binding.progressBarReco.visibility = if (show) View.VISIBLE else View.INVISIBLE
    }

    private fun showProgressInTendency(show: Boolean) {
        binding.progressBarTendency.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showStatusProgressbar(show: Boolean) {
        binding.mpChartProgressBar.visibility = if (show) View.VISIBLE else View.GONE
    }


    private fun getBindTextView(tendency: Tendency) {
        var callTypeIncomingRate = 0
        var callTypeOutgoingRate = 0
        var callTypeMissedRate = 0
        var callDurationAvg = 0
        var callDurationAvgGroup = 0
        var callDurationAvgReco = 0

        if (tendency.allCallCount != "0") {
            callTypeIncomingRate = tendency.allCallIncoming.toInt() / tendency.allCallCount.toInt()
            callTypeOutgoingRate = tendency.allCallOutgoing.toInt() / tendency.allCallCount.toInt()
            callTypeMissedRate = tendency.allCallMissed.toInt() / tendency.allCallCount.toInt()
            callDurationAvg = tendency.allCallDuration.toInt() / tendency.allCallCount.toInt()
        }

        if (tendency.groupListCallCount != "0") {
            callDurationAvgGroup =
                tendency.groupListCallDuration.toInt() / tendency.groupListCallCount.toInt()
        }

        if (tendency.recommendationCallCount != "0") {
            callDurationAvgReco =
                tendency.recommendationCallDuration.toInt() / tendency.recommendationCallCount.toInt()
        }

        val callPartnerNumber = tendency.allCallPartnerList.count()
        var callTypeTendency = "발수신 성향"
        var callTypeTendencyInReco = "알람 발수신 성향"
        var callDurationTendency = "총 통화 시간 성향"

        callTypeTendency =
            if (tendency.allCallIncoming.toInt() >= tendency.allCallOutgoing.toInt()) {
                val rating = (((tendency.allCallIncoming.toDouble()
                    .minus(tendency.allCallOutgoing.toDouble())) / tendency.allCallOutgoing.toDouble()) * 100).toInt()
                "'전화를 받는 편이에요.'\n($rating% 더 받아요.)"

            } else {
                val rating = (((tendency.allCallOutgoing.toDouble()
                    .minus(tendency.allCallIncoming.toDouble())) / tendency.allCallIncoming.toDouble()) * 100).toInt()
                "'전화를 거는 편이에요.'\n($rating% 더 걸어요.)"
            }

        callTypeTendencyInReco =
            if (tendency.recommendationCallIncoming.toInt() >= tendency.recommendationCallOutgoing.toInt()) {
                val rating = (((tendency.recommendationCallIncoming.toDouble()
                    .minus(tendency.recommendationCallOutgoing.toDouble())) / tendency.recommendationCallOutgoing.toDouble()) * 100).toInt()
                "'전화를 받는 편이에요.'\n($rating% 더 받아요.)"
            } else {
                val rating = (((tendency.recommendationCallOutgoing.toDouble()
                    .minus(tendency.recommendationCallIncoming.toDouble())) / tendency.recommendationCallIncoming.toDouble()) * 100).toInt()
                "'전화를 거는 편이에요.'\n($rating% 더 걸어요.)"
            }

        callDurationTendency =
            "'전체 전화 ${callDurationAvg / 60} 분'\n'그룹 전화 ${callDurationAvgGroup / 60} 분'\n'추천 전화 ${callDurationAvgReco / 60} 분' 입니다."

        binding.textCallTypeTendency.text = callTypeTendency
        binding.textCallTypeTendencyReco.text = callTypeTendencyInReco
        binding.textCallDuration.text = callDurationTendency

        showProgressInTendency(false)
    }

    // 초기 데이터 로드 함수(ContactBase)
    @SuppressLint("Range")
    fun appBuildLoadContact() {
        mainViewModel.appBuildLoadContact(requireActivity())
    }


    // 허용 체크 후, appBuildLoadContact() 시작
    private fun checkPermissionsAndStart(permissions: Array<out String>) {
        if (!checkNeedPermissionBoolean(permissions)) {
            // 허용 안되어 있는 경우, 요청
            requestMultiplePermissions.launch(
                permissions
            )
        } else {
            // 허용 되어있는 경우, 통화기록, 통계 가져오기
            appBuildLoadContact()
        }
    }

    // 허용 여부에 따른 Boolean 반환
    private fun checkNeedPermissionBoolean(permissions: Array<out String>): Boolean {
        for (perm in permissions) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    perm
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    // 허용 요청 코드
    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->

            val granted = permissions.entries.all {
                it.value == true
            }

            if (granted) {
                // 허용된, 경우
                Toast.makeText(context, "권한 허용", Toast.LENGTH_SHORT).show()
                appBuildLoadContact()
            } else {
                // 허용안된 경우,
                Toast.makeText(context, "권한 거부", Toast.LENGTH_SHORT).show()
            }
        }
}