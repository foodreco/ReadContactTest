package com.leesangmin89.readcontacttest

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.leesangmin89.readcontacttest.databinding.FragmentListBinding


class ListFragment : Fragment() {

    lateinit var binding: FragmentListBinding

    // 연락처 허용 코드
    private val CONTACT_PERMISSION_CODE = 1

    // 연락처 선택 코드
    private val CONTACT_PICK_CODE = 2

    @SuppressLint("Range")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentListBinding.inflate(layoutInflater, container, false)

        binding.button.setOnClickListener {
            // 권한 허용 여부 확인
            if (checkContactPermission()) {
                // 허용 시
                loadContact()
            } else {
                // 거절 시, 허용 요청
                requestContactPermission()
            }
        }
        return binding.root
    }

    @SuppressLint("Range")
    fun loadContact() {
        val contactList: MutableList<ContactDTO> = ArrayList()
        val contacts = requireActivity().contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            null
        )
        while (contacts!!.moveToNext()) {
            val name =
                contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            val number =
                contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            val photo_uri =
                contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI))
            val obj = ContactDTO()
            obj.name = name
            obj.number = number
            if (photo_uri != null) {
                obj.image = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, Uri.parse(photo_uri))
            }

            contactList.add(obj)
        }

        val adapter = ContactAdapter(requireContext())

        binding.recyclerViewList.adapter = adapter
        adapter.submitList(contactList)

        contacts.close()
    }

    private fun checkContactPermission(): Boolean {
        // 권한 허용 여부 확인
        // 허용되었으면 true 반환
        return ContextCompat.checkSelfPermission(
            requireContext(), android.Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestContactPermission() {
        // READ_CONTACT 허용 요청 함수
        val permission = arrayOf(android.Manifest.permission.READ_CONTACTS)
        ActivityCompat.requestPermissions(requireActivity(), permission, CONTACT_PERMISSION_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // 권한 허용 시, pickContact() 실행
        if (requestCode == CONTACT_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadContact()
            } else {
                Toast.makeText(context, "허용 거부되었습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

}