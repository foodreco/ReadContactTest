package com.leesangmin89.readcontacttest.group

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.ui.res.stringResource
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.leesangmin89.readcontacttest.R
import com.leesangmin89.readcontacttest.databinding.GroupChildBinding

class GroupAdapter() : ListAdapter<GroupData, GroupHolder>(GroupDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupHolder {
        val binding =
            GroupChildBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GroupHolder(binding)
    }

    @SuppressLint("MissingPermission")
    override fun onBindViewHolder(holder: GroupHolder, position: Int) {
        val item = getItem(position)
        with(holder) {
            groupName.text = item.groupName
            groupNumber.text = "등록인원 수 : ${item.groupNumber}명"
            val rateNumber: Double =
                (item.groupNumber.toDouble() / item.totalNumber.toDouble()) * 100
            groupRate.text = "등록비율 : ${
                String.format(
                    "%.0f",
                    rateNumber
                )
            }%      (${item.groupNumber}/${item.totalNumber})"
            rating.numStars = 5
            rating.rating = item.importanceRating.toFloat()

            //리싸이클러 터치 시, GroupListFragment 로 이동
            groupEachList.setOnClickListener {
                // 이동 시, 그룹명을 args 로 넘겨준다.
                val action =
                    GroupFragmentDirections.actionGroupFragmentToGroupListFragment(item.groupName)
                it.findNavController().navigate(action)
            }

            //리싸이클러 길게 터치 시, 그룹명 변경하기
            groupEachList.setOnLongClickListener {
                // 그룹명 argument 로 넘겨주기
                val action =
                    GroupFragmentDirections.actionGroupFragmentToGroupNameEditDialog(item.groupName)
                it.findNavController().navigate(action)
                return@setOnLongClickListener true
            }
        }
    }
}

class GroupHolder constructor(private val binding: GroupChildBinding) :
    RecyclerView.ViewHolder(binding.root) {
    val groupName = binding.textGroupName
    val groupNumber = binding.textGroupNumbers
    val groupRate = binding.textGroupRate
    val groupEachList = binding.groupEachList
    val rating = binding.groupRating
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
    val groupNumber: Int,
    val totalNumber: Int,
    val recommendedNumber: Int,
    val importanceRating : Double
)