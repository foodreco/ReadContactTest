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
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.util.set
import androidx.fragment.app.FragmentManager
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.leesangmin89.readcontacttest.R
import com.leesangmin89.readcontacttest.convertLongToDateString
import com.leesangmin89.readcontacttest.convertLongToTimeString
import com.leesangmin89.readcontacttest.customDialog.CrossroadDialog
import com.leesangmin89.readcontacttest.customDialog.UpdateDialog
import com.leesangmin89.readcontacttest.data.entity.ContactBase
import com.leesangmin89.readcontacttest.data.entity.GroupList
import com.leesangmin89.readcontacttest.databinding.GroupListChildBinding

class GroupListAdapter(ctx: Context, fragmentManager: FragmentManager) :
    ListAdapter<GroupList, GroupListAdapter.GroupHolder>(GroupDiffCallback()) {

    private var mFragmentManager: FragmentManager = fragmentManager
    private var context: Context = ctx

    private var checkBoxControlNumber: Int = 0
    private val checkboxStatus = SparseBooleanArray()
    private val alarmBtnStatus = SparseBooleanArray()
    val checkBoxReturnList = mutableListOf<GroupList>()
    val alarmReturnList = mutableListOf<GroupList>()

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


            // 체크박스 on-off 코드
            if (checkBoxControlNumber == 1) {
                // checkBox 를 표시하고 코드 진행
                checkBox.visibility = View.VISIBLE
                // 체크박스 유지 코드
                checkBox.isChecked = checkboxStatus[num]
                checkBox.setOnClickListener {
                    if (checkBox.isChecked) {
                        checkboxStatus[num] = true
                        checkBoxReturnList.add(item)
                    }
                    else {
                        checkboxStatus[num] = false
                        checkBoxReturnList.remove(item)
                    }
                    notifyItemChanged(num)
                }
            } else {
                // 체크박스 off 상태인 경우 발동
                // 체크박스 초기화
                checkboxStatus.clear()
                checkBox.visibility = View.GONE

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
                    val action = GroupListFragmentDirections.actionGroupListFragmentToCrossroadDialog(item)
                    it.findNavController().navigate(action)
                }


                //image_btn 터치 시, 작동 코드
                binding.btnAlarm.setOnClickListener {
//                    alarmBtnStatus[num] = !alarmBtnStatus[num]
//                    if (alarmBtnStatus[num]) {
//                        binding.btnAlarm.setImageResource(R.drawable.ic_baseline_notifications_active_24)
//                    } else {
//                        binding.btnAlarm.setImageResource(R.drawable.ic_baseline_notifications_off_24)
//                    }
//                    notifyItemChanged(num)
                }
            }
        }

    }

    fun onCheckBox(number: Int) {
        checkBoxControlNumber = number
        notifyDataSetChanged()
    }

    @JvmName("getCheckBoxReturnList1")
    fun getCheckBoxReturnList() : List<GroupList> {
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
}

class GroupDiffCallback : DiffUtil.ItemCallback<GroupList>() {

    override fun areItemsTheSame(oldItem: GroupList, newItem: GroupList): Boolean {
        return oldItem.number == newItem.number
    }

    override fun areContentsTheSame(oldItem: GroupList, newItem: GroupList): Boolean {
        return oldItem == newItem
    }

}
