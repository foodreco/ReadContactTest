package com.dreamreco.howru.main

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dreamreco.howru.R
import com.dreamreco.howru.databinding.RecommendationListChildBinding
import com.dreamreco.howru.databinding.RecommendationListEmptyHeaderBinding
import com.dreamreco.howru.group.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecommendationListShowAdapter(ctx: Context) :
    ListAdapter<RecommendationMinimalItem, RecyclerView.ViewHolder>(RecommendationMinimalDiffCallback()) {

    val context : Context = ctx

    override fun getItemViewType(position: Int): Int = getItem(position).id

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            RecommendationMinimalItem.EmptyHeader.VIEW_TYPE -> RecommendationEmptyHeader.from(parent)
            RecommendationMinimalItem.Item.VIEW_TYPE -> RecoListHolder.from(parent)
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is RecoListHolder -> {
                val item = getItem(position) as RecommendationMinimalItem.Item
                holder.bind(item.recoData, position)
            }
        }
    }

    // 어뎁터 리스트를 갱신할 함수
    // submitList 메서드 대신 이걸 사용.
    // 헤더를 추가하고 나서 리스트를 추가하는 방식
    fun addHeaderAndSubmitList(list: List<RecommendationMinimal>) {
        adapterScope.launch {
            val items = when (list) {
                // 받은 리스트가 empty 면, 헤더만 반환
                emptyList<RecommendationMinimal>() -> listOf(RecommendationMinimalItem.EmptyHeader())
                // 리스트를 받으면, 리스트만 반환
                else -> list.map { RecommendationMinimalItem.Item(it) }
            }
            withContext(Dispatchers.Main) {
                submitList(items)
            }
        }
    }
}

// Item 홀더
class RecoListHolder constructor(private val binding: RecommendationListChildBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(item: RecommendationMinimal, num: Int) {
        binding.apply {
            recommendationGroup.text = item.group
            recommendationName.text = item.name
        }
    }
    companion object {
        fun from(parent: ViewGroup): RecoListHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = RecommendationListChildBinding.inflate(layoutInflater, parent, false)
            return RecoListHolder(binding)
        }
    }
}

// Header 홀더
class RecommendationEmptyHeader(private val binding: RecommendationListEmptyHeaderBinding) :
    RecyclerView.ViewHolder(binding.root) {
    companion object {
        fun from(parent: ViewGroup): RecommendationEmptyHeader {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = RecommendationListEmptyHeaderBinding.inflate(layoutInflater, parent, false)
            return RecommendationEmptyHeader(binding)
        }
    }
}

data class RecommendationMinimal(
    val name: String,
    val group: String
)

class RecommendationMinimalDiffCallback : DiffUtil.ItemCallback<RecommendationMinimalItem>() {
    override fun areItemsTheSame(
        oldItem: RecommendationMinimalItem,
        newItem: RecommendationMinimalItem
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: RecommendationMinimalItem,
        newItem: RecommendationMinimalItem
    ): Boolean {
        return oldItem == newItem
    }
}

sealed class RecommendationMinimalItem {

    abstract val id: Int

    data class Item(
        val recoData: RecommendationMinimal,
        override val id: Int = VIEW_TYPE
    ) : RecommendationMinimalItem() {
        companion object {
            const val VIEW_TYPE = R.layout.recommendation_list_child
        }
    }

    data class EmptyHeader(override val id: Int = VIEW_TYPE) : RecommendationMinimalItem() {
        companion object {
            const val VIEW_TYPE = R.layout.recommendation_list_empty_header
        }
    }
}
