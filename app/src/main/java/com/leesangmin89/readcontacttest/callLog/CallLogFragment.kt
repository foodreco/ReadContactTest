package com.leesangmin89.readcontacttest.callLog

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.provider.CallLog
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.fragment.app.viewModels
import com.leesangmin89.readcontacttest.R
import com.leesangmin89.readcontacttest.data.entity.CallLogData
import com.leesangmin89.readcontacttest.databinding.FragmentCallLogBinding
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat

@AndroidEntryPoint
class CallLogFragment : Fragment() {

    private val binding by lazy { FragmentCallLogBinding.inflate(layoutInflater) }
    private val callLogViewModel: CallLogViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val adapter = CallLogAdapter()
        binding.callLogRecyclerView.adapter = adapter

        showProgress(true)

        callLogViewModel.callLogList.observe(viewLifecycleOwner, {
            adapter.submitList(it)
            showProgress(false)
        })

        return binding.root
    }

    fun showProgress(show:Boolean) {
        binding.callLogProgressBar.visibility = if(show) View.VISIBLE else View.GONE
    }
}