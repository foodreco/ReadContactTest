package com.leesangmin89.readcontacttest.list

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import com.leesangmin89.readcontacttest.data.entity.ContactBase
import com.leesangmin89.readcontacttest.R
import com.leesangmin89.readcontacttest.databinding.FragmentListBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ListFragment : Fragment(), SearchView.OnQueryTextListener {

    private val binding by lazy { FragmentListBinding.inflate(layoutInflater) }
    private val listViewModel: ListViewModel by viewModels()

    private val adapter: ContactAdapter by lazy {
        ContactAdapter(
            requireContext(),
            childFragmentManager
        )
    }


    // 권한 허용 리스트
    val permissions = arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.CALL_PHONE)

    // 권한 허용 코드
    private val CONTACT_AND_CALL_PERMISSION_CODE = 1

    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("Range")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding.recyclerViewList.adapter = adapter

        // 초기화 버튼 클릭 시, 연락처 데이터를 다시 가져오는 코드
        binding.button.setOnClickListener {
            checkAndStart()
        }

//        // 리싸이클러뷰 옵저버
//        // 정렬기능 추가 (구조 개선 필요!! 처음 로드 시 앱 데드)
//        listViewModel.sortEvent.observe(viewLifecycleOwner,
//            {
//                // 연락처를 다시 가져올 때마다, 연락처-그룹 동기화 하는 코드
//                listViewModel.updateGroupNameInContactBase()
//                when (it) {
//                    0 -> {
//                        listViewModel.listData.observe(viewLifecycleOwner, {
////                            adapter.submitList(it)
//                            adapter.setData(it)
//                        })
//                    }
//                    1 -> {
//                        listViewModel.listDataNameDESC.observe(viewLifecycleOwner, {
////                            adapter.submitList(it)
//                            adapter.setData(it)
//                        })
//                    }
//                    2 -> {
//                        listViewModel.listDataNumberASC.observe(viewLifecycleOwner, {
////                            adapter.submitList(it)
//                            adapter.setData(it)
//                        })
//                    }
//                }
//            })

        // 리싸이클러뷰 옵저버
        // 정렬기능 추가 (구조 개선 필요!! 처음 로드 시 앱 데드)
        Log.i("수정", "bottomNavi 터치 시, 정렬 초기화 오류")
        Log.i("보완", "통화기록이 있는 연락처만 불러오기 정렬 -> 그룹 추가 용이")
        listViewModel.testData.observe(viewLifecycleOwner, {
            adapter.submitList(it)
            Log.i("확인", "listViewModel.listData.observe 발동")
        })

        // 연락처 업데이트 옵저버
        listViewModel.initializeContactEvent.observe(viewLifecycleOwner,
            {
                if (it) {
                    updateContactBase()
                }
            })

        // 리싸이클러뷰 스크롤을 최상단으로 하는 버튼
        binding.btnScrollUp.setOnClickListener {
            binding.recyclerViewList.scrollToPosition(0)
        }

        setHasOptionsMenu(true)
        return binding.root
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu, menu)

        val search = menu?.findItem(R.id.menu_search)
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
        listViewModel.searchDatabase(searchQuery).observe(this, { list ->
            list.let {
                adapter.submitList(it)
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.delete_menu -> {
                deleteData()
                true
            }
            R.id.sort_by_name_asc -> {
                listViewModel.getAllDataByASC()
                true
            }
            R.id.sort_by_name_desc -> {
                listViewModel.getAllDataByDESC()
                true
            }
            R.id.sory_by_number -> {
                listViewModel.getAllDataByNumberASC()
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
            Snackbar.make(requireView(), "접근 허용됨", Snackbar.LENGTH_SHORT).show()
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

    private fun checkAndStart() {
        // 권한 허용 여부 확인
        if (checkNeedPermission()) {
            // 허용 시
            activatingContact()
        } else {
            requestContactPermission()
        }
    }

    private fun checkNeedPermission(): Boolean {
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

    private fun requestContactPermission() {
        // READ_CONTACT 허용 요청 함수
        ActivityCompat.requestPermissions(
            requireActivity(),
            permissions,
            CONTACT_AND_CALL_PERMISSION_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == CONTACT_AND_CALL_PERMISSION_CODE) {
            var check = true

            for (grant in grantResults) {
                if (grant != PackageManager.PERMISSION_GRANTED) {
                    check = false
                    break
                }
            }
            if (check) activatingContact()
            else {
                Toast.makeText(requireContext(), "허용 거부되었습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

}