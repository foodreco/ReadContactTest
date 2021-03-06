package com.leesangmin89.readcontacttest.list

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.leesangmin89.readcontacttest.data.ContactBase
import com.leesangmin89.readcontacttest.R
import com.leesangmin89.readcontacttest.data.ContactDatabase
import com.leesangmin89.readcontacttest.databinding.FragmentListBinding
import com.leesangmin89.readcontacttest.update.UpdateFragmentArgs
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ListFragment : Fragment(), SearchView.OnQueryTextListener {

    private val args by navArgs<ListFragmentArgs>()
    private val binding by lazy { FragmentListBinding.inflate(layoutInflater) }
    private val listViewModel: ListViewModel by viewModels()
    private val adapter : ContactAdapter by lazy { ContactAdapter(requireContext()) }

    // 권한 허용 리스트
    val permissions = arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.CALL_PHONE)

    // 권한 허용 코드
    private val CONTACT_AND_CALL_PERMISSION_CODE = 1


    @SuppressLint("Range")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding.recyclerViewList.adapter = adapter

        //리싸이클러뷰 스크롤 관련 코드 : 작동 안됨??
//        val smoothScroller : RecyclerView.SmoothScroller by lazy {
//            object : LinearSmoothScroller(context) {
//                override fun getVerticalSnapPreference() = SNAP_TO_START
//            }
//        }
//        smoothScroller.targetPosition = args.currentItemId
//        binding.recyclerViewList.layoutManager?.startSmoothScroll(smoothScroller)


        // 리싸이클러뷰 이전 위치 유지하는 코드
        // 작동안함??
//        adapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY

        binding.button.setOnClickListener {
            checkAndStart()
        }

        // 리싸이클러뷰 옵저버
        // 정렬기능 추가 (구조 개선 필요!! 처음 로드 시 앱 데드)
        listViewModel.sortEvent.observe(viewLifecycleOwner,
            {
                when (it) {
                    0 -> {
                        listViewModel.listData.observe(viewLifecycleOwner, {
                            adapter.submitList(it)
//                            binding.recyclerViewList.scrollToPosition(0)
                        })
                    }
                    1 -> {
                        listViewModel.listDataNameDESC.observe(viewLifecycleOwner, {
                            adapter.submitList(it)
//                            binding.recyclerViewList.scrollToPosition(0)
                        })
                    }
                    2 -> {
                        listViewModel.listDataNumberASC.observe(viewLifecycleOwner, {
                            adapter.submitList(it)
//                            binding.recyclerViewList.scrollToPosition(0)

                        })
                    }
                }
                Log.i("확인", "adapter.submitList(list)")
            })

        // 최초 데이터 불러오기 옵저버
        listViewModel.initializeContactEvent.observe(viewLifecycleOwner,
            {
                if (it) {
                    loadContact()
                }
            })

        // Add 버튼 클릭 시
        binding.floatingActionButtonAdd.setOnClickListener {
            findNavController().navigate(
                ListFragmentDirections.actionListFragmentToAddFragment()
            )
        }

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
                Log.i("확인", "sort_by_name_asc")
                true
            }
//            R.id.sort_by_name_desc -> {
//                listViewModel.getAllDataByDESC()
//                Log.i("확인", "sort_by_name_desc")
//                true
//            }
            R.id.sory_by_number -> {
                listViewModel.getAllDataByNumberASD()
                Log.i("확인", "sort_by_number")
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
                "포맷 완료",
                Snackbar.LENGTH_SHORT
            ).show()
        }
        builder.setNegativeButton("No") { _, _ -> }
        builder.setTitle("포맷")
        builder.setMessage("전체 데이터를 삭제하시겠습니까?")
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
                "데이터 초기화됨",
                Toast.LENGTH_SHORT
            ).show()
        }
        builder.setNegativeButton("아니오") { _, _ -> }
        builder.setTitle("데이터 초기화")
        builder.setMessage("모든 정보를 초기화하시겠습니까?")
        builder.create().show()
    }

    // 초기 데이터 로드 함수
    @SuppressLint("Range")
    fun loadContact() {
        // 기존 데이터 삭제
        listViewModel.clear()

        val contacts = requireActivity().contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            null
        )

        while (contacts!!.moveToNext()) {
            val photo : Bitmap?
            val name =
                contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            val number =
                contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            val photo_uri =
                contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI))
            val contactList = ContactBase(name, number, "", null,0)
            if (photo_uri != null) {
                contactList.image = MediaStore.Images.Media.getBitmap(
                    requireActivity().contentResolver,
                    Uri.parse(photo_uri)
                )
            } else {
                contactList.image = null
            }
            listViewModel.insert(contactList)
        }

        contacts.close()
        listViewModel.contactInitCompleted()
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