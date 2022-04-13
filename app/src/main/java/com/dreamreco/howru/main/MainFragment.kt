package com.dreamreco.howru.main

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.PieEntry
import com.dreamreco.howru.R
import com.dreamreco.howru.customDialog.RecommendationListShowDialog
import com.dreamreco.howru.data.entity.Recommendation
import com.dreamreco.howru.data.entity.Tendency
import com.dreamreco.howru.databinding.FragmentMainBinding
import com.dreamreco.howru.recommendationLogic.NumberForStatusProgressbar
import com.dreamreco.howru.recommendationLogic.RecommendationViewModel
import com.dreamreco.howru.util.CUSTOM_CHART_COLORS
import com.dreamreco.howru.util.sendNotification
import com.dreamreco.howru.util.setUpImageWithConvertCallType
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.abs

@AndroidEntryPoint
class MainFragment : Fragment() {

    // 뒤로 가기 처리(종료)를 위한 콜백 변수
    private lateinit var callback: OnBackPressedCallback

    // 뒤로 가기 연속 클릭 대기 시간
    var mBackWait: Long = 0

    // 요청 권한 리스트
    companion object {
        val BASIC_PERMISSIONS = arrayOf(
            Manifest.permission.READ_CONTACTS,
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

    private val approvalCallLog = MutableLiveData(false)
    var isCallLogEmpty = false

    var easterEggInt = 0
    var spin: Boolean = true

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        Log.d("개선", "가끔 MainFragment 먹통 - 함수가 무거워서??")

        showProgressInRecommendation(true)
        showProgressInTendency(true)
        showStatusProgressbar(true)

        // 허용 체크 후, 기초 정보 구축하기
        checkPermissionsAndStart(BASIC_PERMISSIONS)

        // 추천 정보를 가져오는 코드
        mainViewModel.makeRecommendationInfoEvent.observe(viewLifecycleOwner) { ready ->
            if (ready) {
                // 실행 시 매번 업데이트 되어야 함
                makeRecommendationInfo()
                mainViewModel.makeRecommendationInfoEventDone()
                // CountNumber 를 업데이트 하는 함수
                mainViewModel.syncCountNumbers()
            }
        }
        // 추천 정보를 가져와 viewPager 에 출력하는 코드
        recoViewModel.getRecommendationLiveList().observe(viewLifecycleOwner) {
            if (it == emptyList<Recommendation>()) {
                binding.textWellActivation.visibility = View.VISIBLE
                binding.recommendationViewPager.visibility = View.GONE
                binding.dotsIndicator.visibility = View.INVISIBLE
                showProgressInRecommendation(false)
            } else {
                binding.textWellActivation.visibility = View.GONE
                binding.recommendationViewPager.visibility = View.VISIBLE
                binding.dotsIndicator.visibility = View.VISIBLE
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

            }
            // 유지상태 프로그래스바 활성화 코드
            recoViewModel.makeDataForStatusProgressbar(it)
        }

        // 추천창 전화걸기 작동 코드
        adapter.checkAndCall.observe(viewLifecycleOwner) { phoneNumber ->
            if (phoneNumber != null) {
                checkPermissionsAndCall(phoneNumber)
                adapter.checkAndCallClear()
            }
        }

        // StatusProgressbar 활성화 코드
        recoViewModel.mainStatusProgressbar.observe(viewLifecycleOwner) {
            if (it.allNumber == it.recommendedNumber) {
                // 활성화도가 0% 인 경우,
                activateStatusProgressBarWithZero(it)
            } else {
                activateStatusProgressBar(it)
            }
        }

        // 경향 LiveData 출력 코드
        recoViewModel.getTendencyLive()?.observe(viewLifecycleOwner) { tendency ->
            if (tendency != null) {
                getBindTextView(tendency)
                // 통화기록이 없으면 MainSub 로 넘어가지 않게 하는 코드
                if (tendency.allCallCount.toInt() == 0) {
                    isCallLogEmpty = true
                }
            }
        }

        // Recommendation List 를 보여주는 버튼
        binding.btnShowRecoList.setOnClickListener {
            val dialog = RecommendationListShowDialog()
            dialog.show(childFragmentManager, "RecommendationListShowDialog")
        }

        // PieChart 데이터 생성 코드
        mainViewModel.getGroupNameListDataLive().observe(viewLifecycleOwner) {
            mainViewModel.getGroupChartData(it)
        }
        // PieChart 데이터 출력 코드
        mainViewModel.mainGroupChartData.observe(viewLifecycleOwner) {
            // null 이 아니면 empty list 도 아님
            if (it != null) {
                activateGroupPieChart(it)
                showStatusProgressbar(false)
            } else {
                binding.mpChart.setNoDataText("그룹이 구성되지 않았습니다.")
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

        // 활성화 그래프 터치 시, countNumbers 를 수집하여 Dialog 로 넘겨주고 이동
        binding.activationLayout.setOnClickListener {
            var argsCountNumbers = CountNumbers(0, 0, 0, 0)
            val emptyCountNumbers = CountNumbers(0, 0, 0, 0)
            // 연락처 개수, 그룹 지정수, 알림 지정 수 표현 코드
            mainViewModel.countNumbers.observe(viewLifecycleOwner) { countNumbers ->
                if (countNumbers != null) {
                    argsCountNumbers = countNumbers
                }
            }
            // argsCountNumbers 생성 확인 후 넘어간다.
            if (argsCountNumbers != emptyCountNumbers) {
                val action =
                    MainFragmentDirections.actionMainFragmentToMainActivationDialog(argsCountNumbers)
                it.findNavController().navigate(action)
            } else {
                if (approvalCallLog.value == false) {
                    Toast.makeText(requireContext(), "통화 기록 권한을 허용해주세요.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "연락처, 통화기록 데이터가 없습니다.\n다음에 다시 시도해 주세요.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 통화기록 권한이 허용됬다면 성향을 보여주는 코드
        approvalCallLog.observe(viewLifecycleOwner) { isApproved ->
            if (isApproved) {
                binding.cardViewTendency.visibility = View.VISIBLE
                binding.cardViewVisibility.visibility = View.GONE
            } else {
                binding.cardViewTendency.visibility = View.GONE
                binding.cardViewVisibility.visibility = View.VISIBLE
            }
        }
        checkPermissionsAndConfirmTendency()

        // 성향 터치 시, 통화기록 권한이 허용됬다면(View VISIBLE) 정보 detail 을 보여주는 코드
        binding.tendencyLayout.setOnClickListener {
            // 단, 통화기록이 있을때만 넘어감
            if (!isCallLogEmpty) {
                val action = MainFragmentDirections.actionMainFragmentToMainSubFragment()
                it.findNavController().navigate(action)
            } else {
                Toast.makeText(requireContext(),"통화기록이 없습니다.",Toast.LENGTH_SHORT).show()
            }
        }


        // 그룹 구성 터치 시, 이스터 에그
        binding.groupFormationLayout.setOnClickListener {
            spin = if (spin) {
                binding.mpChart.spin(500, 270f, 91f, Easing.EaseInExpo)
                false
            } else {
                binding.mpChart.spin(500, 90f, 269f, Easing.EaseInExpo)
                true
            }
            easterEggInt++
            when (easterEggInt) {
                10 -> {
                    Toast.makeText(requireContext(), "♥", Toast.LENGTH_LONG).show()
                }
                20 -> {
                    easterEggInt = 0
                    // 알림을 보내거나 취소하거나 업데이트하려면 시스템에서 인스턴스를 요청해야 합니다. app 이 들어가는 이유.
                    val notificationManager = ContextCompat.getSystemService(
                        requireContext(),
                        NotificationManager::class.java
                    ) as NotificationManager
                    // 그리고 NotificationUtil 에서 정의한 알림 함수 사용가능하다.
                    notificationManager.sendNotification(
                        requireContext().getString(R.string.notification_text),
                        requireContext()
                    )
                }
            }
        }


        // backEventCheckLive true 일 때, 백버튼 콜백 생성
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                //뒤로 가기 시 특정 코드 작동
                if (System.currentTimeMillis() - mBackWait >= 2000) {
                    mBackWait = System.currentTimeMillis()
                    Toast.makeText(
                        requireContext(),
                        "뒤로 가기 버튼을 한번 더 누르면 종료됩니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    //액티비티 종료
                    requireActivity().finishAndRemoveTask()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        return binding.root
    }


    private fun call(phoneNumber: String) {
        val uri = Uri.parse("tel:$phoneNumber")
        val intent = Intent(Intent.ACTION_CALL, uri)
        requireContext().startActivity(intent)
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

            // 그래프 아이템 표현여부
            setDrawEntryLabels(false)
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
            legend.isEnabled = true
            val l = binding.mpChart.legend
            l.verticalAlignment = Legend.LegendVerticalAlignment.CENTER
            l.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
            l.orientation = Legend.LegendOrientation.VERTICAL
            l.setDrawInside(false)
            l.textSize = 16f
            l.xEntrySpace = 8f
            l.yEntrySpace = 0f
            l.yOffset = 0f


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

    // 활성화도가 0% 일 때, 작동하는 프로그래스바 코드
    private fun activateStatusProgressBarWithZero(it: NumberForStatusProgressbar) {
        // 알람 설정 상대가 없는 경우,
        if (it.allNumber == 0) {
            binding.textTotalActivation.text = "# 알림 설정된 상대가 없습니다."
        } else {
            binding.textTotalActivation.text = "# 교류가 부족해요."
        }
        binding.textStatusProgressbar.text = "활성화도 0%"
        binding.statusProgressbar.setProgressWithAnimation(0f, 3000)
    }

    // 활성화도가 0% 이 아닐 때, 작동하는 프로그래스바 코드
    private fun activateStatusProgressBar(it: NumberForStatusProgressbar) {
        // 활성화도 : 추천대상인 중, 교류하고 있는 사람 비율(높을수록 교류 양호)
        val rateText =
            ((it.allNumber - it.recommendedNumber).toDouble()) / (it.allNumber.toDouble()) * 100

        // 알람 설정 상대가 없는 경우,
        if (it.allNumber == 0) {
            binding.textTotalActivation.text = "# 알림 설정된 상대가 없습니다."
        }

        // bar 지수 (180을 기준으로 함)
        val barState = rateText / 100 * 180

        binding.textStatusProgressbar.text =
            getString(R.string.recommendation_active_rate, rateText.toInt())
        binding.statusProgressbar.setProgressWithAnimation(barState.toFloat(), 3000)
        Log.i("차트", "barState : $barState")
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
                        R.color.yellow
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
        Log.d("수정", "최초 앱 빌드 시, The application may be doing too much work on its main thread.")
        Log.d("수정", "매번 업데이트 해야 하는 함수를 `가볍게 개선 -> 세부사항 눌렀을 때만 로드 걸리도록")
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

        val callTypeTendency : String
        val callTypeTendencyInReco : String
        val callTypeTendencyInGroup : String

        val callTypeNumberForAll : Int
        val callTypeNumberForGroup : Int
        val callTypeNumberForReco : Int

        val callTypeTendencyConstant =
            tendency.allCallIncoming.toInt() - tendency.allCallOutgoing.toInt()
        val callTypeTendencyInRecoConstant =
            tendency.recommendationCallIncoming.toInt() - tendency.recommendationCallOutgoing.toInt()
        val callTypeTendencyInGroupConstant =
            tendency.groupListCallIncoming.toInt() - tendency.groupListCallOutgoing.toInt()

        callTypeTendency = when (callTypeTendencyConstant) {
            0 -> {
                callTypeNumberForAll = 0
                if (tendency.allCallOutgoing.toInt() == 0) {
                    "'통화기록이 없습니다.'"
                } else {
                    "'발신 수신 횟수가 같아요.'"
                }
            }
            abs(callTypeTendencyConstant) -> {
                callTypeNumberForAll = 1
                if (tendency.allCallOutgoing.toInt() == 0) {
                    "'전화를 받는 편이에요.'"
                } else {
                    val rating = (((tendency.allCallIncoming.toDouble()
                        .minus(tendency.allCallOutgoing.toDouble())) / tendency.allCallOutgoing.toDouble()) * 100).toInt()
                    "'전화를 받는 편이에요.'\n($rating% 더 받아요.)"
                }
            }
            else -> {
                callTypeNumberForAll = 2
                if (tendency.allCallIncoming.toInt() == 0) {
                    "'전화를 거는 편이에요.'"
                } else {
                    val rating = (((tendency.allCallOutgoing.toDouble()
                        .minus(tendency.allCallIncoming.toDouble())) / tendency.allCallIncoming.toDouble()) * 100).toInt()
                    "'전화를 거는 편이에요.'\n($rating% 더 걸어요.)"
                }
            }
        }

        callTypeTendencyInGroup = when (callTypeTendencyInGroupConstant) {
            0 -> {
                callTypeNumberForGroup = 0
                if (tendency.groupListCallOutgoing.toInt() == 0) {
                    "'그룹이 없어요.'"
                } else {
                    "'발신 수신 횟수가 같아요.'"
                }
            }
            abs(callTypeTendencyInGroupConstant) -> {
                callTypeNumberForGroup = 1
                if (tendency.groupListCallOutgoing.toInt() == 0) {
                    "'전화를 받는 편이에요.'"
                } else {
                    val rating = (((tendency.groupListCallIncoming.toDouble()
                        .minus(tendency.groupListCallOutgoing.toDouble())) / tendency.groupListCallOutgoing.toDouble()) * 100).toInt()
                    "'전화를 받는 편이에요.'\n($rating% 더 받아요.)"
                }
            }
            else -> {
                callTypeNumberForGroup = 2
                if (tendency.groupListCallIncoming.toInt() == 0) {
                    "'전화를 거는 편이에요.'"
                } else {
                    val rating = (((tendency.groupListCallOutgoing.toDouble()
                        .minus(tendency.groupListCallIncoming.toDouble())) / tendency.groupListCallIncoming.toDouble()) * 100).toInt()
                    "'전화를 거는 편이에요.'\n($rating% 더 걸어요.)"
                }
            }
        }

        callTypeTendencyInReco = when (callTypeTendencyInRecoConstant) {
            0 -> {
                callTypeNumberForReco = 0
                if (tendency.recommendationCallOutgoing.toInt() == 0) {
                    "'알림 상대가 없어요.'"
                } else {
                    "'발신 수신 횟수가 같아요.'"
                }
            }
            abs(callTypeTendencyInRecoConstant) -> {
                callTypeNumberForReco = 1
                if (tendency.recommendationCallOutgoing.toInt() == 0) {
                    "'전화를 받는 편이에요.'"
                } else {
                    val rating = (((tendency.recommendationCallIncoming.toDouble()
                        .minus(tendency.recommendationCallOutgoing.toDouble())) / tendency.recommendationCallOutgoing.toDouble()) * 100).toInt()
                    "'전화를 받는 편이에요.'\n($rating% 더 받아요.)"
                }
            }
            else -> {
                callTypeNumberForReco = 2
                if (tendency.recommendationCallIncoming.toInt() == 0) {
                    "'전화를 거는 편이에요.'"
                } else {
                    val rating = (((tendency.recommendationCallOutgoing.toDouble()
                        .minus(tendency.recommendationCallIncoming.toDouble())) / tendency.recommendationCallIncoming.toDouble()) * 100).toInt()
                    "'전화를 거는 편이에요.'\n($rating% 더 걸어요.)"
                }
            }
        }

        setUpImageWithConvertCallType(binding.imgForAll, callTypeNumberForAll, requireContext())
        setUpImageWithConvertCallType(binding.imgForGroup, callTypeNumberForGroup, requireContext())
        setUpImageWithConvertCallType(binding.imgForAlarm, callTypeNumberForReco, requireContext())

        binding.textCallTypeTendency.text = callTypeTendency
        binding.textCallTypeTendencyReco.text = callTypeTendencyInReco
        binding.textCallTypeTendencyGroup.text = callTypeTendencyInGroup

        showProgressInTendency(false)
    }

    // 초기 데이터 로드 함수(ContactBase)
    @SuppressLint("Range")
    fun appBuildLoadContact() {
        mainViewModel.appBuildLoadContact(requireActivity())
    }


    // 허용 체크 후, appBuildLoadContact() 시작
    private fun checkPermissionsAndStart(permissions: Array<String>) {
        if (!checkNeedPermissionBoolean(permissions)) {
            // 허용 안되어 있는 경우, 요청
            requestMultiplePermissions.launch(
                permissions
            )
        } else {
            // 허용 되어있는 경우, 통화기록, 통계 가져오기
            appBuildLoadContact()
            approvalCallLog.value = true
        }
    }

    // 허용 체크 후, 전화걸기
    private fun checkPermissionsAndCall(phoneNumber: String) {
        val permission = Manifest.permission.CALL_PHONE
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                permission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // 허용 안되어 있는 경우, 요청
            requestCallPermission.launch(
                permission
            )
        } else {
            // 허용 되어있는 경우, 전화걸기
            call(phoneNumber)
        }
    }

    // 허용 체크 후, 통화 성향 보기
    private fun checkPermissionsAndConfirmTendency() {
        val permission = Manifest.permission.READ_CALL_LOG
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                permission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // 허용 안되어 있는 경우, 요청
            requestCallLogPermission.launch(
                permission
            )
        } else {
            // 허용 되어있는 경우
            approvalCallLog.value = true
        }
    }

    // 허용 여부에 따른 Boolean 반환
    private fun checkNeedPermissionBoolean(permissions: Array<String>): Boolean {
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

    // 초기 허용 요청 코드 및 작동
    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.entries.all {
                it.value
            }
            if (granted) {
                // 허용된, 경우
                Toast.makeText(context, "권한이 허용되었습니다.", Toast.LENGTH_SHORT).show()
                approvalCallLog.value = true
                appBuildLoadContact()
            } else {
                // 허용안된 경우,
                Toast.makeText(context, "거부된 권한이 있습니다.", Toast.LENGTH_SHORT).show()
            }
        }

    // 전화걸기 허용 요청 코드 및 작동
    private val requestCallPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // 허용된, 경우
                Toast.makeText(context, "이제 전화를 할 수 있습니다.", Toast.LENGTH_SHORT).show()
            } else {
                // 허용안된 경우,
                Toast.makeText(context, "전화를 하기 위해,\n전화 권한을 허용해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

    // 통화성향 허용 요청 코드 및 작동
    private val requestCallLogPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // 허용된, 경우
                Toast.makeText(context, "이제 통화 성향을 확인할 수 있습니다.", Toast.LENGTH_SHORT).show()
                approvalCallLog.value = true

            } else {
                // 허용안된 경우,
                Toast.makeText(context, "통화 성향을 확인하려면\n통화 기록 권한을 허용해주세요.", Toast.LENGTH_SHORT)
                    .show()
                approvalCallLog.value = false
            }
        }

}