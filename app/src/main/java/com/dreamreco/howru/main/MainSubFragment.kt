package com.dreamreco.howru.main


import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.mikephil.charting.charts.CombinedChart.DrawOrder
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis.XAxisPosition
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.dreamreco.howru.R
import com.dreamreco.howru.data.entity.Tendency
import com.dreamreco.howru.databinding.FragmentMainSubBinding
import com.dreamreco.howru.recommendationLogic.RecommendationViewModel
import com.dreamreco.howru.util.CombinedChartData
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.abs
import kotlin.math.round

@AndroidEntryPoint
class MainSubFragment : Fragment() {

    private val binding by lazy { FragmentMainSubBinding.inflate(layoutInflater) }
    private val mainViewModel: MainViewModel by viewModels()
    private val recoViewModel: RecommendationViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        Log.i("추가", "총, 그룹, 추천 통화시간 넣자 / 막대 그래프로?")

        showProgressStatusProgress(true)
//        showCallDurationProgress(true)
        showCombinedChartLayoutProgress(true)

        // 1. 프로그래스 상세 layout 관련 코드
        recoViewModel.getTendencyLive()?.observe(viewLifecycleOwner) { tendency ->
            if (tendency != null) {
                // 그룹별 발수신 프로그래스바 활성화
                activateProgressbar(tendency)
                // 전체 발수신 파이차트 활성화
                activateCallPieChart(tendency)
            }
        }

        // 2. 월별 통화량 CombinedChart 관련 코드
        // combinedChartList 를 생성하는 함수 데이터 (뷰모델인데 넘어온다?? activity 때문?)
        val combinedChartList = mainViewModel.contactInfoUpdate(requireActivity())
        // 콤바인 차트 (월별 통화횟수 및 통화량)
        activateCombinedChart(combinedChartList)

        // 3. 그룹별 통화시간 관련 코드

