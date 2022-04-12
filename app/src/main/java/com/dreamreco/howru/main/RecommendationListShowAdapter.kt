package com.dreamreco.howru.main

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dreamreco.howru.databinding.RecommendationListChildBinding

class RecommendationListShowAdapter(ctx: Context) :
    ListAdapter<RecommendationMinimal, RecommendationListShowAdapter.RecoListHolder>(RecommendationMinimalDiffCallback()) {

    val context : Context = ctx

    inner class RecoListHolder constructor(private val binding: RecommendationListChildBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: RecommendationMinimal, num: Int) {
            binding.apply {
                recommendationGroup.text = item.group
                recommendationName.text = item.name
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecoListHolder {
        val binding =
            RecommendationListChildBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return RecoListHolder(binding)
    }

    override fun onBindViewHolder(holder: RecoListHolder, position: Int) {
        holder.bind(getItem(position), position)
    }
}

data class RecommendationMinimal(
    val name: String,
    val group: String
)

class RecommendationMinimalDiffCallback : DiffUtil.ItemCallback<RecommendationMinimal>() {
    override fun areItemsTheSame(
        oldItem: RecommendationMinimal,
        newItem: RecommendationMinimal
    ): Boolean {
        return oldItem.name == newItem.name
    }

    override fun areContentsTheSame(
        oldItem: RecommendationMinimal,
        newItem: RecommendationMinimal
    ): Boolean {
        return oldItem == newItem
    }
}
