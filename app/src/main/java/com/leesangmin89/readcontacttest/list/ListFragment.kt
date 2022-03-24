package com.leesangmin89.readcontacttest.list

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialFadeThrough
import com.hieupt.android.standalonescrollbar.attachTo
import com.leesangmin89.readcontacttest.R
import com.leesangmin89.readcontacttest.databinding.FragmentListBinding
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

        binding.recyclerViewList.adapter = adapter

        // 초기화 버튼 클릭 시, 연락처 데이터를 다시 가져오는 코드
        binding.btnUpdateList.setOnClickListener {
            checkPermissionsAndStart(PERMISSIONS)
        }

        // 리싸이클러뷰 옵저버
        // 정렬기능 추가 (구조 개선 필요!! 처음 로드 시 앱 데드)
        Log.i("보완", "통화기록이 있는 연락처만 불러오기 정렬 -> 그룹 추가 용이")
        Log.i("보완", "recyclerView 헤더 추가하기")

        listViewModel.testData.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        // 연락처 업데이트 옵저버
        listViewModel.initializeContactEvent.observe(
            viewLifecycleOwner
        ) {
            if (it) {
                updateContactBase()
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
            list.let {
                adapter.submitList(it)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.delete_menu -> {
                deleteData()
                true
            }
            R.id.sort_by_call -> {
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // 데이터 전체 삭제 함수
    private fun deleteData() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("Yes") { _, _ ->
            listViewModel.clear()
            Snackbar.make(
                requireView(),
                "삭제 완료",
                Snackbar.LENGTH_SHORT
            ).show()
        }
        builder.setNegativeButton("No") { _, _ -> }
        builder.setTitle("연락처 삭제")
        builder.setMessage("연락처 정보를 삭제하시겠습니까?")
        builder.create().show()
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

    private fun checkPermissionsAndStart(permissions: Array<out String>) {
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
    private fun checkNeedPermissionBoolean(permissions: Array<out String>): Boolean {
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
                it.value == true
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