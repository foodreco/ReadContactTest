package com.leesangmin89.readcontacttest.list

import android.R.attr
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.*
import com.leesangmin89.readcontacttest.data.dao.CallLogDao
import com.leesangmin89.readcontacttest.data.dao.ContactDao
import com.leesangmin89.readcontacttest.data.dao.GroupListDao
import com.leesangmin89.readcontacttest.data.entity.ContactBase
import com.leesangmin89.readcontacttest.util.ContactBaseItem
import com.leesangmin89.readcontacttest.util.transformingToInitialSpell
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ListViewModel @Inject constructor(
    private val database: ContactDao,
    private val dataGroup: GroupListDao,
    private val dataCall : CallLogDao,
    application: Application
) : AndroidViewModel(application) {

    private val app = application

    private var _initializeContactEvent = MutableLiveData<Boolean>()
    val initializeContactEvent: LiveData<Boolean> = _initializeContactEvent

    private val _contactBaseItemData = MutableLiveData<List<ContactBaseItem>>()
    val contactBaseItemData: LiveData<List<ContactBaseItem>> = _contactBaseItemData

    lateinit var testData: LiveData<List<ContactBase>>

    init {
        initSortId()
    }

    fun getContactBaseLiveData(): LiveData<List<ContactBase>> {
        return database.getAllContactBaseFlow().asLiveData()
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

    fun makeList(contactBase: List<ContactBase>) {
        val listItems = contactBase.toListItems()
        _contactBaseItemData.postValue(listItems)
    }

    // DB 에서 가져온 리스트 가공 (미리 날짜별로 정렬한 리스트를 가져와야 함)
    private fun List<ContactBase>.toListItems(): List<ContactBaseItem> {
        val result = arrayListOf<ContactBaseItem>() // 결과를 리턴할 리스트
        var listHeaderText = "" // 리스트 초성
        this.forEach { contactBase ->
            // 초성이 달라지면 헤더로 추가
            if (listHeaderText != transformingToInitialSpell(contactBase.name)) {
                result.add(ContactBaseItem.Header(contactBase))
            }
            // entity 데이터 추가
            result.add(ContactBaseItem.Item(contactBase))

            // 헤더값 설정
            listHeaderText = transformingToInitialSpell(contactBase.name)
        }
        return result
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
                        update(updateList)
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
                if(Build.VERSION.SDK_INT < 28) {
                    contactList.image = MediaStore.Images.Media.getBitmap(
                        activity.contentResolver,
                        Uri.parse(photoUri)
                    )
                } else {
                    val source = ImageDecoder.createSource(activity.contentResolver, Uri.parse(photoUri))
                    val bitmap = ImageDecoder.decodeBitmap(source)
                    contactList.image = bitmap
                }
            }
            insert(contactList)
        }
        contacts.close()

        // 연락처를 다시 가져올 때마다, 연락처-그룹 동기화 하는 코드
        updateGroupNameInContactBase()
    }

    fun sortByCallLog() : LiveData<List<String>> {
        return dataCall.getAllCallLogDataFlow().asLiveData()
    }

    // 통화이력 중 전화번호를 numberList 로 받아 해당 번호를 연락처에서 가져오는 함수
    fun checkCallLogData(numberList : List<String>) {
        viewModelScope.launch {
            val callLogNumberList = numberList.distinct()
            val newContactBaseList = mutableListOf<ContactBase>()
            for (number in callLogNumberList) {
                val eachContact = database.getContact(number)
                if (eachContact != null) {
                    newContactBaseList.add(eachContact)
                }
            }
            makeList(newContactBaseList)
        }
    }


}