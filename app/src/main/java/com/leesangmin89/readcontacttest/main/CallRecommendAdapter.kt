package com.leesangmin89.readcontacttest.main

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.leesangmin89.readcontacttest.R
import com.leesangmin89.readcontacttest.customDialog.RecommendationDetailDialog
import com.leesangmin89.readcontacttest.customDialog.UpdateDialog
import com.leesangmin89.readcontacttest.data.entity.ContactBase
import com.leesangmin89.readcontacttest.data.entity.Recommendation
import com.leesangmin89.readcontacttest.databinding.CallRecommendAdapterChildBinding

class CallRecommendAdapter(ctx: Context, fragmentManager: FragmentManager) :
    ListAdapter<Recommendation, CallRecommendAdapter.GroupHolder>(RecoDiffCallback()) {

    private var mFragmentManager: FragmentManager = fragmentManager
    private var context: Context = ctx

    inner class GroupHolder constructor(private val binding: CallRecommendAdapterChildBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Recommendation, fragmentManager: FragmentManager) {

            binding.contactName.text = item.name
            binding.contactGroup.text = item.group

            if (item.numberOfCallingBelow) {
                binding.textRecommendationReason.text = "최근 통화가 없습니다."
            } else {
                if (item.recentCallExcess) {
                    binding.textRecommendationReason.text = "마지막 연락이 1년 경과되었습니다."
                } else {
                    binding.textRecommendationReason.text = "연락 빈도를 초과했습니다."
                }
            }

            // 레이아웃 터치 시, 상세정보
            binding.recommendLayout.setOnClickListener {
                // recommendation detail dialog 띄우고, item 넘겨줌
                val bundle = bundleOf()
                bundle.putParcelable("recommendation", item)
                val dialog = RecommendationDetailDialog()
                dialog.arguments = bundle
                dialog.show(fragmentManager, "RecommendationDetailDialog")
            }

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
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupHolder {
        val binding =
            CallRecommendAdapterChildBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return GroupHolder(binding)
    }

    @SuppressLint("MissingPermission")
    override fun onBindViewHolder(holder: GroupHolder, position: Int) {
        holder.bind(getItem(position), mFragmentManager)
    }
}

class RecoDiffCallback : DiffUtil.ItemCallback<Recommendation>() {

    override fun areItemsTheSame(oldItem: Recommendation, newItem: Recommendation): Boolean {
        return oldItem.number == newItem.number
    }

    override fun areContentsTheSame(oldItem: Recommendation, newItem: Recommendation): Boolean {
        return oldItem == newItem
    }

}
