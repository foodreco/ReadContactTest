package com.dreamreco.howru.customDialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import com.dreamreco.howru.MyApplication
import com.dreamreco.howru.data.entity.ContactBase
import com.dreamreco.howru.databinding.UpdateDialogBinding
import com.dreamreco.howru.group.GroupViewModel
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONArray

@AndroidEntryPoint
class UpdateDialog : DialogFragment() {
    private val groupViewModel: GroupViewModel by viewModels()
    private val binding by lazy { UpdateDialogBinding.inflate(layoutInflater) }

    private val spinnerGroupNameList = mutableListOf<String>()
    private var selectedGroupName = ""

    // 추천 스위치 작동 관련 LiveData
    private val switchLiveData = MutableLiveData<Boolean>()

    // Dialog 배경 투명하게 하는 코드??
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val args = arguments?.getParcelable<ContactBase>("contactBase")

        binding.contactNameUpdate.text = args?.name
        binding.contactNumberUpdate.text = args?.number
        binding.contactGroupUpdate.setText(args?.group)

        // updateDialog 의 알람 버튼 상태 확인 코드
        groupViewModel.checkAlarmState(args?.number!!)


        // updateDialog 의 알람 버튼 상태 코드 2
        groupViewModel.groupListForSwitch.observe(viewLifecycleOwner) {
            binding.swtichAlarm.isChecked = it
        }

        //그룹명 리스트 형태로 출력하는 함수
        groupViewModel.getOnlyGroupName()

        // 그룹명을 리스트로 받는게 끝나면, 스피너를 동작시키는 코드
        groupViewModel.getOnlyGroupNameDoneEvent.observe(viewLifecycleOwner) {
            if (it) {
                spinnerAddListCall()
                //스피너
                val spinnerAdapter =
                    ArrayAdapter<String>(
                        requireContext(),
                        android.R.layout.simple_list_item_1,
                        spinnerGroupNameList
                    )
                binding.spinnerGroup.adapter = spinnerAdapter


                // 전달된 데이터 선택 시, 해당값 출력하기
                binding.spinnerGroup.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener { //object 선언 언제 하는거야????
                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            // 스피너에서 선택한 그룹명
                            selectedGroupName =
                                spinnerGroupNameList[position] // 스피너의 어떤 데이터를 선택했느냐를 position 으로 알수가 있다. 그래서 get(position)으로 데이터 찾는거임

                            // 스피너 그룹명 선택에 따른 경우
                            when (selectedGroupName) {

                                // 0. 초기상태, 아무 변화 없음
                                "" -> {
                                    binding.contactGroupUpdate.isEnabled = false
                                    binding.contactGroupUpdate.setText(args.group)
                                }

                                // 1. "그룹명" 선택 시, 아무 변화 없음
                                "그룹명 선택" -> {
                                    binding.contactGroupUpdate.isEnabled = false
                                    binding.contactGroupUpdate.setText(args.group)
                                }

                                // 2. 직접입력 선택 시,
                                "직접입력" -> {
                                    // 알람 스위치 사용가능하게 함
                                    switchLiveData.value = true
                                    // editText 사용가능하게 함
                                    binding.contactGroupUpdate.isEnabled = true
                                    // 포커스 지정
                                    binding.contactGroupUpdate.requestFocus()
                                    // 포커스 끝으로 보냄
                                    binding.contactGroupUpdate.setSelection(binding.contactGroupUpdate.text.length)
                                    // 키보드 올리기
                                    val mInputMethodManager =
                                        context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                    mInputMethodManager.showSoftInput(
                                        binding.contactGroupUpdate,
                                        InputMethodManager.SHOW_IMPLICIT
                                    )
                                }
                                // 3. 그 외, 추가된 실제 group 선택 시,
                                else -> {
                                    // 알람 스위치 사용가능하게 함
                                    switchLiveData.value = true
                                    binding.contactGroupUpdate.isEnabled = false
                                    binding.contactGroupUpdate.setText(selectedGroupName)
                                }
                            }
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {
                        }
                    }
            }
        }

        // 배경 터치 시, 키보드 내림
        binding.updateFragmentBackground.setOnClickListener {
            val mInputMethodManager =
                requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            mInputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
        }

        binding.btnUpdate.setOnClickListener {
            updateData()
        }


        // updateData() 작업이 완료되면, dialog 종료
        groupViewModel.updateDataEvent.observe(viewLifecycleOwner) { doneNumber ->
            if (doneNumber == 3) {
                groupViewModel.updateDialogDoneFinished()
                dismiss()
            }
        }

        // 넘어온 group 여부에 따라, 알람버튼 작동 코드
        when (args.group) {
            "" -> switchLiveData.value = false
            else -> switchLiveData.value = true
        }
        switchLiveData.observe(viewLifecycleOwner) {
            binding.swtichAlarm.isEnabled = it
        }

        return binding.root
    }

    // SharedPreferences 로부터 groupList 를 불러와서 spinnerGroupNameList 에 추가하는 함수
    private fun spinnerAddListCall() {
        val lastList = MyApplication.prefs.getString("group", "None")
        val arrJson = JSONArray(lastList)
        for (i in 0 until arrJson.length()) {
            spinnerGroupNameList.add(arrJson.optString(i))
        }
    }

    // 그룹을 추가하고(GroupList), 연락처 정보를 변경하는 함수
    private fun updateData() {
        val args = arguments?.getParcelable<ContactBase>("contactBase")

        val contactName = binding.contactNameUpdate.text.toString()
        val contactNumber = binding.contactNumberUpdate.text.toString()
        val contactGroup = binding.contactGroupUpdate.text.toString()
        val contactRecommendation = binding.swtichAlarm.isChecked

        val updateList = ContactBase(
            contactName,
            contactNumber,
            contactGroup,
            args?.image,
            args!!.id
        )
        groupViewModel.updateData(updateList, args, contactGroup, contactRecommendation)
    }
}