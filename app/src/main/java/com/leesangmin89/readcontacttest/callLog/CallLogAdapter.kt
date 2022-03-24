package com.leesangmin89.readcontacttest.callLog

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
import com.leesangmin89.readcontacttest.databinding.CallLogHeaderBinding
import com.leesangmin89.readcontacttest.databinding.FragmentCallLogChildBinding

class CallLogAdapter(context: Context, fragmentManager: FragmentManager) :
    ListAdapter<CallLogItem, RecyclerView.ViewHolder>(CallLogItemDiffCallback()) {

    var mFragmentManager: FragmentManager = fragmentManager
    private val mContext = context

    private val expandStatus = SparseBooleanArray()
    private val importanceStatus = SparseBooleanArray()
    private val _callLogImportanceSetting = MutableLiveData<CallLogData?>()
    val callLogImportanceSetting: LiveData<CallLogData?> = _callLogImportanceSetting
    private val _callLogImportanceRemoving = MutableLiveData<CallLogData?>()
    val callLogImportanceRemoving: LiveData<CallLogData?> = _callLogImportanceRemoving

    override fun getItemViewType(position: Int): Int = getItem(position).layoutId

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        Log.i("수정", "통화 메모 UI 개선")

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
                setUpImageWithConvertCallType(viewHolder.imgCallType, callLogData.callType!!.toInt(),mContext)
                if (callLogData.callContent == "") {
                    viewHolder.callLogContentText.text = "메모없음"
                } else {
                    viewHolder.callLogContentText.text = callLogData.callContent
                }

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

//                // expandable layout 코드
//                if (expandStatus[position]) {
//                    viewHolder.layoutExpand.visibility = View.VISIBLE
//
//                } else {
//                    viewHolder.layoutExpand.visibility = View.GONE
//                }

                // 별 터치 시 작동 코드
                viewHolder.btnImportance.setOnClickListener {
                    if (callLogData.importance == true) {
                        _callLogImportanceSetting.value = callLogData
                    } else {
                        _callLogImportanceRemoving.value = callLogData
                    }
                }

//                // view 터치 시, expandable layout 확장
//                viewHolder.callLogChild.setOnClickListener {
//                    expandStatus[position] = !expandStatus[position]
//                    notifyDataSetChanged()
//                }
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

//        private val expandStatus = SparseBooleanArray()
//        private val importanceStatus = SparseBooleanArray()
//        private val _callLogImportanceSetting = MutableLiveData<CallLogData?>()
//        val callLogImportanceSetting: LiveData<CallLogData?> = _callLogImportanceSetting
//        private val _callLogImportanceRemoving = MutableLiveData<CallLogData?>()
//        val callLogImportanceRemoving: LiveData<CallLogData?> = _callLogImportanceRemoving

        companion object {
            fun from(parent: ViewGroup): CallLogItemViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = FragmentCallLogChildBinding.inflate(layoutInflater, parent, false)
                return CallLogItemViewHolder(binding)
            }
        }

        val imgCallType = binding.imgCallType
        val callLogContentText = binding.callLogContentText
        val btnImportance = binding.btnImportance
        val layoutExpand = binding.layoutExpand
        val callLogChild = binding.callLogChild

        fun bind(item: CallLogItem, num: Int, fragmentManager: FragmentManager, context: Context) {
            val callLogData = (item as CallLogItem.Item).callLog
            binding.apply {
                logName.text = callLogData.name
                logDuration.text = convertLongToTimeString(callLogData.duration!!.toLong())
                callLogContentText.text = callLogData.callContent

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
            }

            if (callLogData.callKeyword == "" && callLogData.callContent == "") {
                binding.imgMemo.visibility = View.INVISIBLE
            } else {
                binding.imgMemo.visibility = View.VISIBLE
            }

//            setUpImageWithConvertCallType(binding.imgCallType, callLogData.callType!!.toInt(),context)
//            if (callLogData.callContent == "") {
//                binding.callLogContentText.text = "메모없음"
//            } else {
//                binding.callLogContentText.text = callLogData.callContent
//            }
//
//            importanceStatus[num] = callLogData.importance == true
//
//            if (importanceStatus[num]) {
//                binding.btnImportance.setImageResource(R.drawable.ic_baseline_star_yellow_50)
//                binding.btnImportance.setColorFilter(ContextCompat.getColor(context, R.color.yellow))
//            } else {
//                binding.btnImportance.setImageResource(R.drawable.ic_baseline_star_yellow_50)
//                binding.btnImportance.setColorFilter(ContextCompat.getColor(context, R.color.light_gray))
//            }
//
//            // expandable layout 코드
//            if (expandStatus[num]) {
//                binding.layoutExpand.visibility = View.VISIBLE
//            } else {
//                binding.layoutExpand.visibility = View.GONE
//            }
//
//            // 별 터치 시, 작동코드
//            binding.btnImportance.setOnClickListener {
//                if (callLogData.importance == true) {
//                    _callLogImportanceSetting.value = callLogData
//                } else {
//                    _callLogImportanceRemoving.value = callLogData
//                }
//            }
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




/////////////////////////////// 복사 //////////////////////////////////


//class CallLogAdapter(context: Context, fragmentManager: FragmentManager) :
//    ListAdapter<CallLogData, CallLogAdapter.CallHolder>(CallLogDiffCallback()) {
//
//    private var mFragmentManager: FragmentManager = fragmentManager
//    private val expandStatus = SparseBooleanArray()
//    private val importanceStatus = SparseBooleanArray()
//    private val mContext = context
//
//    private val _callLogImportanceSetting = MutableLiveData<CallLogData?>()
//    val callLogImportanceSetting : LiveData<CallLogData?> = _callLogImportanceSetting
//
//    private val _callLogImportanceRemoving = MutableLiveData<CallLogData?>()
//    val callLogImportanceRemoving : LiveData<CallLogData?> = _callLogImportanceRemoving
//
//
//    inner class CallHolder constructor(private val binding: FragmentCallLogChildBinding) :
//        RecyclerView.ViewHolder(binding.root) {
//        fun bind(item: CallLogData, num: Int, fragmentManager: FragmentManager) {
//
//            Log.i("수정", "통화 메모 UI 개선")
//            통화일자 헤더로 넣기
//            메모표시로 expandable?
//
//            binding.logName.text = item.name
//            setUpImageWithConvertCallType(binding.imgCallType, item.callType!!.toInt(), mContext)
//            binding.logDuration.text = convertLongToTimeString(item.duration!!.toLong())
//            binding.logDate.text = convertLongToDateString(item.date!!.toLong())
//            binding.callLogContentText.text = item.callContent
//            if (item.callContent == "") {
//                binding.callLogContentText.text = "메모없음"
//            } else {
//                binding.callLogContentText.text = item.callContent
//            }
//
//            importanceStatus[num] = item.importance == true
//
//            if (importanceStatus[num]) {
//                binding.btnImportance.setImageResource(R.drawable.ic_baseline_star_yellow_50)
//                binding.btnImportance.setColorFilter(ContextCompat.getColor(mContext, R.color.yellow))
//            } else {
//                binding.btnImportance.setImageResource(R.drawable.ic_baseline_star_yellow_50)
//                binding.btnImportance.setColorFilter(ContextCompat.getColor(mContext, R.color.light_gray))
//            }
//
//            // expandable layout 코드
//            if (expandStatus[num]) {
//                binding.layoutExpand.visibility = View.VISIBLE
//            } else {
//                binding.layoutExpand.visibility = View.GONE
//            }
//
//            // view 터치 시, expandable layout 확장
//            binding.callLogChild.setOnClickListener {
//                expandStatus[num] = !expandStatus[num]
//                notifyDataSetChanged()
//            }
//
//            // 별 터치 시 작동 코드
//            binding.btnImportance.setOnClickListener {
//                if (item.importance == true) {
//                    _callLogImportanceSetting.value = item
//                } else {
//                    _callLogImportanceRemoving.value = item
//                }
//            }
//
//            // layoutExpand 터치 시, EditCallContent Dialog show
//            binding.layoutExpand.setOnClickListener {
//                // EditCallContent Dialog 띄우고, CallLogData 넘겨줌
//                // 해당 전화번호를 넘겨주는 코드
//                val bundle = bundleOf()
//                val list: CallLogData = item
//                bundle.putParcelable("callLogData", list)
//                val dialog = EditCallContent()
//                dialog.arguments = bundle
//                dialog.show(fragmentManager, "EditCallContentDialog")
//            }
//        }
//    }
//
//    fun importanceReset() {
//        _callLogImportanceSetting.value = null
//        _callLogImportanceRemoving.value = null
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CallHolder {
//        val binding =
//            FragmentCallLogChildBinding.inflate(LayoutInflater.from(parent.context), parent, false)
//        return CallHolder(binding)
//    }
//
//    @SuppressLint("SimpleDateFormat", "SetTextI18n")
//    override fun onBindViewHolder(holder: CallHolder, position: Int) {
//        holder.bind(getItem(position), position, mFragmentManager)
//    }
//}
//
//class CallLogDiffCallback : DiffUtil.ItemCallback<CallLogData>() {
//    override fun areItemsTheSame(oldItem: CallLogData, newItem: CallLogData): Boolean {
//        return oldItem.number == newItem.number
//    }
//
//    override fun areContentsTheSame(oldItem: CallLogData, newItem: CallLogData): Boolean {
//        return oldItem == newItem
//    }
//
//}


