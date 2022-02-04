package com.leesangmin89.readcontacttest.add

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.leesangmin89.readcontacttest.data.entity.ContactBase
import com.leesangmin89.readcontacttest.databinding.FragmentAddBinding
import com.leesangmin89.readcontacttest.list.ListViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddFragment : Fragment() {

    private val listViewModel: ListViewModel by viewModels()
    private val binding by lazy { FragmentAddBinding.inflate(layoutInflater) }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        binding = FragmentAddBinding.inflate(layoutInflater, container, false)

//        // 뷰모델팩토리 세팅
//        val application = requireNotNull(this.activity).application
//        val dataSource = ContactDatabase.getInstance(application).contactDao
//        val viewModelFactory = ListViewModelFactory(dataSource, application)
//        // 뷰모델 초기화
//        listViewModel = ViewModelProvider(this, viewModelFactory).get(ListViewModel::class.java)

        binding.btnAdd.setOnClickListener {
            addData()
        }

        binding.addFragmentBackground.setOnClickListener {
//            hideKeyboard()
        }


        return binding.root
    }

    private fun addData() {
        val contactName = binding.contactNameAdd.text.toString()
        val contactNumber = binding.contactNumberAdd.text.toString()
        val contactGroup = binding.contactGroupAdd.text.toString()

        val addList = ContactBase(contactName, contactNumber, contactGroup, null, 0)
        listViewModel.insert(addList)
        Toast.makeText(requireContext(), "$contactName 추가완료", Toast.LENGTH_SHORT).show()
        findNavController().navigate(AddFragmentDirections.actionAddFragmentToListFragment())

    }

}