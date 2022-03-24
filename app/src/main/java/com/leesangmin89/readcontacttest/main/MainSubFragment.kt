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

        // 연락처 갯수를 업데이트 하는 함수
        mainViewModel.countContactNumbers(requireActivity())
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
        mainViewModel.infoData.observe(viewLifecycleOwner, Observer {
            showMainProgress(true)
            binding.apply {
                if (it == null) {
//                    textContactNumber.text = getString(R.string.contact_number, 0)
                    textContactActivated.text = getString(R.string.contact_activated, 0)
                    textRecentContact.text = getString(R.string.recent_contact, "해당없음")
                    mostContactNumber.text = getString(R.string.most_contact_name, "해당없음")
                    mostContactDuration.text = getString(R.string.most_contact_duration, 0, 0)
                    mainViewModel.progressBarEventDone()
                } else {
//                    textContactNumber.text = getString(R.string.contact_number, it.contactNumber)
                    textContactActivated.text =
                        getString(R.string.contact_activated, it.activatedContact)
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
        })

        // 경향 text 대입
        recoViewModel.getTendencyLive()?.observe(viewLifecycleOwner) { tendency ->
            if (tendency != null) {
                getBindText(tendency)
            }
        }

        // 연락처 개수 표현 코드
        mainViewModel.contactNumbers.observe(viewLifecycleOwner) {
            if (it == null) {
                binding.textContactNumber.text = getString(R.string.contact_phone_numbers, 0)
            } else {
                binding.textContactNumber.text = getString(R.string.contact_phone_numbers, it)
            }
        }

        return binding.root
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

    fun showMainProgress(show: Boolean) {
        binding.progressBarMain.visibility = if (show) View.VISIBLE else View.GONE
    }
}
