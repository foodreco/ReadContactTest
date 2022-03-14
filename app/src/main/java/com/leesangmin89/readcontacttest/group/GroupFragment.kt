package com.leesangmin89.readcontacttest.group

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.transition.MaterialFadeThrough
import com.leesangmin89.readcontacttest.R
import com.leesangmin89.readcontacttest.databinding.FragmentGroupBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GroupFragment : Fragment() {

    private val binding by lazy { FragmentGroupBinding.inflate(layoutInflater) }
    private val groupViewModel: GroupViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val adapter = GroupAdapter(requireContext())
        binding.groupRecyclerView.adapter = adapter

        // onCreateView 와 함께 리스트 업데이트
        groupViewModel.getGroupName()

        // 업데이트 리스트 recyclerView로 반환
        groupViewModel.groupInfo.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        groupViewModel.groupListEmptyEvent.observe(viewLifecycleOwner) {
            if (it) {
                // 그룹이 empty 인 경우, 확인창 띄우기
                val builder = AlertDialog.Builder(requireContext())
                builder.setPositiveButton("그룹추가") { dialog, _ ->
                    Log.i("확인", "다이아로그 ok")
                    dialog.dismiss()
                    addGroup()
                }
                builder.setTitle("그룹 없음")
                builder.setMessage("연락처에 지정된 그룹이 없습니다. \n 그룹을 추가하세요!")
                builder.create().show()

                groupViewModel.groupListEmptyChecked()
            }
        }
        setHasOptionsMenu(true)

        return binding.root
    }

    // 메뉴 활성화
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.group_menu, menu)
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
        // Dialog 를 거쳐서 GroupListAddFragment 까지 가야하므로, navigation 을 사용한다.
        findNavController().navigate(GroupFragmentDirections.actionGroupFragmentToGroupAddDialog())
    }

    private fun deletePart() {
        Toast.makeText(requireContext(), "부분 삭제 코드 미완성", Toast.LENGTH_SHORT).show()
    }

    private fun deleteData() {
        Toast.makeText(requireContext(), "전체 삭제 코드 미완성", Toast.LENGTH_SHORT).show()
    }

}