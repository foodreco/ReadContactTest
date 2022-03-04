package com.leesangmin89.readcontacttest.main


import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.leesangmin89.readcontacttest.R
import com.leesangmin89.readcontacttest.databinding.FragmentMainSubBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainSubFragment : Fragment() {
    private val binding by lazy { FragmentMainSubBinding.inflate(layoutInflater) }
    private val mainViewModel: MainViewModel by viewModels()
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // 연락처 갯수를 업데이트 하는 함수
        mainViewModel.countContactNumbers(requireActivity())
        // ContactInfo 를 업데이트 하는 함수
        mainViewModel.contactInfoUpdate(requireActivity())

        // 프로그래스바 노출 코드
        mainViewModel.progressBarEventFinished.observe(viewLifecycleOwner,
            { progressBarFinish ->
                if (progressBarFinish) {
                    showProgress(false)
                    mainViewModel.progressBarEventReset()
                }
            })

        // 활성 통화 횟수 및 마지막 통화 표현 코드
        mainViewModel.infoData.observe(viewLifecycleOwner, Observer {
            showProgress(true)
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

        // 연락처 개수 표현 코드
        mainViewModel.contactNumbers.observe(viewLifecycleOwner, {
            if (it == null) {
                binding.textContactNumber.text = getString(R.string.contact_phone_numbers, 0)
            } else {
                binding.textContactNumber.text = getString(R.string.contact_phone_numbers, it)
            }
        })

        binding.btnToMain.setOnClickListener {
            val action = MainSubFragmentDirections.actionMainSubFragmentToMainFragment()
            findNavController().navigate(action)
        }
        return binding.root
    }

    fun showProgress(show: Boolean) {
        binding.progressBarMain.visibility = if (show) View.VISIBLE else View.GONE
    }
}
