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
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.leesangmin89.readcontacttest.data.entity.Recommendation
import com.leesangmin89.readcontacttest.data.entity.Tendency
import com.leesangmin89.readcontacttest.databinding.FragmentMainBinding
import com.leesangmin89.readcontacttest.recommendationLogic.RecommendationViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainFragment : Fragment() {

    private val binding by lazy { FragmentMainBinding.inflate(layoutInflater) }
    private val recoViewModel: RecommendationViewModel by viewModels()
    private val mainViewModel: MainViewModel by viewModels()
    private val adapter: CallRecommendAdapter by lazy { CallRecommendAdapter(requireContext()) }

    // 권한 허용 리스트
    private val permissions = arrayOf(
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.CALL_PHONE,
        Manifest.permission.READ_CALL_LOG
    )
    // 권한 허용 코드
    private val CONTACT_AND_CALL_PERMISSION_CODE = 1

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        showProgress(true)

        // 기초 정보 구축하기
        checkAndStart()

        mainViewModel.makeRecommendationInfoEvent.observe(viewLifecycleOwner, { ready ->
            if (ready) {
                // 실행 시 매번 업데이트 되어야 함
                makeRecommendationInfo()
                mainViewModel.makeRecommendationInfoEventDone()
            }
        })

        // call 추천
        // ViewPager2 적용
        binding.recommendationViewPager.adapter = adapter
        binding.recommendationViewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        recoViewModel.recommendationLiveList.observe(viewLifecycleOwner, {
            if (it == emptyList<Recommendation>()) {
                Log.d("보완", "testList null 일 때, '관계 유지가 잘 되고 있습니다.' view 띄우기")
                Toast.makeText(requireContext(), "관계 유지가 잘 되고 있습니다. \n 알림친구가 없다면 설정하세요.", Toast.LENGTH_SHORT).show()
            } else {
                adapter.submitList(it)
            }
        })

        // 경향
        recoViewModel.tendencyLive?.observe(viewLifecycleOwner, { tendency ->
            if (tendency != null) {
                getBindTextView(tendency)
            }
        })

        binding.btnToSub.setOnClickListener {
            val action = MainFragmentDirections.actionMainFragmentToMainSubFragment()
            it.findNavController().navigate(action)
        }

        return binding.root
    }

    // CallLogCalls 데이터를 순회하며, Recommendation 정보를 가져오는 함수
    @SuppressLint("Range")
    fun makeRecommendationInfo() {
        Log.i("확인","makeRecommendationInfo 시작")
        showProgress(true)
        Log.d("수정", "최초 앱 빌드 시, The application may be doing too much work on its main thread.")
        // contactInfo 전화 통계 데이터 갱신
        // 실행 시 매번 업데이트 되어야 함
        recoViewModel.arrangeRecommendation()
        Log.i("확인","makeRecommendationInfo 종료")
    }

    private fun showProgress(show: Boolean) {
        binding.progressBarMainProto.visibility = if (show) View.VISIBLE else View.GONE
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

        callTypeTendency = if (tendency.allCallIncoming >= tendency.allCallOutgoing) {
            "일반적으로 거는 전화가 많아요."
        } else {
            "일반적으로 받는 전화가 많아요."
        }
        callTypeTendencyInReco =
            if (tendency.recommendationCallIncoming >= tendency.recommendationCallOutgoing) {
                "가까운 사람들에게 주로 전화를 먼저 거는 편이에요."
            } else {
                "가까운 사람들에게 주로 전화를 받는 편이에요."
            }
        callDurationTendency = "평균 통화시간은 \n" +
                "전체 전화 ${callDurationAvg / 60} 분 \n" +
                "그룹 전화 ${callDurationAvgGroup / 60} 분 \n" +
                "추천 전화 ${callDurationAvgReco / 60} 분 입니다."

        binding.textCallTypeTendency.text = callTypeTendency
        binding.textCallTypeTendencyReco.text = callTypeTendencyInReco
        binding.textCallDuration.text = callDurationTendency

        showProgress(false)
    }

    // 초기 데이터 로드 함수(ContactBase)
    @SuppressLint("Range")
    fun appBuildLoadContact() {
        mainViewModel.appBuildLoadContact(requireActivity())
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun checkAndStart() {
        // 권한 허용 여부 확인
        if (checkNeedPermission()) {
            // 허용 시, 통화기록, 통계 가져오기
            appBuildLoadContact()
        } else {
            requestContactPermission()
        }
    }

    private fun checkNeedPermission(): Boolean {
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

    private fun requestContactPermission() {
        // READ_CONTACT 허용 요청 함수
        ActivityCompat.requestPermissions(
            requireActivity(),
            permissions,
            CONTACT_AND_CALL_PERMISSION_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Log.d("수정", "onRequestPermissionsResult deprecated")
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CONTACT_AND_CALL_PERMISSION_CODE) {
            var check = true
            for (grant in grantResults) {
                if (grant != PackageManager.PERMISSION_GRANTED) {
                    check = false
                    break
                }
            }
            if (check) {
                Toast.makeText(context, "권한 지금 허용 됨", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "허용 거부되었습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

}