        return binding.root
    }

    private fun activateCallPieChart(tendency: Tendency) {
        // MP 차트 관련 코드
        with(binding.callPieChart) {
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
            setEntryLabelTextSize(12f)
            setEntryLabelTypeface(Typeface.DEFAULT_BOLD)

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
            val l = binding.callPieChart.legend
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
            setCenterTextSize(16f)
            setCenterTextTypeface(Typeface.DEFAULT_BOLD)

            // 그래프 애니메이션 (https://superkts.com/jquery/@easingEffects)
            // ★ 이게 있어야 실시간으로 업데이트 됨
            animateY(1000, com.github.mikephil.charting.animation.Easing.EaseInCubic)

            // data set - 데이터 넣어주기
            // 인수가 많은 집합부터 불러오기(사람수 많은 그룹부터 불러오기)
            // 데이터 넣기, 조건부로 null 일 때 넣을 리스트 따로 만들기
            val entries = mutableListOf<PieEntry>()
            entries.add(PieEntry(tendency.allCallIncoming.toFloat(), "수신"))
            entries.add(PieEntry(tendency.allCallOutgoing.toFloat(), "발신"))
            entries.add(PieEntry(tendency.allCallMissed.toFloat(), "부재중"))

//            // 통화수 많은 순으로 정렬해서 대입한다.
//            entries.sortByDescending { it.value }

            // 데이터 관련 옵션 정하기
            val dataSet = PieDataSet(entries, "")
            with(dataSet) {
                // 그래프 사이 간격
                sliceSpace = 1f
                colors = arrayListOf<Int>(
                    Color.rgb(0, 205, 62), Color.rgb(0, 144, 205), Color.rgb(239, 13, 13))
            }

            // 차트 제목 텍스트?
            val pieData = PieData(dataSet)
            with(pieData) {
                // 그래프 value(수치) 크기
                setValueTextSize(12f)
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

    private fun activateCombinedChart(applyList: List<CombinedChartData>) {

        val chart = binding.combinedChart

        chart.description.isEnabled = false
        chart.setBackgroundColor(Color.WHITE)
        chart.setDrawGridBackground(false)
        chart.setDrawBarShadow(false)
        chart.isHighlightFullBarEnabled = false

        // 그림 덮어쓰는 순서에 영향 미침
        // draw bars behind lines
        chart.drawOrder = arrayOf(
            DrawOrder.BAR, DrawOrder.LINE
        )

        val l = chart.legend
        l.isWordWrapEnabled = true
        l.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        l.orientation = Legend.LegendOrientation.HORIZONTAL
        l.setDrawInside(false)


        val rightAxis = chart.axisRight
        rightAxis.setDrawGridLines(false)
        rightAxis.axisMinimum = 0f // this replaces setStartAtZero(true)


        val leftAxis = chart.axisLeft
        leftAxis.setDrawGridLines(false)
        leftAxis.axisMinimum = 0f // this replaces setStartAtZero(true)

        var months = mutableListOf<String>()
        for (num in applyList) {
            val monthToString =
                num.month.toString().substring(0, 2) + "/" + num.month.toString().substring(2)
            months.add(monthToString)
        }

        val xAxis = chart.xAxis
        xAxis.position = XAxisPosition.BOTTOM
        xAxis.axisMinimum = 0f
        xAxis.granularity = 1f
        xAxis.valueFormatter = IndexAxisValueFormatter(months)

        val data = CombinedData()

        data.setData(generateBarData(applyList))
        data.setData(generateLineData(applyList))
        data.setValueTypeface(Typeface.DEFAULT_BOLD)

        xAxis.axisMaximum = data.xMax + 0.5f
        xAxis.axisMinimum = data.xMin - 0.5f

        chart.data = data
        showCombinedChartLayoutProgress(false)

        with(binding) {

            var reviewText1 = "통화 횟수와 시간 트렌드는 어떤가요?"
            var reviewText2 = "사용자님의 인간관계와 비교해보세요."

            textComReview.text = reviewText1
            textComReview2.text = reviewText2
        }

    }


    // layout 텍스트 대입 함수
    private fun activateProgressbar(tendency: Tendency) {

        var callDurationAvg : Double = 0.0
        var callDurationAvgGroup : Double = 0.0
        var callDurationAvgReco : Double = 0.0

        if (tendency.allCallCount != "0") {
            callDurationAvg = tendency.allCallDuration.toDouble() / tendency.allCallCount.toDouble()
        }

        if (tendency.groupListCallCount != "0") {
            callDurationAvgGroup =
                tendency.groupListCallDuration.toDouble() / tendency.groupListCallCount.toDouble()
        }

        if (tendency.recommendationCallCount != "0") {
            callDurationAvgReco =
                tendency.recommendationCallDuration.toDouble() / tendency.recommendationCallCount.toDouble()
        }

        val avgDuration =
            "평균 통화시간 \n전체 ${round((callDurationAvg / 60)*10)/10 } 분  그룹 ${round((callDurationAvgGroup / 60)*10)/10} 분  알림 ${round((callDurationAvgReco / 60)*10)/10} 분'"

        var callTypeTendencyOverView = "발수신 성향은...??"

        val callTypeTendencyInRecoConstant =
            tendency.recommendationCallIncoming.toInt() - tendency.recommendationCallOutgoing.toInt()
        val callTypeTendencyInGroupConstant =
            tendency.groupListCallIncoming.toInt() - tendency.groupListCallOutgoing.toInt()

        val durationTendencyOverView : String = when (callDurationAvgGroup - callDurationAvgReco) {
            0.0 -> {
                if (callDurationAvgGroup != 0.0) {
                    "그룹과 알림 상대와의 평균 통화시간이 같아요."
                } else {
                 "그룹, 알림 상대를 설정해주세요."
                }}
            abs(callDurationAvgGroup - callDurationAvgReco) -> {
                "그룹 평균 통화시간이 알림 상대보다 길어요."
            }
            else -> {"알림 상대 평균 통화시간이 그룹보다 길어요."}
        }

        // 발신이 많으면 true
        val recoTendency: Boolean? = when (callTypeTendencyInRecoConstant) {
            0 -> {
                null
            }
            abs(callTypeTendencyInRecoConstant) -> {
                false
            }
            else -> {
                true
            }
        }

        val groupTendency: Boolean? = when (callTypeTendencyInGroupConstant) {
            0 -> {
                null
            }
            abs(callTypeTendencyInGroupConstant) -> {
                false
            }
            else -> {
                true
            }
        }


        if (groupTendency == true && recoTendency == true) {
            // 둘다 발신이 많은 경우,
            callTypeTendencyOverView = "먼저 적극적으로 전화하는 스타일이시군요."
        }
        if (groupTendency == true && recoTendency == false) {
            // 그룹만 발신이 많은 경우,
            callTypeTendencyOverView = "알림 설정한 분들에게도 적극적으로 연락해보세요."
        }
        if (groupTendency == false && recoTendency == true) {
            // 추천만 발신이 많은 경우,
            callTypeTendencyOverView = "중요한 사람들에게 진심인 당신"
        }
        if (groupTendency == false && recoTendency == false) {
            // 둘다 수신이 많은 경우,
            callTypeTendencyOverView = "좀 더 적극적으로 연락해보는건 어떨까요?"
        }

        if (groupTendency == null && recoTendency == null) {
            callTypeTendencyOverView = ""
        }

        if (groupTendency == null && recoTendency != null) {
            callTypeTendencyOverView = "놀랍게도 그룹 발수신 횟수가 같습니다."
        }

        if (groupTendency != null && recoTendency == null) {
            callTypeTendencyOverView = if (tendency.recommendationCallIncoming.toInt() != 0) {
                "놀랍게도 그룹 발수신 횟수가 같습니다."
            } else {
                "알림 상대를 설정해주세요."
            }
        }

        with(binding) {

            textTotalIn.text = tendency.allCallIncoming
            textTotalOut.text = tendency.allCallOutgoing
            textGroupIn.text = tendency.groupListCallIncoming
            textGroupOut.text = tendency.groupListCallOutgoing
            textRecoIn.text = tendency.recommendationCallIncoming
            textRecoOut.text = tendency.recommendationCallOutgoing

            textCallTypeReview.text = avgDuration
            textCallTypeReview2.text = durationTendencyOverView
            textCallTypeReview3.text = callTypeTendencyOverView

            // 발수신 프로그래스바 수치 대입 코드
            rcProgressbarTotal.max =
                tendency.allCallIncoming.toFloat() + tendency.allCallOutgoing.toFloat()
            rcProgressbarTotal.progress = tendency.allCallIncoming.toFloat()
            rcProgressbarGroup.max =
                tendency.groupListCallIncoming.toFloat() + tendency.groupListCallOutgoing.toFloat()
            rcProgressbarGroup.progress = tendency.groupListCallIncoming.toFloat()
            rcProgressbarReco.max =
                tendency.recommendationCallIncoming.toFloat() + tendency.recommendationCallOutgoing.toFloat()
            rcProgressbarReco.progress = tendency.recommendationCallIncoming.toFloat()

            showProgressStatusProgress(false)
        }
    }

    // Combined Chart 중 바 차트
    private fun generateBarData(applyList: List<CombinedChartData>): BarData? {
        val entries1: ArrayList<BarEntry> = ArrayList()
        for (index in 1 until applyList.lastIndex + 1) {
            entries1.add(BarEntry(index + 0f, applyList[index - 1].duration / 60.toFloat()))
        }
        val set1 = BarDataSet(entries1, "통화시간(분)")
        set1.color = Color.rgb(60, 220, 78)
        set1.valueTextColor = Color.rgb(60, 220, 78)
        set1.valueTextSize = 10f
        set1.axisDependency = YAxis.AxisDependency.LEFT

        val barWidth = 0.5f // x2 dataset

        val d = BarData(set1)
        d.barWidth = barWidth

        return d
    }

    // Combined Chart 중 라인 차트
    private fun generateLineData(applyList: List<CombinedChartData>): LineData? {
        val d = LineData()
        val entries: ArrayList<Entry> = arrayListOf()
        for (index in 1 until applyList.lastIndex + 1) entries.add(
            Entry(index + 0f, applyList[index - 1].times.toFloat())
        )
        val set = LineDataSet(entries, "통화횟수")
        set.color = Color.rgb(74, 101, 114)
        set.lineWidth = 2.5f
        set.setCircleColor(Color.rgb(74, 101, 114))
        set.circleRadius = 5f
//        set.fillColor = Color.rgb(240, 238, 70)
        set.fillColor = Color.rgb(74, 101, 114)
        set.mode = LineDataSet.Mode.LINEAR
        set.setDrawValues(true)
        set.valueTextSize = 10f
        set.valueTextColor = Color.rgb(74, 101, 114)
        set.axisDependency = YAxis.AxisDependency.RIGHT
        d.addDataSet(set)
        return d
    }

    private fun showProgressStatusProgress(show: Boolean) {
        binding.progressbarProgressStatus.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showCombinedChartLayoutProgress(show: Boolean) {
        binding.progressbarForCombinedChartLayout.visibility = if (show) View.VISIBLE else View.GONE
    }

//    private fun showCallDurationProgress(show: Boolean) {
//        binding.progressbarForCallDuration.visibility = if (show) View.VISIBLE else View.GONE
//    }
}
