package com.leesangmin89.readcontacttest.update

import android.app.AlertDialog
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.leesangmin89.readcontacttest.data.entity.ContactBase
import com.leesangmin89.readcontacttest.R
import com.leesangmin89.readcontacttest.data.entity.GroupList
import com.leesangmin89.readcontacttest.databinding.FragmentUpdateBinding
import com.leesangmin89.readcontacttest.group.GroupViewModel
import com.leesangmin89.readcontacttest.list.ListViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.parcelize.Parcelize

@AndroidEntryPoint
class UpdateFragment : Fragment() {

    private val args by navArgs<UpdateFragmentArgs>()
    private val listViewModel: ListViewModel by viewModels()
    private val groupViewModel: GroupViewModel by viewModels()
    private val binding by lazy { FragmentUpdateBinding.inflate(layoutInflater) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding.contactNameUpdate.setText(args.currentItem.name)
        binding.contactNumberUpdate.setText(args.currentItem.number)
        binding.contactGroupUpdate.setText(args.currentItem.group)

        // 변경 버튼 클릭 시, data update
        binding.btnUpdate.setOnClickListener {
            updateData()
        }

        setHasOptionsMenu(true)

        return binding.root
    }

    private fun updateData() {
        val contactName = binding.contactNameUpdate.text.toString()
        val contactNumber = binding.contactNumberUpdate.text.toString()
        val contactGroup = binding.contactGroupUpdate.text.toString()

        val preContactName = args.currentItem.name
        val preContactNumber = args.currentItem.number
        val preContactGroup = args.currentItem.group

        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("예") { _, _ ->

            val updateList = ContactBase(
                contactName,
                contactNumber,
                contactGroup,
                args.currentItem.image,
                args.currentItem.id
            )

            Log.d("확인", "$updateList")
            // 업데이트 data to ContactBase DB
            listViewModel.update(updateList)

            // 아래 코드는 신규 GroupList DB를 관리하는 코드
            if (args.phoneNumber == "") {
                val insertList = GroupList(
                    contactName,
                    contactNumber,
                    contactGroup,
                    args.currentItem.image,
                    "0",
                    "0",
                    0
                )
                // ContactAdapter 에서 넘어온 groupName 이 없을 때(지정된 Group 이 없을 때)
                when (contactGroup) {
                    // 신규 지정 Group 도 없다면
                    "" -> {
                        Toast.makeText(requireContext(), "Group DB 변화 없음", Toast.LENGTH_SHORT)
                            .show()
                    }
                    // 넘어온 건 없는데, 신규 지정 Group 은 있다면, 그룹 DB 에 신규 추가
                    else -> {
                        groupViewModel.insert(insertList)
                        Toast.makeText(requireContext(), "Group DB 추가", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
            // 넘어온 groupName 이 있을 때(지정된 Group 존재할 때)
            else {
                when (contactGroup) {
                    // 수정하여 Group 을 없앨 때, 그룹 DB 에서 해당 List 제거
                    "" -> {
                        groupViewModel.findAndDelete(args.currentItem.number)
                        Toast.makeText(requireContext(), "Group DB 제거", Toast.LENGTH_SHORT)
                            .show()
                    }
                    // 수정하여 Group 을 바꿀 때, 그룹 DB 에서 해당 List 업데이트
                    else -> {
                        groupViewModel.findAndUpdate(contactName, contactNumber, contactGroup)
                        Toast.makeText(requireContext(), "Group DB 수정", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }

            // recyclerView 정렬을 위해서 인자를 넘겨주는 것임
            findNavController().navigate(
                UpdateFragmentDirections.actionUpdateFragmentToListFragment(
                    args.currentItem.id
                )
            )
        }
        builder.setNegativeButton("아니오") { _, _ -> }
        builder.setTitle("데이터 변경")
        builder.setMessage("$preContactName -> $contactName \n $preContactNumber -> $contactNumber \n $preContactGroup -> $contactGroup")
        builder.create().show()

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
        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("Yes") { _, _ ->
            listViewModel.delete(args.currentItem)
            Toast.makeText(
                requireContext(),
                "${args.currentItem.name} 정보 삭제됨",
                Toast.LENGTH_SHORT
            ).show()
            findNavController().navigate(UpdateFragmentDirections.actionUpdateFragmentToListFragment())
        }
        builder.setNegativeButton("No") { _, _ -> }
        builder.setTitle("삭제 ${args.currentItem.name}")
        builder.setMessage("해당 정보를 삭제하시겠습니까?")
        builder.create().show()
    }

}
