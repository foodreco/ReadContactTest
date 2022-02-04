package com.leesangmin89.readcontacttest.update

import android.app.AlertDialog
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Parcelable
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
    private val groupViewModel : GroupViewModel by viewModels()
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

            val updateList = ContactBase(contactName, contactNumber, contactGroup, args.currentItem.image, args.currentItem.id)
            if (args.groupNumber == "") {
                val insertList = GroupList(contactName, contactNumber, contactGroup, args.currentItem.image, "0","0",0)
                groupViewModel.insert(insertList)
            } else {
                groupViewModel.find(args.groupNumber)
                val newList = groupViewModel.groupListForUpdate
                val insertList = GroupList(contactName, contactNumber, contactGroup, newList.image, newList.recentContact,newList.recentContactCallTime,newList.id)
                groupViewModel.update(insertList)
            }

            // 업데이트 data to DB
            listViewModel.update(updateList)
            Toast.makeText(requireContext(), "업데이트 완료", Toast.LENGTH_SHORT).show()

            // GroupList DB 인서트

            // recyclerView 정렬을 위해서 인자를 넘겨주는 것임
            findNavController().navigate(UpdateFragmentDirections.actionUpdateFragmentToListFragment(args.currentItem.id))
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
