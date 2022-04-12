package com.dreamreco.howru.group.groupListAdd

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.dreamreco.howru.R
import com.dreamreco.howru.data.entity.ContactBase
import com.dreamreco.howru.databinding.FragmentGroupListAddBinding
import com.dreamreco.howru.util.InputModeLifecycleHelper
import com.dreamreco.howru.util.clearFocusAndHideKeyboard
import com.hieupt.android.standalonescrollbar.attachTo
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GroupListAddFragment : Fragment(), SearchView.OnQueryTextListener {

    private val binding by lazy { FragmentGroupListAddBinding.inflate(layoutInflater) }
    private val groupListAddViewModel: GroupListAddViewModel by viewModels()
    private val adapter: GroupListAddAdapter by lazy { GroupListAddAdapter(requireContext()) }
    private val args by navArgs<GroupListAddFragmentArgs>()

    private val adapterListClearCode = MutableLiveData<String>()

    // deprecated 대비용
//    private var window: Window? = null
//    private var originalMode: Int = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
//
//    private val lifecycleEventObserver = LifecycleEventObserver { source, event ->
//        if (event == Lifecycle.Event.ON_START) {
//            window?.let {
//                originalMode = it.getSoftInputMode()
//                it.setSoftInputMode(
//                        WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
//                )
//            }
//        } else if (event == Lifecycle.Event.ON_STOP) {
//            if (originalMode != WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN) {
//                window?.setSoftInputMode(originalMode)
//            }
//            window = null
//        }
//    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        showProgress(true)
        binding.groupListAddRecyclerView.adapter = adapter

        groupListAddViewModel.getOtherContactBase(args.groupName).observe(viewLifecycleOwner){
            adapter.submitList(it)
            showProgress(false)
        }

        binding.btnGroupAdd.setOnClickListener {
            val list = adapter.getCheckBoxReturnList()
            if (list == mutableListOf<ContactBase>()) {
                Toast.makeText(requireContext(), "선택된 연락처가 없습니다.", Toast.LENGTH_SHORT).show()
            } else {
                // 체크박스 선택된 데이터 업데이트
                updateDataInScope(list)
            }
        }

        // 업데이트 완료 시, GroupListFragment 이동 코드
        groupListAddViewModel.navigateEvent.observe(viewLifecycleOwner) { updateFinished ->
            if (updateFinished) {
                binding.apply {
                    // 검색 사용 시, 키보드 먼저 내리고 이동
                    btnGroupAdd.clearFocusAndHideKeyboard(requireContext())
                    // 시간을 두고 dismiss 후 Fragment 이동
                    btnGroupAdd.postDelayed({
                        val action =
                            GroupListAddFragmentDirections.actionGroupListAddFragmentToGroupListFragment(
                                args.groupName
                            )
                        findNavController().navigate(action)
                        groupListAddViewModel.navigateDone()
                    }, 50)
                }
            }
        }


        viewLifecycleOwner.lifecycle.addObserver(InputModeLifecycleHelper(activity?.window))

        // deprecated 대비용
//        viewLifecycleOwner.lifecycle.addObserver(lifecycleEventObserver)


        // scroll bar
        val colorThumb = ContextCompat.getColor(requireContext(), R.color.hau_dark_green)
        val colorTrack = ContextCompat.getColor(requireContext(), android.R.color.transparent)
        with(binding) {
            scrollBar.attachTo(binding.groupListAddRecyclerView)
            scrollBar.defaultThumbTint = ColorStateList.valueOf(colorThumb)
            scrollBar.defaultTrackTint = ColorStateList.valueOf(colorTrack)
        }


        setHasOptionsMenu(true)

        return binding.root
    }


    // 그룹을 추가하고(GroupList), 연락처 정보를 변경하는 함수
    private fun updateDataInScope(list : List<ContactBase>) {
        groupListAddViewModel.addGroupListData(list, args.groupName)
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.group_list_add_menu, menu)

        val search = menu.findItem(R.id.menu_search)
        val searchView = search?.actionView as? SearchView
        searchView?.isSubmitButtonEnabled = false
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
        adapterListClearCode.value = searchQuery
        // 검색 텍스트가 변할 때 마다, 어뎁터 초기화
        adapterListClearCode.observe(viewLifecycleOwner){
            adapter.submitList(null)
            adapter.clearReturnList()
            adapter.clearCheckBox()
        }
        // 검색어에 맞는 결과 어뎁터에 전달
        groupListAddViewModel.searchDatabase(searchQuery).observe(viewLifecycleOwner) { list ->
            list.let {
                adapter.submitList(it)
            }
        }
    }

    private fun showProgress(show: Boolean) {
        binding.groupListAddProgressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

}