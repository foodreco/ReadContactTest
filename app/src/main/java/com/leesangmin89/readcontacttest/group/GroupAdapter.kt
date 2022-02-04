package com.leesangmin89.readcontacttest.group

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.leesangmin89.readcontacttest.databinding.GroupChildBinding

class GroupAdapter(ctx: Context) : ListAdapter<GroupData, GroupHolder>(GroupDiffCallback()) {

    private var context: Context = ctx

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupHolder {
        val binding =
            GroupChildBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GroupHolder(binding)
    }

    @SuppressLint("MissingPermission")
    override fun onBindViewHolder(holder: GroupHolder, position: Int) {
        var safePosition = holder.adapterPosition
        val item = getItem(safePosition)

        holder.groupName.text = item.groupName
        holder.groupNumber.text = item.groupNumber
        holder.groupRate.text = item.groupRate

        //리싸이클러 터치 시, GroupListFragment 로 이동
        holder.groupEachList.setOnClickListener {
            // 이동 시, 그룹명을 args 로 넘겨준다.
            val action = GroupFragmentDirections.actionGroupFragmentToGroupListFragment(item.groupName)
            holder.groupEachList.findNavController().navigate(action)
        }
    }
}

class GroupHolder constructor(private val binding: GroupChildBinding) :
    RecyclerView.ViewHolder(binding.root) {
    val groupName = binding.textGroupName
    val groupNumber = binding.textGroupNumbers
    val groupRate = binding.textGroupRate
    val groupEachList = binding.groupEachList
}

class GroupDiffCallback : DiffUtil.ItemCallback<GroupData>() {
    override fun areItemsTheSame(oldItem: GroupData, newItem: GroupData): Boolean {
        return oldItem.groupName == newItem.groupName
    }

    override fun areContentsTheSame(oldItem: GroupData, newItem: GroupData): Boolean {
        return oldItem == newItem
    }

}

data class GroupData(
    val groupName: String,
    val groupNumber: String,
    val groupRate: String
)