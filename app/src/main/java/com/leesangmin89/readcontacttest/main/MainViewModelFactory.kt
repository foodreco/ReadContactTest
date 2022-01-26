package com.leesangmin89.readcontacttest.main

import android.app.Application
import android.widget.ListView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.leesangmin89.readcontacttest.data.ContactInfoDao
import java.lang.IllegalArgumentException
import javax.sql.DataSource

class MainViewModelFactory(
    private val dataSource: ContactInfoDao,
    private val application: Application
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(dataSource, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}