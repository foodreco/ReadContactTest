package com.dreamreco.howru.list

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import com.dreamreco.howru.R
import com.dreamreco.howru.data.entity.ContactBase
import com.dreamreco.howru.databinding.FragmentListBinding
import com.dreamreco.howru.util.ContactBaseItem
import com.hieupt.android.standalonescrollbar.attachTo
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ListFragment : Fragment(), SearchView.OnQueryTextListener {

    // 요청 권한 리스트
    companion object {
        val PERMISSIONS = arrayOf(
            Manifest.permission.READ_CONTACTS
        )
    }

    private val binding by lazy { FragmentListBinding.inflate(layoutInflater) }
    private val listViewModel: ListViewModel by viewModels()
    private val adapter: ContactAdapter by lazy {
        ContactAdapter(
            requireContext(),
            childFragmentManager
        )
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("Range")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        with(binding) {
            recyclerViewList.adapter = adapter

            // 화살 버튼 클릭 시, 리싸이클러뷰 최상단 이동
            btnUpScroll.setOnClickListener {
                binding.recyclerViewList.scrollToPosition(0)
            }
        }

        with(listViewModel) {
            // ContactBase DB 모든 값을 이름오름차순으로 가져오는 코드
            getContactBaseLiveData().observe(viewLifecycleOwner) {
                if (it == emptyList<ContactBase>()) {
                    // 만약 연락처 리스트가 아무것도 없다면 (empty 헤더밖에 없다면,)
                    binding.btnUpScroll.visibility = View.GONE
                } else {
                    binding.btnUpScroll.visibility = View.VISIBLE
                }
                listViewModel.makeList(it)
            }
            // 변환된 코드를 헤더 adapter 에 적용하는 코드
            contactBaseItemData.observe(viewLifecycleOwner) {
                adapter.submitList(it)
            }
            // 연락처 업데이트 옵저버
            initializeContactEvent.observe(
                viewLifecycleOwner
            ) {
                if (it) {
                    updateContactBase()
                }
            }
        }


        // scroll bar
        val colorThumb = ContextCompat.getColor(requireContext(), R.color.hau_dark_green)
        val colorTrack = ContextCompat.getColor(requireContext(), android.R.color.transparent)
        with(binding) {
            scrollBar.attachTo(binding.recyclerViewList)
            scrollBar.defaultThumbTint = ColorStateList.valueOf(colorThumb)
            scrollBar.defaultTrackTint = ColorStateList.valueOf(colorTrack)
        }
        setHasOptionsMenu(true)

        return binding.root
    }


    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.list_menu, menu)

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
        listViewModel.searchDatabase(searchQuery).observe(this) { list ->
            if (list != emptyList<ContactBase>()) {
                listViewModel.makeList(list)
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.update_menu -> {
                checkPermissionsAndStart(PERMISSIONS)
                true
            }
            R.id.sort_by_call -> {
                getHavingCallLogDataContactBaseOnly()
                true
            }
            R.id.sort_all -> {
                // ContactBase DB 모든 값을 이름오름차순으로 가져오는 코드
                listViewModel.getContactBaseLiveData().observe(viewLifecycleOwner) {
                    listViewModel.makeList(it)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // 통화이력 있는 연락처만 가져오는 코드
    private fun getHavingCallLogDataContactBaseOnly() {
        listViewModel.sortByCallLog().observe(viewLifecycleOwner) { numberList ->
            if (numberList != emptyList<String>()) {
                listViewModel.checkCallLogData(numberList)
            } else {
                // 통화기록이 없을 때,
                Toast.makeText(requireContext(), "통화기록이 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 초기화 진행 함수
    private fun activatingContact() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("예") { _, _ ->
            listViewModel.contactActivate()
            Toast.makeText(
                requireContext(),
                "연락처 업데이트 됨",
                Toast.LENGTH_SHORT
            ).show()
        }
        builder.setNegativeButton("아니오") { _, _ -> }
        builder.setTitle("연락처 업데이트")
        builder.setMessage("연락처 정보를 업데이트하시겠습니까?")
        builder.create().show()
    }

    // 초기 데이터 로드 함수(ContactBase)
    @SuppressLint("Range")
    fun updateContactBase() {
        listViewModel.updateContactBase(requireActivity())
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 권한 요청 관련 함수

    private fun checkPermissionsAndStart(permissions: Array<String>) {
        if (!checkNeedPermissionBoolean(permissions)) {
            // 허용 안되어 있는 경우, 요청
            requestMultiplePermissions.launch(
                permissions
            )
        } else {
            // 허용 되어있는 경우, 통화기록, 통계 가져오기
            activatingContact()
        }
    }

    // 허용 여부에 따른 Boolean 반환
    private fun checkNeedPermissionBoolean(permissions: Array<String>): Boolean {
        for (perm in permissions) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    perm
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    // 허용 요청 코드
    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.entries.all {
                it.value
            }
            if (granted) {
                // 허용된, 경우
                Toast.makeText(context, "권한 허용", Toast.LENGTH_SHORT).show()
                activatingContact()
            } else {
                // 허용안된 경우,
                Toast.makeText(
                    context, "연락처 접근 권한이 거부되었으므로, \n" +
                            "불러올 수 없습니다.", Toast.LENGTH_SHORT
                ).show()
            }
        }
}