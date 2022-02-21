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
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DiffUtil.DiffResult.NO_POSITION
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.leesangmin89.readcontacttest.R
import com.leesangmin89.readcontacttest.data.entity.ContactBase
import com.leesangmin89.readcontacttest.databinding.ContactChildBinding
import com.leesangmin89.readcontacttest.databinding.ContactGroupAddChildBinding
import com.leesangmin89.readcontacttest.list.CheckBoxData

class GroupListAddAdapter(ctx: Context) :
    ListAdapter<ContactBase, GroupListAddAdapter.Holder>(ContactDiffCallback()) {

    private var context: Context = ctx
    private val checkboxStatus = SparseBooleanArray()
    private var checkboxClearEvent = 0
    var btnAddClickableEvent = 0
    val checkBoxReturnList = mutableListOf<ContactBase>()

    inner class Holder constructor(private val binding: ContactGroupAddChildBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ContactBase, num: Int) {

            val name = binding.tvName
            val number = binding.tvNumber
            val profile = binding.ivProfile
            val update = binding.contactChildEachList
            val call = binding.btnCall
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

            // 체크박스 유지 코드
            checkBox.isChecked = checkboxStatus[num]
            checkBox.setOnClickListener {
                if (checkBox.isChecked) {
                    checkboxStatus.put(num, true)
                    Log.i("어댑터","체크됨")
                    checkBoxReturnList.add(item)
                } else {
                    checkboxStatus.put(num, false)
                    Log.i("어댑터","체크해제")
                    checkBoxReturnList.remove(item)
                }
                notifyItemChanged(num)
            }

            if (checkboxClearEvent == 1) {
                checkboxStatus.clear()
            }

            // 전화버튼 클릭 시, 전화걸기
            call.setOnClickListener {
                item.number.let { phoneNumber ->
                    val uri = Uri.parse("tel:${phoneNumber.toString()}")
                    val intent = Intent(Intent.ACTION_CALL, uri)
                    context.startActivity(intent)
                }
            }

            //리싸이클러 터치 시, update 이동
            update.setOnClickListener {
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

    fun clearCheckBox(number: Int) {
        checkboxClearEvent = number
    }

    @JvmName("getCheckBoxReturnList1")
    fun getCheckBoxReturnList() : List<ContactBase> {
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
