package com.dreamreco.howru.group.groupListAdd

import android.annotation.SuppressLint
import android.content.Context
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.util.set
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dreamreco.howru.R
import com.dreamreco.howru.data.entity.ContactBase
import com.dreamreco.howru.databinding.ContactGroupAddChildBinding

class GroupListAddAdapter(ctx: Context) :
    ListAdapter<ContactBase, GroupListAddAdapter.Holder>(ContactDiffCallback()) {

    private var context: Context = ctx
    private val checkboxStatus = SparseBooleanArray()
    val checkBoxReturnList = mutableListOf<ContactBase>()

    inner class Holder constructor(private val binding: ContactGroupAddChildBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ContactBase, num: Int) {

            val name = binding.tvName
            val number = binding.tvNumber
            val profile = binding.ivProfile
            val update = binding.contactChildEachList
            val group = binding.textGroup
            val checkBox = binding.checkBoxListRecycler

            name.text = item.name
            number.text = item.number
            group.text = item.group

            // 이미지 관련
            if (item.image != null)
                profile.setImageBitmap(item.image)
            else
                profile.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.mipmap.none_profile
                    )
                )

            // 그룹이 없는 view 만 체크기능 작동
            if (item.group == "") {
                checkBox.visibility = View.VISIBLE
                checkBox.isChecked = checkboxStatus[num]

                // checkboxStatus[num] 경우에 따른 리스트 변경
                if (checkboxStatus[num]) {
                    checkBox.isChecked = true
                    checkBoxReturnList.add(item)
                } else {
                    checkBox.isChecked = false
                    checkBoxReturnList.remove(item)
                }

                // 체크박스 or 레이아웃 터치 시 checkboxStatus 상태 변경
                checkBox.setOnClickListener {
                    checkboxStatus[num] = !checkboxStatus[num]
                    notifyItemChanged(num)
                }
                update.setOnClickListener {
                    checkboxStatus[num] = !checkboxStatus[num]
                    notifyItemChanged(num)
                }
            } else {
                checkBox.visibility = View.GONE
                update.setOnClickListener {
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding =
            ContactGroupAddChildBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    @SuppressLint("MissingPermission")
    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position), position)
    }

    // 체크박스를 초기화하는 함수
    fun clearCheckBox() {
        checkboxStatus.clear()
    }

    // 리턴리스트를 초기화하는 함수
    fun clearReturnList() {
        checkBoxReturnList.clear()
    }

    @JvmName("getCheckBoxReturnList1")
    fun getCheckBoxReturnList(): List<ContactBase> {
        return checkBoxReturnList
    }


}


class ContactDiffCallback : DiffUtil.ItemCallback<ContactBase>() {
    override fun areItemsTheSame(oldItem: ContactBase, newItem: ContactBase): Boolean {
        return oldItem.number == newItem.number
    }

    override fun areContentsTheSame(oldItem: ContactBase, newItem: ContactBase): Boolean {
        return oldItem == newItem
    }
}
