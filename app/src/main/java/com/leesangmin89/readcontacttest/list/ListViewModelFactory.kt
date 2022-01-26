package com.leesangmin89.readcontacttest.list

import android.app.Application
import android.widget.ListView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.leesangmin89.readcontacttest.data.ContactDao
import java.lang.IllegalArgumentException
import javax.sql.DataSource

class ListViewModelFactory(
    private val dataSource: ContactDao,
    private val application: Application
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ListViewModel::class.java)) {
            return ListViewModel(dataSource, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}