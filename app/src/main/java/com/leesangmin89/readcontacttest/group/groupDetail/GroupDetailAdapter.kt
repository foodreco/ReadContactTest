package com.leesangmin89.readcontacttest.group.groupDetail

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
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
import com.leesangmin89.readcontacttest.databinding.GroupDetailChildBinding

class GroupDetailAdapter(context: Context, fragmentManager: FragmentManager) :
    ListAdapter<CallLogData, GroupDetailAdapter.CallHolder>(CallLogDiffCallback()) {

    private var mFragmentManager: FragmentManager = fragmentManager
    private val expandStatus = SparseBooleanArray()
    private val importanceStatus = SparseBooleanArray()
    private val mContext = context

    private val _callLogImportanceSetting = MutableLiveData<CallLogData?>()
    val callLogImportanceSetting : LiveData<CallLogData?> = _callLogImportanceSetting

    private val _callLogImportanceRemoving = MutableLiveData<CallLogData?>()
    val callLogImportanceRemoving : LiveData<CallLogData?> = _callLogImportanceRemoving


    inner class CallHolder constructor(private val binding: GroupDetailChildBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CallLogData, num: Int, fragmentManager: FragmentManager) {

            Log.i("수정", "통화 메모 UI 개선")
//            통화일자 헤더로 넣기
//            메모표시로 expandable?

            setUpImageWithConvertCallType(binding.imgCallType, item.callType!!.toInt(), mContext)
            binding.logDuration.text = convertLongToTimeString(item.duration!!.toLong())
            binding.logDate.text = convertLongToDateString(item.date!!.toLong())
            if (item.callKeyword == "") {
                binding.callKeyword.text = "키워드없음"
            } else {
                binding.callKeyword.text = "${item.callKeyword}"
            }
            if (item.callContent == "") {
                binding.callLogContentText.text = "메모없음"
            } else {
                binding.callLogContentText.text = item.callContent
            }

            importanceStatus[num] = item.importance == true

            if (importanceStatus[num]) {
                binding.btnImportance.setImageResource(R.drawable.ic_baseline_star_yellow_50)
                binding.btnImportance.setColorFilter(ContextCompat.getColor(mContext, R.color.yellow))
            } else {
                binding.btnImportance.setImageResource(R.drawable.ic_baseline_star_yellow_50)
                binding.btnImportance.setColorFilter(ContextCompat.getColor(mContext, R.color.light_gray))
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

            // 별 터치 시 작동 코드
            binding.btnImportance.setOnClickListener {
                if (item.importance == true) {
                    _callLogImportanceSetting.value = item
                } else {
                    _callLogImportanceRemoving.value = item
                }
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

    fun importanceReset() {
        _callLogImportanceSetting.value = null
        _callLogImportanceRemoving.value = null
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