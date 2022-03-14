package com.leesangmin89.readcontacttest.group.groupListAdd

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.leesangmin89.readcontacttest.R
import com.leesangmin89.readcontacttest.data.entity.ContactBase
import com.leesangmin89.readcontacttest.databinding.FragmentGroupListAddBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GroupListAddFragment : Fragment(), SearchView.OnQueryTextListener {

    private val binding by lazy { FragmentGroupListAddBinding.inflate(layoutInflater) }
    private val groupListAddViewModel: GroupListAddViewModel by viewModels()
    private val adapter: GroupListAddAdapter by lazy { GroupListAddAdapter(requireContext()) }
    private val args by navArgs<GroupListAddFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        showProgress(true)
        binding.groupListAddRecyclerView.adapter = adapter

        groupListAddViewModel.initSort(args.groupName)

        groupListAddViewModel.liveList.observe(viewLifecycleOwner, {
            adapter.submitList(it)
            showProgress(false)
        })

        binding.btnGroupAdd.setOnClickListener {
            val list = adapter.getCheckBoxReturnList()

            if (list == mutableListOf<ContactBase>()) {
                Toast.makeText(requireContext(), "선택된 연락처가 없습니다.", Toast.LENGTH_SHORT).show()
            } else {
                // 체크박스 선택된 데이터 업데이트
                updateDataInScope()
            }
        }

        // 업데이트 완료 시, GroupListFragment 이동 코드
        groupListAddViewModel.navigateEvent.observe(viewLifecycleOwner, { updateFinished ->
            if (updateFinished) {
                val action =
                    GroupListAddFragmentDirections.actionGroupListAddFragmentToGroupListFragment(
                        args.groupName
                    )
                findNavController().navigate(action)
                groupListAddViewModel.navigateDone()
            }
        })

        setHasOptionsMenu(true)
        return binding.root
    }

    // 그룹을 추가하고(GroupList), 연락처 정보를 변경하는 함수
    private fun updateDataInScope() {
        val list: List<ContactBase> = adapter.getCheckBoxReturnList()
        groupListAddViewModel.addGroupListData(list, args.groupName)
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.group_list_add_menu, menu)

        val search = menu.findItem(R.id.menu_search)
        val searchView = search?.actionView as? SearchView
        searchView?.isSubmitButtonEnabled = true
        searchView?.setOnQueryTextListener(this)
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return true
    }

    override fun onQueryTextChange(query: String?): Boolean {
        if (query != null) {
            searchDatabase(query)
        }
        return true
    }

    private fun searchDatabase(query: String) {
        val searchQuery = "%$query%"
        groupListAddViewModel.searchDatabase(searchQuery).observe(this, { list ->
            list.let {
                adapter.submitList(it)
                adapter.clearCheckBox(1)
            }
        })
    }

    fun showProgress(show: Boolean) {
        binding.groupListAddProgressBar.visibility = if (show) View.VISIBLE else View.GONE
    }


}