package com.leesangmin89.readcontacttest.list

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
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
import androidx.compose.animation.core.animateDpAsState
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.leesangmin89.readcontacttest.data.entity.ContactBase
import com.leesangmin89.readcontacttest.R
import com.leesangmin89.readcontacttest.databinding.FragmentListBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ListFragment : Fragment(), SearchView.OnQueryTextListener {

    private val binding by lazy { FragmentListBinding.inflate(layoutInflater) }
    private val listViewModel: ListViewModel by viewModels()

    //    private val adapter: ContactAdapter by lazy { ContactAdapter(requireContext()) }
    private val adapter: ContactAdapterTest by lazy { ContactAdapterTest(requireContext()) }

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

        // 초기화 버튼 클릭 시, 연락처 데이터를 다시 가져오는 코드
        binding.button.setOnClickListener {
            checkAndStart()
        }

        // 리싸이클러뷰 옵저버
        // 정렬기능 추가 (구조 개선 필요!! 처음 로드 시 앱 데드)
        Log.i("보완", "정렬 및 recyclerView 위치")
        listViewModel.sortEvent.observe(viewLifecycleOwner,
            {
                Log.i("수정", "초기화 직후 group 반영되지 않음")
                Log.i("수정", "adapter.submitList(it) 후에 작동됨...")
                // 연락처를 다시 가져올 때마다, 연락처-그룹 동기화 하는 코드
                listViewModel.updateGroupNameInContactBase()
                when (it) {
                    0 -> {
                        listViewModel.listData.observe(viewLifecycleOwner, {
//                            adapter.submitList(it)
                            adapter.setData(it)
                        })
                    }
                    1 -> {
                        listViewModel.listDataNameDESC.observe(viewLifecycleOwner, {
//                            adapter.submitList(it)
                            adapter.setData(it)
                        })
                    }
                    2 -> {
                        listViewModel.listDataNumberASC.observe(viewLifecycleOwner, {
//                            adapter.submitList(it)
                            adapter.setData(it)
                        })
                    }
                }
            })

        // 최초 데이터 불러오기 옵저버
        listViewModel.initializeContactEvent.observe(viewLifecycleOwner,
            {
                if (it) {
                    // 연락처 가져오기
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

        // 체크박스 테스트
        binding.switchCheckboxTest.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                adapter.onCheckBox(1)
            } else {
                adapter.onCheckBox(0)
            }
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
//                adapter.submitList(it)
                adapter.setData(it)
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
//            R.id.sort_by_name_desc -> {
//                listViewModel.getAllDataByDESC()
//                true
//            }
            R.id.sory_by_number -> {
                listViewModel.getAllDataByNumberASD()
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
        builder.setMessage("연락처 정보를 초기화하시겠습니까?")
        builder.create().show()
    }

    // 초기 데이터 로드 함수(ContactBase)
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
            val photo: Bitmap?
            val name =
                contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            // 번호 수집 시, - 일괄 제거하여 수집한다.
            val number =
                contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                    .replace("-", "")
            val photoUri =
                contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI))
            val contactList = ContactBase(name, number, "", null, 0)

            if (photoUri != null) {
                Log.i("수정", "getBitmap -> ImageDecoder 변경시도 but 오류발생")
                contactList.image = MediaStore.Images.Media.getBitmap(
                    requireActivity().contentResolver,
                    Uri.parse(photoUri)
                )
//                    val source = ImageDecoder.createSource(requireActivity().contentResolver, photoUri)
//                    val bitmap = ImageDecoder.decodeBitmap(source)
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