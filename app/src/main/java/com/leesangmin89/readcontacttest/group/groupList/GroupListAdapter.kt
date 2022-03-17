package com.leesangmin89.readcontacttest.group.groupList

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
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
import com.leesangmin89.readcontacttest.R
import com.leesangmin89.readcontacttest.convertLongToDateString
import com.leesangmin89.readcontacttest.convertLongToTimeString
import com.leesangmin89.readcontacttest.data.entity.GroupList
import com.leesangmin89.readcontacttest.databinding.GroupListChildBinding

class GroupListAdapter(ctx: Context, fragmentManager: FragmentManager) :
    ListAdapter<GroupList, GroupListAdapter.GroupHolder>(GroupDiffCallback()) {

    private var mFragmentManager: FragmentManager = fragmentManager
    private var context: Context = ctx
    private var checkBoxControlNumber: Int = 0
    private val checkboxStatus = SparseBooleanArray()
    private val alarmBtnStatus = SparseBooleanArray()
    val checkBoxReturnList = mutableListOf<String>()
    val alarmReturnList = mutableListOf<GroupList>()

    private val _alarmNumberSetting = MutableLiveData<GroupList?>()
    val alarmNumberSetting : LiveData<GroupList?> = _alarmNumberSetting

    private val _alarmNumberRemoving = MutableLiveData<GroupList?>()
    val alarmNumberRemoving : LiveData<GroupList?> = _alarmNumberRemoving

    private val _deleteEventActive = MutableLiveData<Boolean?>()
    val deleteEventActive : LiveData<Boolean?> = _deleteEventActive

    inner class GroupHolder constructor(private val binding: GroupListChildBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: GroupList, num: Int, fragmentManager: FragmentManager) {

            val name = binding.contactName
            val number = binding.contactNumber
            val profile = binding.contactImage
            val detail = binding.groupDetail
            val call = binding.btnCallDirect
            val currentCall = binding.currentContact
            val currentCallTimes = binding.contactTime
            val checkBox = binding.checkBoxGroupListChild

            name.text = item.name
            number.text = item.number
            if (item.recentContact == "") {
                currentCall.text = "최근 통화 없음"
            } else {
                currentCall.text = convertLongToDateString(item.recentContact!!.toLong())
            }
            if (item.recentContactCallTime == "") {
                currentCallTimes.text = ""
            } else {
                currentCallTimes.text =
                    convertLongToTimeString(item.recentContactCallTime!!.toLong())
            }
            //이미지 관련
            if (item.image != null)
                profile.setImageBitmap(item.image)
            else
                profile.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.mipmap.ic_launcher_round
                    )
                )
            alarmBtnStatus[num] = item.recommendation

            if (alarmBtnStatus[num]) {
                binding.btnAlarm.setImageResource(R.drawable.ic_baseline_notifications_active_24)
            } else {
                binding.btnAlarm.setImageResource(R.drawable.ic_baseline_notifications_off_24)
            }

            binding.checkBoxGroupListChild.isChecked = checkboxStatus[num]

            if (binding.checkBoxGroupListChild.isChecked) {
                checkBoxReturnList.add(item.number)
            } else {
                checkBoxReturnList.remove(item.number)
            }


            // 체크박스 on-off 코드
            if (checkBoxControlNumber == 1) {
                checkBoxOnActivate(num, item)
            } else {
                // 체크박스 off 상태인 경우 발동
                // 체크박스 초기화
                checkboxStatus.clear()
                checkBox.visibility = View.GONE
                binding.btnAlarm.visibility = View.VISIBLE
                binding.btnCallDirect.visibility = View.VISIBLE

                // 전화버튼 클릭 시, 전화걸기
                call.setOnClickListener {
                    item.number.let { phoneNumber ->
                        val uri = Uri.parse("tel:${phoneNumber.toString()}")
                        val intent = Intent(Intent.ACTION_CALL, uri)
                        context.startActivity(intent)
                    }
                }

                //리싸이클러 터치 시, CrossroadDialog 로 이동
                detail.setOnClickListener {
                    val action =
                        GroupListFragmentDirections.actionGroupListFragmentToCrossroadDialog(item)
                    it.findNavController().navigate(action)
                }

                //리싸이클러 길게 터치 시, 삭제 작동
                detail.setOnLongClickListener {
                    checkboxStatus[num] = true
                    _deleteEventActive.value = true
                    return@setOnLongClickListener true
                }


                //image_btn 터치 시, 알람 설정 작동 코드
                binding.btnAlarm.setOnClickListener {
                    if (item.recommendation) {
                        _alarmNumberSetting.value = item
                    } else {
                        _alarmNumberRemoving.value = item
                    }
                }
            }
        }

        // 체크박스 on 일 때 작동하는 코드
        private fun checkBoxOnActivate(num:Int, item:GroupList) {
            binding.btnAlarm.visibility = View.GONE
            binding.btnCallDirect.visibility = View.GONE
            // checkBox 를 표시하고 코드 진행
            binding.checkBoxGroupListChild.visibility = View.VISIBLE
//            // 체크박스 유지 코드
//            binding.checkBoxGroupListChild.isChecked = checkboxStatus[num]
//            if (binding.checkBoxGroupListChild.isChecked) {
//                checkBoxReturnList.add(item)
//            } else {
//                checkBoxReturnList.remove(item)
//            }
            // 체크 박스 터치 시,
            binding.checkBoxGroupListChild.setOnClickListener {
                checkboxStatus[num] = !checkboxStatus[num]
                notifyItemChanged(num)
            }
            // 항목 전체 터치 시,
            binding.groupDetail.setOnClickListener {
                checkboxStatus[num] = !checkboxStatus[num]
                notifyItemChanged(num)
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupHolder {
        val binding =
            GroupListChildBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GroupHolder(binding)
    }

    @SuppressLint("MissingPermission")
    override fun onBindViewHolder(holder: GroupHolder, position: Int) {
        holder.bind(getItem(position), position, mFragmentManager)
    }

    fun clearCheckBoxReturnList() {
        checkBoxReturnList.clear()
        checkboxStatus.clear()
    }
}

class GroupDiffCallback : DiffUtil.ItemCallback<GroupList>() {

    override fun areItemsTheSame(oldItem: GroupList, newItem: GroupList): Boolean {
        return oldItem.number == newItem.number
    }

    override fun areContentsTheSame(oldItem: GroupList, newItem: GroupList): Boolean {
        return oldItem == newItem
    }

}
