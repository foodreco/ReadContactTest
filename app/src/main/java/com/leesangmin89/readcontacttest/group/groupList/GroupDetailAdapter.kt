package com.leesangmin89.readcontacttest.group.groupList

import android.annotation.SuppressLint
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.provider.CallLog
import android.util.Log
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.core.util.set
import androidx.fragment.app.FragmentManager
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.leesangmin89.readcontacttest.R
import com.leesangmin89.readcontacttest.convertCallTypeToString
import com.leesangmin89.readcontacttest.convertLongToDateString
import com.leesangmin89.readcontacttest.convertLongToTimeString
import com.leesangmin89.readcontacttest.customDialog.EditCallContent
import com.leesangmin89.readcontacttest.customDialog.UpdateDialog
import com.leesangmin89.readcontacttest.data.entity.CallLogData
import com.leesangmin89.readcontacttest.data.entity.ContactBase
import com.leesangmin89.readcontacttest.databinding.FragmentCallLogChildBinding
import com.leesangmin89.readcontacttest.databinding.GroupDetailChildBinding

class GroupDetailAdapter(fragmentManager: FragmentManager) :
    ListAdapter<CallLogData, GroupDetailAdapter.CallHolder>(CallLogDiffCallback()) {

    private var mFragmentManager: FragmentManager = fragmentManager
    private val expandStatus = SparseBooleanArray()

    inner class CallHolder constructor(private val binding: GroupDetailChildBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(item: CallLogData, num: Int, fragmentManager: FragmentManager) {

            binding.logCallType.text = convertCallTypeToString(item.callType!!.toInt())
            binding.logDuration.text = convertLongToTimeString(item.duration!!.toLong())
            binding.logDate.text = convertLongToDateString(item.date!!.toLong())
            if (item.callKeyword == "") {
                binding.callKeyword.text = "키워드없음"
            } else {
                binding.callKeyword.text = "통화키워드 : ${item.callKeyword}"
            }
            if (item.callContent == "") {
                binding.callLogContentText.text = "메모없음"
            } else {
                binding.callLogContentText.text = item.callContent
            }

            // expandable layout 코드
            if (expandStatus[num]) {
                binding.layoutExpand.visibility = View.VISIBLE
            } else {
                binding.layoutExpand.visibility = View.GONE
            }

            binding.callLogChild.setOnClickListener {
                expandStatus[num] = !expandStatus[num]
                notifyDataSetChanged()
            }

            // layoutExpand 터치 시, EditCallContent Dialog show
            binding.layoutExpand.setOnClickListener {
                // EditCallContent Dialog 띄우고, CallLogData 넘겨줌
                // 해당 전화번호를 넘겨주는 코드
                val bundle = bundleOf()
                val list: CallLogData = item
                bundle.putParcelable("callLogData", list)
                val dialog = EditCallContent()
                dialog.arguments = bundle
                dialog.show(fragmentManager, "EditCallContentDialog")
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CallHolder {
        val binding =
            GroupDetailChildBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CallHolder(binding)
    }

    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    override fun onBindViewHolder(holder: CallHolder, position: Int) {
        holder.bind(getItem(position), position, mFragmentManager)
    }
}


class CallLogDiffCallback : DiffUtil.ItemCallback<CallLogData>() {
    override fun areItemsTheSame(oldItem: CallLogData, newItem: CallLogData): Boolean {
        return oldItem.number == newItem.number
    }

    override fun areContentsTheSame(oldItem: CallLogData, newItem: CallLogData): Boolean {
        return oldItem == newItem
    }

}