package com.leesangmin89.readcontacttest.update

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.leesangmin89.readcontacttest.data.ContactBase
import com.leesangmin89.readcontacttest.R
import com.leesangmin89.readcontacttest.data.ContactDatabase
import com.leesangmin89.readcontacttest.databinding.FragmentUpdateBinding
import com.leesangmin89.readcontacttest.list.ListViewModel
import com.leesangmin89.readcontacttest.list.ListViewModelFactory

class UpdateFragment : Fragment() {

    private val args by navArgs<UpdateFragmentArgs>()
    private lateinit var listViewModel: ListViewModel
    private val binding by lazy { FragmentUpdateBinding.inflate(layoutInflater) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // 뷰모델팩토리 세팅
        val application = requireNotNull(this.activity).application
        val dataSource = ContactDatabase.getInstance(application).contactDao
        val viewModelFactory = ListViewModelFactory(dataSource, application)
        // 뷰모델 초기화
        listViewModel = ViewModelProvider(this, viewModelFactory).get(ListViewModel::class.java)

        binding.contactNameUpdate.setText(args.currentItem.name)
        binding.contactNumberUpdate.setText(args.currentItem.number)

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

        val preContactName = args.currentItem.name
        val preContactNumber = args.currentItem.number

        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("예") { _, _ ->

            val updateList = ContactBase(contactName, contactNumber, args.currentItem.id)
            // 업데이트 data to DB
            listViewModel.update(updateList)
            Toast.makeText(requireContext(), "업데이트 완료", Toast.LENGTH_SHORT).show()
            findNavController().navigate(UpdateFragmentDirections.actionUpdateFragmentToListFragment())
        }
        builder.setNegativeButton("아니오") { _, _ -> }
        builder.setTitle("데이터 변경")
        builder.setMessage("$preContactName -> $contactName \n $preContactNumber -> $contactNumber")
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