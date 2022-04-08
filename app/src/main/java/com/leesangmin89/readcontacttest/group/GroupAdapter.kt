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
import com.leesangmin89.readcontacttest.data.entity.GroupList
import com.leesangmin89.readcontacttest.databinding.GroupChildBinding
import com.leesangmin89.readcontacttest.databinding.GroupFragmentHeaderBinding
import com.leesangmin89.readcontacttest.util.GroupTopData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// submitList 를 위한 코루틴 장치 적용
private val adapterScope = CoroutineScope(Dispatchers.Default)

class GroupAdapter : ListAdapter<GroupFragmentItem, RecyclerView.ViewHolder>(GroupDiffCallback()) {

    // 1. 항목 유형에 따라, 뷰홀더 타입(일반 리스트용, 헤더용)을 반환할 함수
    override fun getItemViewType(position: Int): Int = getItem(position).id

    // 2. viewType 에 따라서, 헤더용 or 리스트용 뷰홀더를 생성
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            GroupFragmentItem.Header.VIEW_TYPE -> HeaderHolder.from(parent)
            GroupFragmentItem.Item.VIEW_TYPE -> GroupHolder.from(parent)
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    // 3. 생성된 뷰홀더에 따라 bind
    // 리스트용 뷰홀더를 받으면, 리스트 항목에 맞게 bind 하는 함수
    @SuppressLint("MissingPermission")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is GroupHolder -> {
                val item = getItem(position) as GroupFragmentItem.Item
                holder.bind(item.groupData, position)
            }
        }
    }

    // 어뎁터 리스트를 갱신할 함수
    // submitList 메서드 대신 이걸 사용.
    // 헤더를 추가하고 나서 리스트를 추가하는 방식
    fun addHeaderAndSubmitList(list: List<GroupData>) {
        adapterScope.launch {
            val items = when (list) {
                // 받은 리스트가 empty 면, 헤더만 반환
                emptyList<GroupData>() -> listOf(GroupFragmentItem.Header())
                // 리스트를 받으면, 리스트만 반환
                else -> list.map { GroupFragmentItem.Item(it) }
            }
            withContext(Dispatchers.Main) {
                submitList(items)
            }
        }
    }
}

// Item 홀더
class GroupHolder constructor(private val binding: GroupChildBinding) :
    RecyclerView.ViewHolder(binding.root) {
    val groupName = binding.textGroupName
    val groupNumber = binding.textGroupNumbers
    val groupRate = binding.textGroupRate
    val groupEachList = binding.groupEachList
    val rating = binding.groupRating

    companion object {
        fun from(parent: ViewGroup): GroupHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = GroupChildBinding.inflate(layoutInflater, parent, false)
            return GroupHolder(binding)
        }
    }

    fun bind(item: GroupData, position: Int) {
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

// Header 홀더
class HeaderHolder(private val binding: GroupFragmentHeaderBinding) :
    RecyclerView.ViewHolder(binding.root) {
    companion object {
        fun from(parent: ViewGroup): HeaderHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = GroupFragmentHeaderBinding.inflate(layoutInflater, parent, false)
            return HeaderHolder(binding)
        }
    }
}

class GroupDiffCallback : DiffUtil.ItemCallback<GroupFragmentItem>() {
    override fun areItemsTheSame(oldItem: GroupFragmentItem, newItem: GroupFragmentItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: GroupFragmentItem,
        newItem: GroupFragmentItem
    ): Boolean {
        return oldItem == newItem
    }
}

sealed class GroupFragmentItem {

    abstract val id: Int

    data class Item(
        val groupData: GroupData,
        override val id: Int = VIEW_TYPE
    ) : GroupFragmentItem() {
        companion object {
            const val VIEW_TYPE = R.layout.group_child
        }
    }

    data class Header(override val id: Int = VIEW_TYPE) : GroupFragmentItem() {
        companion object {
            const val VIEW_TYPE = R.layout.group_fragment_header
        }
    }
}

data class GroupData(
    val groupName: String,
    val groupNumber: Int,
    val totalNumber: Int,
    val recommendedNumber: Int,
    val importanceRating: Double
)