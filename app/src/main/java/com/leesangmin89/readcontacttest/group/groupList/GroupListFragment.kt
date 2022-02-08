package com.leesangmin89.readcontacttest.group.groupList

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.leesangmin89.readcontacttest.R
import com.leesangmin89.readcontacttest.databinding.FragmentGroupListBinding
import com.leesangmin89.readcontacttest.group.GroupViewModel
import com.leesangmin89.readcontacttest.update.UpdateFragmentDirections
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

        // GroupFragment Adapter 에서 넘어온 groupName 을 매개로 GroupList 로부터 해당 그룹 정보를 가져오는 함수
        groupViewModel.getGroupListFromGroupList(args.groupName)

        // 기존 불러오려던 형태 작동코드
//        groupViewModel.getGroupListFromContactBase(args.groupName)
        // 기존 불러오려던 형태의 recyclerView
//        groupViewModel.groupList.observe(viewLifecycleOwner, {
//            adapter.submitList(it)
//        })

        // 신규 형태의 recyclerView
        groupViewModel.newGroupList.observe(viewLifecycleOwner,{
            adapter.submitList(it)
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

    private fun deleteData() {
//        val builder = AlertDialog.Builder(requireContext())
//        builder.setPositiveButton("Yes") { _, _ ->
//            groupViewModel.clear()
//            Toast.makeText(
//                requireContext(),
//                "그룹 DB 삭제됨",
//                Toast.LENGTH_SHORT
//            ).show()
//            findNavController().navigate(GroupListFragmentDirections.actionGroupListFragmentToGroupFragment())
//        }
//        builder.setNegativeButton("No") { _, _ -> }
//        builder.setTitle("그룹 DB 삭제")
//        builder.setMessage("해당 정보를 삭제하시겠습니까?")
//        builder.create().show()
    }


}