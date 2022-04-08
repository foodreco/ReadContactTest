package com.leesangmin89.readcontacttest.callLog

import android.content.Context
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.util.set
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.leesangmin89.readcontacttest.*
import com.leesangmin89.readcontacttest.customDialog.EditCallContent
import com.leesangmin89.readcontacttest.data.entity.CallLogData
import com.leesangmin89.readcontacttest.databinding.CallLogHeaderBinding
import com.leesangmin89.readcontacttest.databinding.FragmentCallLogChildBinding
import com.leesangmin89.readcontacttest.util.CallLogItem
import com.leesangmin89.readcontacttest.util.convertLongToDateString
import com.leesangmin89.readcontacttest.util.convertLongToTimeString
import com.leesangmin89.readcontacttest.util.setUpImageWithConvertCallType

class CallLogAdapter(context: Context, fragmentManager: FragmentManager) :
    ListAdapter<CallLogItem, RecyclerView.ViewHolder>(CallLogItemDiffCallback()) {

    var mFragmentManager: FragmentManager = fragmentManager
    private val mContext = context

    private val importanceStatus = SparseBooleanArray()
    private val _callLogImportanceSetting = MutableLiveData<CallLogData?>()
    val callLogImportanceSetting: LiveData<CallLogData?> = _callLogImportanceSetting
    private val _callLogImportanceRemoving = MutableLiveData<CallLogData?>()
    val callLogImportanceRemoving: LiveData<CallLogData?> = _callLogImportanceRemoving

    override fun getItemViewType(position: Int): Int = getItem(position).layoutId

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return when (viewType) {
            CallLogItem.Header.VIEW_TYPE -> CallLogHeaderViewHolder.from(parent)
            CallLogItem.Item.VIEW_TYPE -> CallLogItemViewHolder.from(parent)
            else -> throw IllegalArgumentException("Cannot create ViewHolder for view type: $viewType")
        }
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        when (viewHolder) {
            is CallLogHeaderViewHolder -> {
                val item = getItem(position) as CallLogItem.Header
                viewHolder.bind(item)
            }
            is CallLogItemViewHolder -> {
                val item = getItem(position) as CallLogItem.Item
                val callLogData = item.callLog
                viewHolder.bind(item, position, mFragmentManager, mContext)
                setUpImageWithConvertCallType(
                    viewHolder.imgCallType,
                    callLogData.callType!!.toInt(),
                    mContext
                )

                importanceStatus[position] = callLogData.importance == true

                if (importanceStatus[position]) {
                    viewHolder.btnImportance.setImageResource(R.drawable.ic_baseline_star_yellow_50)
                    viewHolder.btnImportance.setColorFilter(
                        ContextCompat.getColor(
                            mContext,
                            R.color.yellow
                        )
                    )
                } else {
                    viewHolder.btnImportance.setImageResource(R.drawable.ic_baseline_star_yellow_50)
                    viewHolder.btnImportance.setColorFilter(
                        ContextCompat.getColor(
                            mContext,
                            R.color.light_gray
                        )
                    )
                }

                // 별 터치 시 작동 코드
                viewHolder.btnImportance.setOnClickListener {
                    if (callLogData.importance == true) {
                        _callLogImportanceSetting.value = callLogData
                    } else {
                        _callLogImportanceRemoving.value = callLogData
                    }
                }
            }
        }
    }

    // 헤더용 뷰홀더
    class CallLogHeaderViewHolder constructor(private val binding: CallLogHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): CallLogHeaderViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = CallLogHeaderBinding.inflate(layoutInflater, parent, false)
                return CallLogHeaderViewHolder(binding)
            }
        }

        fun bind(item: CallLogItem) {
            val callLogData = (item as CallLogItem.Header).callLog
            binding.apply {
                textDate.text = callLogData.date?.let { convertLongToDateString(it.toLong()) }
            }
        }
    }

    // 리스트용 뷰홀더
    class CallLogItemViewHolder constructor(private val binding: FragmentCallLogChildBinding) :
        RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): CallLogItemViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = FragmentCallLogChildBinding.inflate(layoutInflater, parent, false)
                return CallLogItemViewHolder(binding)
            }
        }

        val imgCallType = binding.imgCallType
        val btnImportance = binding.btnImportance

        fun bind(item: CallLogItem, num: Int, fragmentManager: FragmentManager, context: Context) {
            val callLogData = (item as CallLogItem.Item).callLog
            binding.apply {
                logName.text = callLogData.name
                logDuration.text = convertLongToTimeString(callLogData.duration!!.toLong())

                // layoutExpand 터치 시
                callLogChild.setOnClickListener {
                    // EditCallContent Dialog 띄우고, CallLogData 넘겨줌
                    // 해당 전화번호를 넘겨주는 코드
                    val bundle = bundleOf()
                    val list: CallLogData = callLogData
                    bundle.putParcelable("callLogData", list)
                    val dialog = EditCallContent()
                    dialog.arguments = bundle
                    dialog.show(fragmentManager, "EditCallContentDialog")
                }
                if (callLogData.callKeyword == "" && callLogData.callContent == "") {
                    imgMemo.visibility = View.INVISIBLE
                } else {
                    imgMemo.visibility = View.VISIBLE
                }
            }
        }
    }

    fun importanceReset() {
        _callLogImportanceSetting.value = null
        _callLogImportanceRemoving.value = null
    }
}

class CallLogItemDiffCallback : DiffUtil.ItemCallback<CallLogItem>() {
    override fun areItemsTheSame(oldItem: CallLogItem, newItem: CallLogItem): Boolean {
        return oldItem.layoutId == newItem.layoutId
    }

    override fun areContentsTheSame(oldItem: CallLogItem, newItem: CallLogItem): Boolean {
        return oldItem == newItem
    }

}