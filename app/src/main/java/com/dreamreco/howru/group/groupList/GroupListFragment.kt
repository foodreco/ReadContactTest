package com.dreamreco.howru.group.groupList

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.dreamreco.howru.R
import com.dreamreco.howru.databinding.FragmentGroupListBinding
import com.dreamreco.howru.group.GroupViewModel
import com.hieupt.android.standalonescrollbar.attachTo
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GroupListFragment : Fragment() {

    // 뒤로 가기 처리를 위한 콜백 변수
    private lateinit var callback: OnBackPressedCallback

    // 뒤로 가기 처리를 위한 Live 변수
    private val backEventCheckLive = MutableLiveData<Boolean>()

    private val binding by lazy { FragmentGroupListBinding.inflate(layoutInflater) }
    private val groupViewModel: GroupViewModel by viewModels()
    private val args by navArgs<GroupListFragmentArgs>()
    private val adapter: GroupListAdapter by lazy {
        GroupListAdapter(
            requireContext(),
            childFragmentManager
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding.groupListRecyclerView.adapter = adapter

        showProgress(true)

        with(groupViewModel) {
            // 그룹 추가된 리스트 정보 업데이트
            updateGroupRecentInfo(args.groupName)
            // 업데이트 후 해당 그룹 리스트를 Live 로 받아옴
            getGroupListFromGroupListByLive(args.groupName)

            // groupList LiveData 출력
            liveGroupLiveData.observe(viewLifecycleOwner) {
                // 업데이트 된 데이터로 Top Data 계산
                groupViewModel.makeGroupItem(it)
            }

            // 헤더 RecyclerView 출력
            groupListRecyclerView.observe(viewLifecycleOwner) {
                adapter.submitList(it)
                showProgress(false)
            }

            // deleteData() 작업이 완료되면 GroupFragment 로 이동
            coroutineDoneEvent.observe(viewLifecycleOwner) {
                if (it) {
                    findNavController().navigate(GroupListFragmentDirections.actionGroupListFragmentToGroupFragment())
                    groupViewModel.coroutineDoneEventFinished()
                }
            }
        }


        // 삭제 버튼 터치 시 작동 코드
        binding.btnGroupDelete.setOnClickListener {
            backEventCheckLive.value = false
            // 버튼 숨기고 체크박스 비활성화
            binding.btnGroupDelete.visibility = View.GONE

            // checkBox 된 GroupList 반환
            val numberList: List<String> = adapter.getCheckBoxReturnList()

            // 만약 목록 전체를 선택해 제거한다면, 전체삭제와 똑같이 작동해야 한다.
            arrangeGroupList(numberList, args.groupName)

            adapter.onCheckBox(0)
            adapter.clearCheckBoxReturnList()
        }

        with(adapter) {
            // 어뎁터 터치 알람 해제
            alarmNumberSetting.observe(viewLifecycleOwner) { groupList ->
                if (groupList != null) {
                    // GroupList DB 업데이트
                    groupViewModel.findAndUpdate(
                        groupList.name,
                        groupList.number,
                        groupList.group,
                        false
                    )
                    // 알림 false 설정 시, Reco DB 에서 해당 data 를 삭제하는 코드
                    // recommendation 이 해체되면, Recommendation DB 에서도 삭제되어야 함
                    groupViewModel.dataRecoDeleteByNumber(groupList.number)
                    adapter.alarmNumberReset()
                    Toast.makeText(requireContext(), "알림 해제됨", Toast.LENGTH_SHORT).show()
                }
            }
            // 어뎁터 터치 알람 설정
            alarmNumberRemoving.observe(viewLifecycleOwner) { groupList ->
                if (groupList != null) {
                    // GroupList DB 업데이트
                    groupViewModel.findAndUpdate(
                        groupList.name,
                        groupList.number,
                        groupList.group,
                        true
                    )
                    groupViewModel.updateDialogDone()
                    adapter.alarmNumberReset()
                    Toast.makeText(requireContext(), "알림 설정됨", Toast.LENGTH_SHORT).show()
                }
            }

            // 어뎁터 롱터치 삭제 설정
            deleteEventActive.observe(viewLifecycleOwner) { activate ->
                if (activate == true) {
                    deletePart()
                    adapter.deleteEventReset()
                }
            }

            // 전화걸기 작동 코드
            checkAndCall.observe(viewLifecycleOwner) { phoneNumber ->
                if (phoneNumber != null) {
                    checkPermissionsAndCall(phoneNumber)
                    adapter.checkAndCallClear()
                }
            }
        }


        // 백버튼 조건부 작동 코드
        backEventCheckLive.observe(viewLifecycleOwner) { backEvent ->
            if (backEvent) {
                // backEventCheckLive true 일 때, 백버튼 콜백 생성
                callback = object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        // 삭제 상태가 활성화 되었다면, 뒤로 가기 시 특정 코드 작동
                        cancelDeletePart()
                        adapter.clearCheckBoxReturnList()
                    }
                }
                requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
            }
            if (!backEvent) {
                // backEventCheckLive false 일 때, 백버튼 콜백 제거
                // 제거하지 않으면 기존 Fragment 뒤로가기 버튼이 작동하지 않는다.
                callback.remove()
            }
        }

        // scroll bar
        val colorThumb = ContextCompat.getColor(requireContext(), R.color.hau_dark_green)
        val colorTrack = ContextCompat.getColor(requireContext(), android.R.color.transparent)
        with(binding) {
            scrollBar.attachTo(binding.groupListRecyclerView)
            scrollBar.defaultThumbTint = ColorStateList.valueOf(colorThumb)
            scrollBar.defaultTrackTint = ColorStateList.valueOf(colorTrack)
        }


        setHasOptionsMenu(true)
        return binding.root
    }

    private fun arrangeGroupList(testList: List<String>, groupName: String) {
        groupViewModel.arrangeGroupList(testList, groupName)
    }

    // 메뉴 활성화
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.grouplist_menu, menu)
    }

    // 메뉴 터치 시 작동
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.delete_all -> deleteData()
            R.id.delete_part -> deletePart()
            R.id.menu_group_add -> addGroup()
        }
        return super.onOptionsItemSelected(item)
    }

    // 그룹 추가 함수
    private fun addGroup() {
        findNavController().navigate(
            GroupListFragmentDirections.actionGroupListFragmentToGroupListAddFragment(
                args.groupName
            )
        )
    }

    // 선택 삭제 함수
    private fun deletePart() {
        backEventCheckLive.value = true
        // 체크박스 활성화
        adapter.onCheckBox(1)
        // 뷰모델 방식으로 visibility 제어하자??
        binding.btnGroupDelete.visibility = View.VISIBLE
    }

    private fun cancelDeletePart() {
        backEventCheckLive.value = false
        // 체크박스 비활성화
        adapter.onCheckBox(0)
        binding.btnGroupDelete.visibility = View.GONE
    }

    // 전체 삭제 함수
    // GroupAdapter 에서 넘어온 groupName 을 매개로 GroupList 로부터 해당 그룹을 삭제하는 함수
    private fun deleteData() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("Yes") { _, _ ->
            with(groupViewModel) {
                clearByGroupName(args.groupName)
                clearGroupNameInContactBase(args.groupName)
                dataRecoDeleteByGroup(args.groupName)
            }
            Toast.makeText(
                requireContext(),
                "${args.groupName} 그룹 삭제됨",
                Toast.LENGTH_SHORT
            ).show()
        }
        builder.setNegativeButton("No") { _, _ -> }
        builder.setTitle("${args.groupName} 그룹 삭제")
        builder.setMessage("해당 정보를 삭제하시겠습니까?")
        builder.create().show()
    }

    private fun call(phoneNumber: String) {
        val uri = Uri.parse("tel:$phoneNumber")
        val intent = Intent(Intent.ACTION_CALL, uri)
        requireContext().startActivity(intent)
    }

    // 허용 체크 후, 전화걸기
    private fun checkPermissionsAndCall(phoneNumber: String) {
        val permission = Manifest.permission.CALL_PHONE
        if (ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
            // 허용 안되어 있는 경우, 요청
            requestCallPermission.launch(
                permission
            )
        } else {
            // 허용 되어있는 경우, 전화걸기
            call(phoneNumber)
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


    private fun showProgress(show: Boolean) {
        binding.groupListProgressBar.visibility = if (show) View.VISIBLE else View.GONE
    }
}