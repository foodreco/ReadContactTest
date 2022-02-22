package com.leesangmin89.readcontacttest.group.groupList

import android.app.AlertDialog
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.room.PrimaryKey
import com.leesangmin89.readcontacttest.R
import com.leesangmin89.readcontacttest.data.entity.ContactBase
import com.leesangmin89.readcontacttest.data.entity.GroupList
import com.leesangmin89.readcontacttest.databinding.FragmentGroupListBinding
import com.leesangmin89.readcontacttest.group.GroupViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GroupListFragment : Fragment() {

    private val binding by lazy { FragmentGroupListBinding.inflate(layoutInflater) }
    private val groupViewModel: GroupViewModel by viewModels()
    private val args by navArgs<GroupListFragmentArgs>()
    private val adapter: GroupListAdapter by lazy { GroupListAdapter(requireContext()) }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding.groupListRecyclerView.adapter = adapter

        showProgress(true)

        // 1.GroupAdapter, GroupListAddFragment 에서 넘어온 groupName 을 매개로 GroupList 로부터 해당 그룹 정보를 가져오는 함수
        groupViewModel.getGroupListFromGroupList(args.groupName)

        // 2. GroupList 업데이트 코드 (1.에서 불러온 데이터를 다시 업데이트 함)
        groupViewModel.groupListGetEvent.observe(viewLifecycleOwner,{ listGetFinished ->
            if (listGetFinished) {
                // GroupList 정보를 업데이트하는 함수
                groupViewModel.updateGroupRecentInfo(args.groupName)
                Log.i("확인", "GroupList 정보를 업데이트하는 함수")

            }
        })

        // 3. 업데이트 된 GroupList 정보 출력 코드 (GroupList 업데이트 후, 작동)
        groupViewModel.groupListUpdateEvent.observe(viewLifecycleOwner,{ updateFinished ->
            if (updateFinished) {
                // 가져온 GroupList 정보를 recyclerView 형태로 출력하는 코드
                groupViewModel.newGroupList.observe(viewLifecycleOwner, {
                    adapter.submitList(it)
                    showProgress(false)
                    Log.i("확인", "가져온 GroupList 정보를 recyclerView 형태로 출력하는 코드")
                })
            }
        })


        // deleteData() 작업이 완료되면 GroupFragment 로 이동
        groupViewModel.coroutineDoneEvent.observe(viewLifecycleOwner, {
            if (it) {
                findNavController().navigate(GroupListFragmentDirections.actionGroupListFragmentToGroupFragment())
            }
        })

        binding.btnGroupDelete.setOnClickListener {
            // 버튼 숨기고 체크박스 비활성화
            binding.btnGroupDelete.visibility = View.GONE
            binding.btnCancel.visibility = View.GONE
            adapter.onCheckBox(0)

            // checkBox 된 GroupList 반환
            val testList : List<GroupList> = adapter.getCheckBoxReturnList()

            // 만약 목록 전체를 선택해 제거한다면, 전체삭제와 똑같이 작동해야 한다.
            arrangeGroupList(testList, args.groupName)
        }

        binding.btnCancel.setOnClickListener {
            cancelDeletePart()
        }

        setHasOptionsMenu(true)

        return binding.root
    }


    private fun arrangeGroupList(testList: List<GroupList>, groupName: String) {
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

    private fun addGroup() {
        findNavController().navigate(GroupListFragmentDirections.actionGroupListFragmentToGroupListAddFragment(args.groupName))
    }

    private fun deletePart() {
        // 체크박스 활성화
        adapter.onCheckBox(1)
        // 뷰모델 방식으로 visibility 제어하자??
        binding.btnGroupDelete.visibility = View.VISIBLE
        binding.btnCancel.visibility = View.VISIBLE
    }

    private fun cancelDeletePart() {
        // 체크박스 비활성화
        adapter.onCheckBox(0)
        binding.btnGroupDelete.visibility = View.GONE
        binding.btnCancel.visibility = View.GONE
    }

    // GroupAdapter 에서 넘어온 groupName 을 매개로 GroupList 로부터 해당 그룹을 삭제하는 함수
    private fun deleteData() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("Yes") { _, _ ->
            groupViewModel.clearByGroupName(args.groupName)
            groupViewModel.clearGroupNameInContactBase(args.groupName)
            Toast.makeText(
                requireContext(),
                "${args.groupName} DB 삭제됨",
                Toast.LENGTH_SHORT
            ).show()
        }
        builder.setNegativeButton("No") { _, _ -> }
        builder.setTitle("${args.groupName} 그룹 삭제")
        builder.setMessage("해당 정보를 삭제하시겠습니까?")
        builder.create().show()
    }

    fun showProgress(show:Boolean) {
        binding.groupListProgressBar.visibility = if(show) View.VISIBLE else View.GONE
    }
}