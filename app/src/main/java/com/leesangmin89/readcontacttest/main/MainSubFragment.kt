package com.leesangmin89.readcontacttest.main


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
import com.leesangmin89.readcontacttest.R
import com.leesangmin89.readcontacttest.data.entity.Tendency
import com.leesangmin89.readcontacttest.databinding.FragmentMainSubBinding
import com.leesangmin89.readcontacttest.recommendationLogic.RecommendationViewModel
import com.leesangmin89.readcontacttest.util.CUSTOM_CHART_COLORS
import com.leesangmin89.readcontacttest.util.CombinedChartData
import dagger.hilt.android.AndroidEntryPoint


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
        Log.i("차트", "combinedChartList : $combinedChartList")
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
            entries.add(PieEntry(tendency.allCallMissed.toFloat(), "부재중"))
            entries.add(PieEntry(tendency.allCallIncoming.toFloat(), "수신"))
            entries.add(PieEntry(tendency.allCallOutgoing.toFloat(), "발신"))
            // 통화수 많은 순으로 정렬해서 대입한다.
            entries.sortByDescending { it.value }

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

        Log.i("차트", "applyList : $applyList")

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
        with(binding) {

//            allCallTime.text =
//                getString(R.string.all_duration, tendency.allCallDuration.toLong() / 60)
//            groupCallTime.text =
//                getString(R.string.group_duration, tendency.groupListCallDuration.toLong() / 60)
//            alarmCallTime.text =
//                getString(R.string.reco_duration, tendency.recommendationCallDuration.toLong() / 60)

            textTotalIn.text = tendency.allCallIncoming
            textTotalOut.text = tendency.allCallOutgoing
            textGroupIn.text = tendency.groupListCallIncoming
            textGroupOut.text = tendency.groupListCallOutgoing
            textRecoIn.text = tendency.recommendationCallIncoming
            textRecoOut.text = tendency.recommendationCallOutgoing

            var callTypeTendencyInGroup = "그룹 발수신 성향"
            var callTypeTendencyInReco = "알람 발수신 성향"
            var callTypeTendencyOverView = "총 성향"
            // 발신이 많으면 true
            var groupTendency: Boolean? = null
            var recoTendency: Boolean? = null

            callTypeTendencyInGroup =
                if (tendency.groupListCallIncoming.toInt() >= tendency.groupListCallOutgoing.toInt()) {
                    if (tendency.groupListCallOutgoing.toInt() == 0) {
                        groupTendency = false
                        "그룹 등록 상대로 '전화를 받는 편이에요.'"
                    } else {
                        groupTendency = false
                        val rating = (((tendency.groupListCallIncoming.toDouble()
                            .minus(tendency.groupListCallOutgoing.toDouble())) / tendency.groupListCallOutgoing.toDouble()) * 100).toInt()
                        "그룹 등록 상대로 '전화를 받는 편이에요.'\n(수신이 $rating% 더 많아요.)"
                    }
                } else {
                    if (tendency.groupListCallIncoming.toInt() == 0) {
                        groupTendency = true
                        "그룹 등록 상대로 '전화를 거는 편이에요.'"
                    } else {
                        groupTendency = true
                        val rating = (((tendency.groupListCallOutgoing.toDouble()
                            .minus(tendency.groupListCallIncoming.toDouble())) / tendency.groupListCallIncoming.toDouble()) * 100).toInt()
                        "그룹 등록 상대로 '전화를 거는 편이에요.'\n(발신이 $rating% 더 많아요.)"
                    }
                }

            callTypeTendencyInReco =
                if (tendency.recommendationCallIncoming.toInt() >= tendency.recommendationCallOutgoing.toInt()) {
                    if (tendency.recommendationCallOutgoing.toInt() == 0) {
                        recoTendency = false
                        "알림 등록 상대로 '전화를 받는 편이에요.'"
                    } else {
                        recoTendency = false
                        val rating = (((tendency.recommendationCallIncoming.toDouble()
                            .minus(tendency.recommendationCallOutgoing.toDouble())) / tendency.recommendationCallOutgoing.toDouble()) * 100).toInt()
                        "알림 등록 상대로 '전화를 받는 편이에요.'\n(수신이 $rating% 더 많아요.)"
                    }
                } else {
                    if (tendency.recommendationCallIncoming.toInt() == 0) {
                        recoTendency = true
                        "알림 등록 상대로 '전화를 거는 편이에요.'"
                    } else {
                        recoTendency = true
                        val rating = (((tendency.recommendationCallOutgoing.toDouble()
                            .minus(tendency.recommendationCallIncoming.toDouble())) / tendency.recommendationCallIncoming.toDouble()) * 100).toInt()
                        "알람 등록 상대로 '전화를 거는 편이에요.'\n(발신이 $rating% 더 많아요.)"
                    }
                }

            if (groupTendency && recoTendency) {
                // 둘다 발신이 많은 경우,
                callTypeTendencyOverView = "먼저 적극적으로 전화하는 스타일이시군요."
            }
            if (groupTendency && !recoTendency) {
                // 그룹만 발신이 많은 경우,
                callTypeTendencyOverView = "알람 설정한 분들에게도 적극적으로 연락해보세요."
            }
            if (!groupTendency && recoTendency) {
                // 추천만 발신이 많은 경우,
                callTypeTendencyOverView = "중요한 사람들에게 진심인 당신"
            }
            if (!groupTendency && !recoTendency) {
                // 둘다 수신이 많은 경우,
                callTypeTendencyOverView = "좀 더 적극적으로 연락해보는건 어떨까요?"
            }

            textCallTypeReview.text = callTypeTendencyInGroup
            textCallTypeReview2.text = callTypeTendencyInReco
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
