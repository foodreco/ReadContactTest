package com.leesangmin89.readcontacttest.update

import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.datastore.dataStore
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.leesangmin89.readcontacttest.MyApplication
import com.leesangmin89.readcontacttest.data.entity.ContactBase
import com.leesangmin89.readcontacttest.R
import com.leesangmin89.readcontacttest.data.entity.GroupList
import com.leesangmin89.readcontacttest.databinding.FragmentUpdateBinding
import com.leesangmin89.readcontacttest.group.GroupViewModel
import com.leesangmin89.readcontacttest.list.ListViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.parcelize.Parcelize
import org.json.JSONArray
import android.widget.EditText

//@AndroidEntryPoint
//class UpdateFragment : Fragment() {
////    private val args by navArgs<UpdateFragmentArgs>()
//    private val listViewModel: ListViewModel by viewModels()
//    private val groupViewModel: GroupViewModel by viewModels()
//    private val binding by lazy { FragmentUpdateBinding.inflate(layoutInflater) }
//
//    //private val spinnerGroupNameList = mutableListOf<String>("그룹명", "직접입력")
//    private val spinnerGroupNameList = mutableListOf<String>()
//    private var selectedGroupName = ""
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//
//        Log.d("수정", "UpdateFragment -> custom Dialog로 전환?? 그룹편집 기능만 살리기?")
//
//        binding.contactNameUpdate.setText(args.currentItem.name)
//        binding.contactNumberUpdate.setText(args.currentItem.number)
//        binding.contactGroupUpdate.setText(args.currentItem.group)
//
//        //그룹명 리스트 형태로 출력하는 함수
//        groupViewModel.getOnlyGroupName()
//
//        // 그룹명을 리스트로 받는게 끝나면, 스피너를 동작시키는 코드
//        groupViewModel.getOnlyGroupNameDoneEvent.observe(viewLifecycleOwner, {
//            if (it) {
//                spinnerAddListCall()
//                //스피너
//                val spinnerAdapter =
//                    ArrayAdapter<String>(
//                        requireContext(),
//                        android.R.layout.simple_list_item_1,
//                        spinnerGroupNameList
//                    )
//                binding.spinnerGroup.adapter = spinnerAdapter
//
//
//                // 전달된 데이터 선택 시, 해당값 출력하기
//                binding.spinnerGroup.onItemSelectedListener =
//                    object : AdapterView.OnItemSelectedListener { //object 선언 언제 하는거야????
//                        override fun onItemSelected(
//                            parent: AdapterView<*>?,
//                            view: View?,
//                            position: Int,
//                            id: Long
//                        ) {
//                            // 스피너에서 선택한 그룹명
//                            selectedGroupName =
//                                spinnerGroupNameList.get(position) // 스피너의 어떤 데이터를 선택했느냐를 position으로 알수가 있다. 그래서 get(position)으로 데이터 찾는거임
//
//                            // 스피너 그룹명 선택에 따른 경우
//                            when (selectedGroupName) {
//
//                                // 0. 초기상태, 아무 변화 없음
//                                "" -> {
//                                    binding.contactGroupUpdate.isEnabled = false
//                                    binding.contactGroupUpdate.setText(args.currentItem.group)
//                                }
//
//                                // 1. "그룹명" 선택 시, 아무 변화 없음
//                                "그룹명 선택" -> {
//                                    binding.contactGroupUpdate.isEnabled = false
//                                    binding.contactGroupUpdate.setText(args.currentItem.group)
//                                }
//
//                                // 2. 직접입력 선택 시,
//                                "직접입력" -> {
////                                    binding.contactGroupUpdate.visibility = View.VISIBLE
//                                    // editText 사용가능하게 함
//                                    binding.contactGroupUpdate.isEnabled = true
//                                    // 포커스 지정
//                                    binding.contactGroupUpdate.requestFocus()
//                                    // 포커스 끝으로 보냄
//                                    binding.contactGroupUpdate.setSelection(binding.contactGroupUpdate.text.length)
//                                    // 키보드 올리기
//                                    val mInputMethodManager =
//                                        context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//                                    mInputMethodManager.showSoftInput(
//                                        binding.contactGroupUpdate,
//                                        InputMethodManager.SHOW_IMPLICIT
//                                    )
//                                }
//                                // 3. 그 외, 추가된 실제 group 선택 시,
//                                else -> {
////                                    binding.contactGroupUpdate.visibility = View.INVISIBLE
//                                    binding.contactGroupUpdate.isEnabled = false
//                                    binding.contactGroupUpdate.setText(selectedGroupName)
//                                }
//                            }
//                        }
//                        override fun onNothingSelected(parent: AdapterView<*>?) {
//                        }
//                    }
//            }
//        })
//
//        // 배경 터치 시, 키보드 내림
//        binding.updateFragmentBackground.setOnClickListener {
//            val mInputMethodManager =
//                requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//            mInputMethodManager.hideSoftInputFromWindow(it.getWindowToken(), 0)
//        }
//
//        binding.btnUpdate.setOnClickListener {
//            updateData()
//        }
//
//        setHasOptionsMenu(true)
//        return binding.root
//    }
//
//    // SharedPreferences 로부터 groupList를 불러와서 spinnerGroupNameList에 추가하는 함수
//    private fun spinnerAddListCall() {
//        val lastList = MyApplication.prefs.getString("group", "None")
//        val arrJson = JSONArray(lastList)
//        for (i in 0 until arrJson.length()) {
//            spinnerGroupNameList.add(arrJson.optString(i))
//        }
//    }
//
//    // 그룹을 추가하고(GroupList), 연락처 정보를 변경하는 함수
//    private fun updateData() {
//        val contactName = binding.contactNameUpdate.text.toString()
//        val contactNumber = binding.contactNumberUpdate.text.toString()
//        val contactGroup = binding.contactGroupUpdate.text.toString()
//
//        val preContactName = args.currentItem.name
//        val preContactNumber = args.currentItem.number
//        val preContactGroup = args.currentItem.group
//
//        val builder = AlertDialog.Builder(requireContext())
//        builder.setPositiveButton("예") { _, _ ->
//
//            val updateList = ContactBase(
//                contactName,
//                contactNumber,
//                contactGroup,
//                args.currentItem.image,
//                args.currentItem.id
//            )
//
//            Log.d("확인", "$updateList")
//            // 업데이트 data to ContactBase DB
//            listViewModel.update(updateList)
//
//            // 아래 코드는 신규 GroupList DB를 관리하는 코드
//            if (args.phoneNumber == "") {
//                val insertList = GroupList(
//                    contactName,
//                    contactNumber,
//                    contactGroup,
//                    args.currentItem.image,
//                    "0",
//                    "0",
//                    0
//                )
//                // ContactAdapter 에서 넘어온 groupName 이 없을 때(지정된 Group 이 없을 때)
//                when (contactGroup) {
//                    // 신규 지정 Group 도 없다면
//                    "" -> {
//                        Toast.makeText(requireContext(), "Group DB 변화 없음", Toast.LENGTH_SHORT)
//                            .show()
//                    }
//                    // 넘어온 건 없는데, 신규 지정 Group 은 있다면, 그룹 DB 에 신규 추가
//                    else -> {
//                        groupViewModel.insert(insertList)
//                        Toast.makeText(requireContext(), "Group DB 추가", Toast.LENGTH_SHORT)
//                            .show()
//                    }
//                }
//            }
//            // 넘어온 groupName 이 있을 때(지정된 Group 존재할 때)
//            else {
//                when (contactGroup) {
//                    // 수정하여 Group 을 없앨 때, 그룹 DB 에서 해당 List 제거
//                    "" -> {
//                        groupViewModel.findAndDelete(args.currentItem.number)
//                        Toast.makeText(requireContext(), "Group DB 제거", Toast.LENGTH_SHORT)
//                            .show()
//                    }
//                    // 수정하여 Group 을 바꿀 때, 그룹 DB 에서 해당 List 업데이트
//                    else -> {
//                        groupViewModel.findAndUpdate(contactName, contactNumber, contactGroup)
//                        Toast.makeText(requireContext(), "Group DB 수정", Toast.LENGTH_SHORT)
//                            .show()
//                    }
//                }
//            }
//
//            // recyclerView 정렬을 위해서 인자를 넘겨주는 것임
//            findNavController().navigate(
//                UpdateFragmentDirections.actionUpdateFragmentToListFragment(
//                    args.currentItem.id
//                )
//            )
//        }
//        builder.setNegativeButton("아니오") { _, _ -> }
//        builder.setTitle("데이터 변경")
//        builder.setMessage("$preContactName -> $contactName \n $preContactNumber -> $contactNumber \n $preContactGroup -> $contactGroup")
//        builder.create().show()
//
//    }
//
//    // 메뉴 활성화
//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        inflater.inflate(R.menu.delete_menu, menu)
//    }
//
//    // 메뉴 터치 시 작동
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        if (item.itemId == R.id.menu_delete) {
//            deleteData()
//        }
//        return super.onOptionsItemSelected(item)
//    }
//
//    private fun deleteData() {
//        val builder = AlertDialog.Builder(requireContext())
//        builder.setPositiveButton("Yes") { _, _ ->
//            listViewModel.delete(args.currentItem)
//            Toast.makeText(
//                requireContext(),
//                "${args.currentItem.name} 정보 삭제됨",
//                Toast.LENGTH_SHORT
//            ).show()
//            findNavController().navigate(UpdateFragmentDirections.actionUpdateFragmentToListFragment())
//        }
//        builder.setNegativeButton("No") { _, _ -> }
//        builder.setTitle("삭제 ${args.currentItem.name}")
//        builder.setMessage("해당 정보를 삭제하시겠습니까?")
//        builder.create().show()
//    }
//
//}
