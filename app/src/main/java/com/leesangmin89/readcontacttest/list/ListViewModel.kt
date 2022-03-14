package com.leesangmin89.readcontacttest.list

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.net.Uri
import android.provider.ContactsContract
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.*
import com.leesangmin89.readcontacttest.data.entity.ContactBase
import com.leesangmin89.readcontacttest.data.dao.ContactDao
import com.leesangmin89.readcontacttest.data.dao.GroupListDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListViewModel @Inject constructor(
    private val database: ContactDao,
    private val dataGroup: GroupListDao,
    application: Application
) : AndroidViewModel(application) {

    private var _initializeContactEvent = MutableLiveData<Boolean>()
    val initializeContactEvent: LiveData<Boolean> = _initializeContactEvent

    lateinit var testData: LiveData<List<ContactBase>>

    init {
        initSortId()
    }

    fun initSortId() {
        testData = database.getAllDataByIdASCLive()
    }

    fun insert(contact: ContactBase) {
        viewModelScope.launch {
            database.insert(contact)
        }
    }

    fun clear() {
        viewModelScope.launch {
            database.clear()
        }
    }

    fun delete(contact: ContactBase) {
        viewModelScope.launch {
            database.delete(contact)
        }
    }

    fun update(contact: ContactBase) {
        viewModelScope.launch {
            database.update(contact)
        }
    }

    fun contactActivate() {
        _initializeContactEvent.value = true
    }

    fun searchDatabase(searchQuery: String): LiveData<List<ContactBase>> {
        return database.searchDatabase(searchQuery).asLiveData()
    }

    private fun updateGroupNameInContactBase() {
        viewModelScope.launch {
            for (contact in database.getAllContactBaseList()) {
                for (groupList in dataGroup.getAllDataFromGroupList()) {
                    if (contact.number == groupList.number) {
                        val updateList = ContactBase(
                            contact.name,
                            contact.number,
                            groupList.group,
                            contact.image,
                            contact.id
                        )
                        database.update(updateList)
                    }
                }
            }
            _initializeContactEvent.value = false
        }
    }

    @SuppressLint("Range")
    fun updateContactBase(activity: Activity) {
        // 기존 데이터 삭제
        clear()
        val contacts = activity.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            null
        )

        while (contacts!!.moveToNext()) {
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
                    activity.contentResolver,
                    Uri.parse(photoUri)
                )
//                    val source = ImageDecoder.createSource(requireActivity().contentResolver, photoUri)
//                    val bitmap = ImageDecoder.decodeBitmap(source)
            }
            insert(contactList)
        }
        contacts.close()

        // 연락처를 다시 가져올 때마다, 연락처-그룹 동기화 하는 코드
        updateGroupNameInContactBase()
    }

}