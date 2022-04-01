package com.leesangmin89.readcontacttest.group.groupDetail

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
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.leesangmin89.readcontacttest.*
import com.leesangmin89.readcontacttest.callLog.CallLogItemDiffCallback
import com.leesangmin89.readcontacttest.customDialog.EditCallContent
import com.leesangmin89.readcontacttest.data.entity.CallLogData
import com.leesangmin89.readcontacttest.databinding.CallLogHeaderBinding
import com.leesangmin89.readcontacttest.databinding.GroupDetailChildBinding

class GroupDetailAdapter(context: Context, fragmentManager: FragmentManager) :
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
            CallLogItem.Header.VIEW_TYPE -> GroupDetailHeaderViewHolder.from(parent)
            CallLogItem.Item.VIEW_TYPE -> GroupDetailItemViewHolder.from(parent)
            else -> throw IllegalArgumentException("Cannot create ViewHolder for view type: $viewType")
        }
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        when (viewHolder) {
            is GroupDetailHeaderViewHolder -> {
                val item = getItem(position) as CallLogItem.Header
                viewHolder.bind(item)
            }
            is GroupDetailItemViewHolder -> {
                val item = getItem(position) as CallLogItem.Item
                val callLogData = item.callLog
                viewHolder.bind(item, position, mFragmentManager, mContext)
                setUpImageWithConvertCallType(viewHolder.imgCallType, callLogData.callType!!.toInt(),mContext)

                importanceStatus[position] = callLogData.importance == true

                if (importanceStatus[position]) {
                    with(viewHolder.btnImportance) {
                        setImageResource(R.drawable.ic_baseline_star_yellow_50)
                        setColorFilter(
                            ContextCompat.getColor(
                                mContext,
                                R.color.yellow
                            )
                        )
                    }
                } else {
                    with(viewHolder.btnImportance) {
                        setImageResource(R.drawable.ic_baseline_star_yellow_50)
                        setColorFilter(
                            ContextCompat.getColor(
                                mContext,
                                R.color.light_gray
                            )
                        )
                    }
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
    class GroupDetailHeaderViewHolder constructor(private val binding: CallLogHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): GroupDetailHeaderViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = CallLogHeaderBinding.inflate(layoutInflater, parent, false)
                return GroupDetailHeaderViewHolder(binding)
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
    class GroupDetailItemViewHolder constructor(private val binding: GroupDetailChildBinding) :
        RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): GroupDetailItemViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = GroupDetailChildBinding.inflate(layoutInflater, parent, false)
                return GroupDetailItemViewHolder(binding)
            }
        }

        val imgCallType = binding.imgCallType
        val btnImportance = binding.btnImportance

        fun bind(item: CallLogItem, num: Int, fragmentManager: FragmentManager, context: Context) {
            val callLogData = (item as CallLogItem.Item).callLog
            binding.apply {
                if (callLogData.callKeyword == "") {
                    logKeyword.text = "키워드 없음"
                } else {
                    logKeyword.text = callLogData.callKeyword
                }
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