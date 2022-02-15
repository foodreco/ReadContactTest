package com.leesangmin89.readcontacttest.group.groupList

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.leesangmin89.readcontacttest.R
import com.leesangmin89.readcontacttest.databinding.FragmentGroupListBinding
import com.leesangmin89.readcontacttest.group.GroupViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GroupListFragment : Fragment() {

    private val binding by lazy { FragmentGroupListBinding.inflate(layoutInflater) }
    private val groupViewModel: GroupViewModel by viewModels()
    private val args by navArgs<GroupListFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val adapter = GroupListAdapter(requireContext())
        binding.groupListRecyclerView.adapter = adapter

        showProgress(true)


        // 1.GroupAdapter 에서 넘어온 groupName 을 매개로 GroupList 로부터 해당 그룹 정보를 가져오는 함수
        groupViewModel.getGroupListFromGroupList(args.groupName)

        // 2. GroupList recyclerView 출력 코드 (GroupList 정보 소환 후, 작동)
        groupViewModel.groupListGetEvent.observe(viewLifecycleOwner,{ listGetFinished ->
            if (listGetFinished) {
                // GroupList 정보를 업데이트하는 함수
                Log.e("수정","로드 딜레이 있음, 프로그래스바 추가 or 앱 빌드 시 실행")
                groupViewModel.updateGroupRecentInfo(args.groupName)
            }
        })

        // 3. 업데이트 된 GroupList 정보 출력 코드 (GroupList 업데이트 후, 작동)
        groupViewModel.groupListUpdateEvent.observe(viewLifecycleOwner,{ updateFinished ->
            if (updateFinished) {
                // 가져온 GroupList 정보를 recyclerView 형태로 출력하는 코드
                groupViewModel.newGroupList.observe(viewLifecycleOwner, {
                    adapter.submitList(it)
                    showProgress(false)
                })
            }
        })


        // deleteData() 작업이 완료되면 GroupFragment 로 이동
        groupViewModel.coroutineDoneEvent.observe(viewLifecycleOwner, {
            if (it) {
                findNavController().navigate(GroupListFragmentDirections.actionGroupListFragmentToGroupFragment())
            }
        })

        setHasOptionsMenu(true)

        return binding.root
    }

    // 메뉴 활성화
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.delete_menu, menu)
    }

    // 메뉴 터치 시 작동
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_delte) {
            deleteData()
        }
        return super.onOptionsItemSelected(item)
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