package com.leesangmin89.readcontacttest.group.groupListAdd

import android.annotation.SuppressLint
import android.content.Context
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.util.set
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.leesangmin89.readcontacttest.R
import com.leesangmin89.readcontacttest.data.entity.ContactBase
import com.leesangmin89.readcontacttest.databinding.ContactGroupAddChildBinding

class GroupListAddAdapter(ctx: Context) :
    ListAdapter<ContactBase, GroupListAddAdapter.Holder>(ContactDiffCallback()) {

    private var context: Context = ctx
    private val checkboxStatus = SparseBooleanArray()
    private var checkboxClearEvent : Boolean = false
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
                        R.mipmap.ic_launcher_round
                    )
                )

            // 그룹이 없는 view 만 체크기능 작동
            if (item.group == "") {

                // 검색 리스트가 새로 갱신되면 checkboxStatus 초기화
                if (checkboxClearEvent) {
                    checkboxStatus.clear()
                    checkboxClearEvent = false
                }

                // 체크박스 유지 코드
                checkBox.visibility = View.VISIBLE
                checkBox.isChecked = checkboxStatus[num]

                if (checkBox.isChecked) {
                    checkBoxReturnList.add(item)
                } else {
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

    fun clearCheckBox() {
        checkboxClearEvent = true
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
