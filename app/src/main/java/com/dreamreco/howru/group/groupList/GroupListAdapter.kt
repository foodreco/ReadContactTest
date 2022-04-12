package com.dreamreco.howru.group.groupList

import android.annotation.SuppressLint
import android.content.Context
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.util.set
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dreamreco.howru.R
import com.dreamreco.howru.data.entity.GroupList
import com.dreamreco.howru.databinding.GroupListChildBinding
import com.dreamreco.howru.databinding.GroupListHeaderBinding
import com.dreamreco.howru.util.GroupTopData

class GroupListAdapter(ctx: Context, fragmentManager: FragmentManager) :
    ListAdapter<GroupItem, RecyclerView.ViewHolder>(GroupDiffCallback()) {

    private var mFragmentManager: FragmentManager = fragmentManager
    private var context: Context = ctx
    private var checkBoxControlNumber: Int = 0
    private val checkboxStatus = SparseBooleanArray()
    private val alarmBtnStatus = SparseBooleanArray()
    val checkBoxReturnList = mutableListOf<String>()

    private val _alarmNumberSetting = MutableLiveData<GroupList?>()
    val alarmNumberSetting: LiveData<GroupList?> = _alarmNumberSetting

    private val _alarmNumberRemoving = MutableLiveData<GroupList?>()
    val alarmNumberRemoving: LiveData<GroupList?> = _alarmNumberRemoving

    private val _deleteEventActive = MutableLiveData<Boolean?>()
    val deleteEventActive: LiveData<Boolean?> = _deleteEventActive

    private val _checkAndCall = MutableLiveData<String?>()
    val checkAndCall : LiveData<String?> = _checkAndCall

    // 1. 항목 유형에 따라, 뷰홀더 타입(일반 리스트용, 헤더용)을 반환할 함수
    override fun getItemViewType(position: Int): Int = getItem(position).id

    // 2. viewType 에 따라서, 헤더용 or 리스트용 뷰홀더를 생성
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            GroupItem.Item.VIEW_TYPE -> GroupHolder.from(parent)
            GroupItem.Header.VIEW_TYPE -> GroupListHeader.from(parent)
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    // 3. 생성된 뷰홀더에 따라 bind
    // 리스트용 뷰홀더를 받으면, 리스트 항목에 맞게 bind 하는 함수
    @SuppressLint("MissingPermission")
    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        when (viewHolder) {
            // 리스트
            is GroupHolder -> {
                val item = getItem(position) as GroupItem.Item
                viewHolder.bind(item.groupList, position, mFragmentManager, context)
                alarmBtnStatus[position] = item.groupList.recommendation

                if (alarmBtnStatus[position]) {
                    viewHolder.btnAlarm.setImageResource(R.drawable.ic_baseline_notifications_active_24)
                    viewHolder.btnAlarm.setColorFilter(
                        ContextCompat.getColor(
                            context,
                            R.color.hau_dark_green
                        )
                    )
                } else {
                    viewHolder.btnAlarm.setImageResource(R.drawable.ic_baseline_notifications_off_24)
                    viewHolder.btnAlarm.setColorFilter(
                        ContextCompat.getColor(
                            context,
                            R.color.light_gray
                        )
                    )
                }

                viewHolder.checkBoxGroupListChild.isChecked = checkboxStatus[position]

                if (viewHolder.checkBoxGroupListChild.isChecked) {
                    checkBoxReturnList.add(item.groupList.number)
                } else {
                    checkBoxReturnList.remove(item.groupList.number)
                }


                // 체크박스 on-off 코드
                if (checkBoxControlNumber == 1) {
                    viewHolder.btnAlarm.visibility = View.GONE
                    viewHolder.btnCallDirect.visibility = View.GONE
                    // checkBox 를 표시하고 코드 진행
                    viewHolder.checkBoxGroupListChild.visibility = View.VISIBLE

                    // 체크 박스 터치 시,
                    viewHolder.checkBoxGroupListChild.setOnClickListener {
                        checkboxStatus[position] = !checkboxStatus[position]
                        notifyItemChanged(position)
                    }
                    // 항목 전체 터치 시,
                    viewHolder.groupDetail.setOnClickListener {
                        checkboxStatus[position] = !checkboxStatus[position]
                        notifyItemChanged(position)
                    }
                } else {
                    // 체크박스 off 상태인 경우 발동
                    // 체크박스 초기화
                    checkboxStatus.clear()
                    viewHolder.checkBoxGroupListChild.visibility = View.GONE
                    viewHolder.btnAlarm.visibility = View.VISIBLE
                    viewHolder.btnCallDirect.visibility = View.VISIBLE

                    // 전화버튼 클릭 시, 스낵바 메세지 띄우기
                    viewHolder.btnCallDirect.setOnClickListener {
                        Toast.makeText(context, "전화하려면 길게 터치하세요.", Toast.LENGTH_SHORT).show()
                    }

                    // 전화버튼 롱클릭 시, 전화걸기
                    viewHolder.btnCallDirect.setOnLongClickListener {
                        _checkAndCall.value = item.groupList.number
                        return@setOnLongClickListener true
                    }

                    //리싸이클러 터치 시, GroupDetailFragment 로 이동
                    viewHolder.groupDetail.setOnClickListener {
                        val action =
                            GroupListFragmentDirections.actionGroupListFragmentToGroupDetailFragment(
                                item.groupList.number,item.groupList.name
                            )
                        it.findNavController().navigate(action)
                    }

                    //리싸이클러 길게 터치 시, 삭제 작동
                    viewHolder.groupDetail.setOnLongClickListener {
                        checkboxStatus[position] = true
                        _deleteEventActive.value = true
                        return@setOnLongClickListener true
                    }

                    //image_btn 터치 시, 알람 설정 작동 코드
                    viewHolder.btnAlarm.setOnClickListener {
                        if (item.groupList.recommendation) {
                            _alarmNumberSetting.value = item.groupList
                        } else {
                            _alarmNumberRemoving.value = item.groupList
                        }
                    }
                }
            }

            // 헤더
            is GroupListHeader -> {
                val item = getItem(position) as GroupItem.Header
                viewHolder.bind(item.topList, position)
            }
        }


    }


    // 기존 홀더
    class GroupHolder constructor(private val binding: GroupListChildBinding) :
        RecyclerView.ViewHolder(binding.root) {

        val btnAlarm = binding.btnAlarm
        val checkBoxGroupListChild = binding.checkBoxGroupListChild
        val btnCallDirect = binding.btnCallDirect
        val groupDetail = binding.groupDetail

        companion object {
            fun from(parent: ViewGroup): GroupHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = GroupListChildBinding.inflate(layoutInflater, parent, false)
                return GroupHolder(binding)
            }
        }

        fun bind(item: GroupList, num: Int, fragmentManager: FragmentManager, context: Context) {
            binding.contactName.text = item.name
            binding.contactNumber.text = item.number
            //이미지 관련
            if (item.image != null)
                binding.contactImage.setImageBitmap(item.image)
            else
                binding.contactImage.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.mipmap.none_profile
                    )
                )
        }


    }

    // 신규 뷰홀더 : 헤더용
    class GroupListHeader(private val binding: GroupListHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(topList: List<GroupTopData>, position: Int) {
            when (topList.count()) {
                0 -> {
                    textBinding(
                        "-", 0L, "-", 0L, "-", 0L,
                        "-", 0, "-", 0, "-", 0
                    )
                }

                2 -> {
                    textBinding(
                        topList[0].name, topList[0].duration, "-", 0L, "-", 0L,
                        topList[1].name, topList[1].times, "-", 0, "-", 0
                    )
                }

                4 -> {
                    textBinding(
                        topList[0].name,
                        topList[0].duration,
                        topList[1].name,
                        topList[1].duration,
                        "-",
                        0L,
                        topList[2].name,
                        topList[2].times,
                        topList[3].name,
                        topList[3].times,
                        "-",
                        0
                    )
                }

                6 -> {
                    textBinding(
                        topList[0].name,
                        topList[0].duration,
                        topList[1].name,
                        topList[1].duration,
                        topList[2].name,
                        topList[2].duration,
                        topList[3].name,
                        topList[3].times,
                        topList[4].name,
                        topList[4].times,
                        topList[5].name,
                        topList[5].times
                    )
                }

                else -> {}
            }
        }

        companion object {
            fun from(parent: ViewGroup): GroupListHeader {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = GroupListHeaderBinding.inflate(layoutInflater, parent, false)
                return GroupListHeader(binding)
            }
        }

        @SuppressLint("SetTextI18n")
        private fun textBinding(
            firstDName: String,
            firstDuration: Long,
            secondDName: String,
            secondDuration: Long,
            thirdDName: String,
            thirdDuration: Long,
            firstTName: String,
            firstTimes: Int,
            secondTName: String,
            secondTimes: Int,
            thirdTName: String,
            thirdTimes: Int
        ) {
            with(binding) {
                textDuration1.text = "1위 : $firstDName"
                textDuration2.text = "2위 : $secondDName"
                textDuration3.text = "3위 : $thirdDName"
                textTimes1.text = "1위 : $firstTName"
                textTimes2.text = "2위 : $secondTName"
                textTimes3.text = "3위 : $thirdTName"
            }
        }
    }

    fun onCheckBox(number: Int) {
        checkBoxControlNumber = number
        notifyDataSetChanged()
    }

    fun alarmNumberReset() {
        _alarmNumberSetting.value = null
        _alarmNumberRemoving.value = null
    }

    fun deleteEventReset() {
        _deleteEventActive.value = null
    }

    @JvmName("getCheckBoxReturnList1")
    fun getCheckBoxReturnList(): List<String> {
        return checkBoxReturnList
    }

    fun clearCheckBoxReturnList() {
        checkBoxReturnList.clear()
        checkboxStatus.clear()
    }

    fun checkAndCallClear() {
        _checkAndCall.value = null
    }

}

class GroupDiffCallback : DiffUtil.ItemCallback<GroupItem>() {

    override fun areItemsTheSame(oldItem: GroupItem, newItem: GroupItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: GroupItem, newItem: GroupItem): Boolean {
        return oldItem == newItem
    }

}


sealed class GroupItem {

    abstract val id: Int

    data class Item(
        val groupList: GroupList,
        override val id: Int = VIEW_TYPE
    ) : GroupItem() {
        companion object {
            const val VIEW_TYPE = R.layout.group_list_header
        }
    }

    data class Header(
        val topList: List<GroupTopData>,
        override val id: Int = VIEW_TYPE
    ) : GroupItem() {
        companion object {
            const val VIEW_TYPE = R.layout.group_list_child
        }
    }
}