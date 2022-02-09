package com.leesangmin89.readcontacttest.callLog

import android.annotation.SuppressLint
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.leesangmin89.readcontacttest.R
import com.leesangmin89.readcontacttest.data.entity.CallLogData
import com.leesangmin89.readcontacttest.databinding.FragmentCallLogChildBinding

class CallLogAdapter : ListAdapter<CallLogData, CallHolder>(CallLogDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CallHolder {
        val binding =
            FragmentCallLogChildBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CallHolder(binding)
    }

    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    override fun onBindViewHolder(holder: CallHolder, position: Int) {
        val item = getItem(position)

        val simpleDateFormat = java.text.SimpleDateFormat("yyyy년MM월dd일")
//        val dateString = simpleDateFormat.format(date)

        holder.callName.text = item.name
        when (item.callType.toInt()) {
            1 -> holder.callType.text = "수신"
            2 -> holder.callType.text = "발신"
            3 -> holder.callType.text = "부재중"
        }

        val minutes = item.duration.toLong() / 60
        val seconds = item.duration.toLong() % 60

        when (item.duration.toLong()) {
            in 0..59 -> holder.callDuration.text = "${seconds}초"
            else -> holder.callDuration.text = "${minutes}분 ${seconds}초"
        }

        holder.callDate.text = simpleDateFormat.format(item.date.toLong())

//        //리싸이클러 터치 시, GroupListFragment 로 이동
//        holder.groupEachList.setOnClickListener {
//            // 이동 시, 그룹명을 args 로 넘겨준다.
//            val action = GroupFragmentDirections.actionGroupFragmentToGroupListFragment(item.groupName)
//            holder.groupEachList.findNavController().navigate(action)
//        }
    }
}

class CallHolder constructor(private val binding: FragmentCallLogChildBinding) :
    RecyclerView.ViewHolder(binding.root) {
    val callName = binding.logName
    val callType = binding.logCallType
    val callDuration = binding.logDuration
    val callDate = binding.logDate
}

class CallLogDiffCallback : DiffUtil.ItemCallback<CallLogData>() {
    override fun areItemsTheSame(oldItem: CallLogData, newItem: CallLogData): Boolean {
        return oldItem.number == newItem.number
    }

    override fun areContentsTheSame(oldItem: CallLogData, newItem: CallLogData): Boolean {
        return oldItem == newItem
    }

}