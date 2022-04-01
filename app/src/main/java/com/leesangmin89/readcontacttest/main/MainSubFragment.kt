package com.leesangmin89.readcontacttest.main


import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.leesangmin89.readcontacttest.R
import com.leesangmin89.readcontacttest.data.entity.ContactInfo
import com.leesangmin89.readcontacttest.data.entity.Tendency
import com.leesangmin89.readcontacttest.databinding.FragmentMainSubBinding
import com.leesangmin89.readcontacttest.recommendationLogic.RecommendationViewModel
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

        Log.i("추가", "성향 데이터 추가")
//        데이터 그래프로 우선 나타내기 ?

        // CountNumber 를 업데이트 하는 함수
        mainViewModel.syncCountNumbers()
        // ContactInfo 를 업데이트 하는 함수
        mainViewModel.contactInfoUpdate(requireActivity())

        // 프로그래스바 노출 코드
        mainViewModel.progressBarEventFinished.observe(
            viewLifecycleOwner
        ) { progressBarFinish ->
            if (progressBarFinish) {
                showMainProgress(false)
                mainViewModel.progressBarEventReset()
            }
        }

        // 활성 통화 횟수 및 마지막 통화 표현 코드
        mainViewModel.getInfoData().observe(viewLifecycleOwner, Observer {
            showMainProgress(true)
            bindTextInContact(it)
        })

        // 경향 text 대입
        recoViewModel.getTendencyLive()?.observe(viewLifecycleOwner) { tendency ->
            if (tendency != null) {
                getBindText(tendency)
            }
        }

        // 연락처 개수, 그룹 지정수, 알림 지정 수 표현 코드
        mainViewModel.countNumbers.observe(viewLifecycleOwner) {
            if (it == null) {
                binding.apply {
                    textContactNumber.text = getString(R.string.contact_phone_numbers, 0)
                    textGroupNumber.text = "그룹 지정 수 : 0명"
                    textRecommendationNumber.text = "전화추천 지정 수 : 0명"
                }
            } else {
                binding.apply {
                    textContactNumber.text = getString(R.string.contact_phone_numbers, it.contactNumbers)
                    textGroupNumber.text = getString(R.string.group_phone_numbers, it.groupNumbers)
                    textRecommendationNumber.text = getString(R.string.recommendation_phone_numbers, it.recoNumbers)
                }
            }
        }

        return binding.root
    }

    // 연락처 통계 bindText 함수
    private fun bindTextInContact(it: ContactInfo?) {
        binding.apply {
            if (it == null) {
                Log.i("삭제할것","앱 다시 빌드시 activatedContact 활성화통화 삭제하고 다른 것으로 대체")
//                textContactActivated.text = getString(R.string.contact_activated, 0)
                textRecentContact.text = getString(R.string.recent_contact, "해당없음")
                mostContactNumber.text = getString(R.string.most_contact_name, "해당없음")
                mostContactDuration.text = getString(R.string.most_contact_duration, 0, 0)
                mainViewModel.progressBarEventDone()
            } else {

                Log.i("삭제할것","앱 다시 빌드시 activatedContact 활성화통화 삭제하고 다른 것으로 대체")
//                textContactActivated.text =
//                getString(R.string.contact_activated, it.activatedContact)
                textRecentContact.text =
                    getString(R.string.recent_contact, it.mostRecentContact)
                mostContactNumber.text =
                    getString(R.string.most_contact_name, it.mostContactName)

                val minutes = it.mostContactTimes!!.toLong() / 60
                val seconds = it.mostContactTimes.toLong() % 60
                mostContactDuration.text =
                    getString(R.string.most_contact_duration, minutes, seconds)
                mainViewModel.progressBarEventDone()
            }
        }
    }

    // layout 텍스트 대입 함수
    private fun getBindText(tendency: Tendency) {
        with(binding) {
            textAllInCount.text = getString(R.string.all_in_count, tendency.allCallIncoming)
            textAllOutCount.text = getString(R.string.all_out_count, tendency.allCallOutgoing)
            textGroupInCount.text =
                getString(R.string.group_in_count, tendency.groupListCallIncoming)
            textGroupOutCount.text =
                getString(R.string.group_out_count, tendency.groupListCallOutgoing)
            textAlarmInCount.text =
                getString(R.string.reco_in_count, tendency.recommendationCallIncoming)
            textAlarmOutCount.text =
                getString(R.string.reco_out_count, tendency.recommendationCallOutgoing)
            allCallTime.text = getString(R.string.all_duration, tendency.allCallDuration.toLong()/60)
            groupCallTime.text = getString(R.string.group_duration, tendency.groupListCallDuration.toLong()/60)
            alarmCallTime.text = getString(R.string.reco_duration, tendency.recommendationCallDuration.toLong()/60)
        }
    }

    private fun showMainProgress(show: Boolean) {
        binding.progressBarMain.visibility = if (show) View.VISIBLE else View.GONE
    }
}
