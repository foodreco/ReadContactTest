package com.leesangmin89.readcontacttest.group.groupList

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.leesangmin89.readcontacttest.R
import com.leesangmin89.readcontacttest.convertLongToDateString
import com.leesangmin89.readcontacttest.convertLongToTimeString
import com.leesangmin89.readcontacttest.data.entity.GroupList
import com.leesangmin89.readcontacttest.databinding.GroupListChildBinding

class GroupListAdapter(ctx: Context) : ListAdapter<GroupList, GroupListAdapter.GroupHolder>(GroupDiffCallback()) {

    private var context: Context = ctx

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupHolder {
        val binding =
            GroupListChildBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GroupHolder(binding)
    }

    @SuppressLint("MissingPermission")
    override fun onBindViewHolder(holder: GroupHolder, position: Int) {
        val item = getItem(position)

        holder.name.text = item.name
        holder.number.text = item.number
        if (item.recentContact == "") {
            holder.currentCall.text = "최근 통화 없음"
        } else {
            holder.currentCall.text = convertLongToDateString(item.recentContact!!.toLong())
        }
        if (item.recentContactCallTime == "") {
            holder.currentCallTimes.text = ""
        } else {
            holder.currentCallTimes.text = convertLongToTimeString(item.recentContactCallTime!!.toLong())
        }

        //이미지 관련
        if (item.image != null)
            holder.profile.setImageBitmap(item.image)
        else
            holder.profile.setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.mipmap.ic_launcher_round
                )
            )

        // 전화버튼 클릭 시, 전화걸기
        holder.call.setOnClickListener {
            item.number.let { phoneNumber ->
                val uri = Uri.parse("tel:${phoneNumber.toString()}")
                val intent = Intent(Intent.ACTION_CALL, uri)
                context.startActivity(intent)
            }
        }

        //리싸이클러 터치 시, GroupDetailFragment 로 이동
        holder.detail.setOnClickListener {
            val action = GroupListFragmentDirections.actionGroupListFragmentToGroupDetailFragment(item)
            holder.detail.findNavController().navigate(action)
        }
    }

    class GroupHolder constructor(private val binding: GroupListChildBinding) :
        RecyclerView.ViewHolder(binding.root) {

        val name = binding.contactName
        val number = binding.contactNumber
        val profile = binding.contactImage
        val detail = binding.groupDetail
        val call = binding.btnCallDirect
        val currentCall = binding.currentContact
        val currentCallTimes = binding.contactTime
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
