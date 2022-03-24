package com.leesangmin89.readcontacttest.group.groupList

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.util.set
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.leesangmin89.readcontacttest.R
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

    private val _alarmNumberSetting = MutableLiveData<GroupList?>()
    val alarmNumberSetting : LiveData<GroupList?> = _alarmNumberSetting

    private val _alarmNumberRemoving = MutableLiveData<GroupList?>()
    val alarmNumberRemoving : LiveData<GroupList?> = _alarmNumberRemoving

    private val _deleteEventActive = MutableLiveData<Boolean?>()
    val deleteEventActive : LiveData<Boolean?> = _deleteEventActive

    inner class GroupHolder constructor(private val binding: GroupListChildBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: GroupList, num: Int, fragmentManager: FragmentManager) {

            binding.contactName.text = item.name
            binding.contactNumber.text = item.number
            //이미지 관련
            if (item.image != null)
                binding.contactImage.setImageBitmap(item.image)
            else
                binding.contactImage.setImageDrawable(
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
                binding.btnAlarm.setColorFilter(ContextCompat.getColor(context, R.color.light_gray))
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
                binding.checkBoxGroupListChild.visibility = View.GONE
                binding.btnAlarm.visibility = View.VISIBLE
                binding.btnCallDirect.visibility = View.VISIBLE

                // 전화버튼 클릭 시, 스낵바 메세지 띄우기
                binding.btnCallDirect.setOnClickListener {
                    val callSnackBar = Snackbar.make(it, "전화하려면 길게 터치하세요.", Snackbar.LENGTH_SHORT)
                    callSnackBar.setTextColor(ContextCompat.getColor(context, R.color.white))
                    callSnackBar.setBackgroundTint(ContextCompat.getColor(context, R.color.hau_emerald))
                    callSnackBar.show()
                }

                // 전화버튼 롱클릭 시, 전화걸기
                binding.btnCallDirect.setOnLongClickListener {
                    item.number.let { phoneNumber ->
                        val uri = Uri.parse("tel:${phoneNumber.toString()}")
                        val intent = Intent(Intent.ACTION_CALL, uri)
                        context.startActivity(intent)
                    }
                    return@setOnLongClickListener true
                }

                //리싸이클러 터치 시, CrossroadDialog 로 이동
                binding.groupDetail.setOnClickListener {
                    val action =
                        GroupListFragmentDirections.actionGroupListFragmentToGroupDetailFragment(item.number)
                    it.findNavController().navigate(action)
                }

                //리싸이클러 길게 터치 시, 삭제 작동
                binding.groupDetail.setOnLongClickListener {
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
