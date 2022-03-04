package com.leesangmin89.readcontacttest.main

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.leesangmin89.readcontacttest.data.entity.Recommendation
import com.leesangmin89.readcontacttest.databinding.CallRecommendAdapterChildBinding

class CallRecommendAdapter(ctx: Context) :
    ListAdapter<Recommendation, CallRecommendAdapter.GroupHolder>(RecoDiffCallback()) {

    private var context: Context = ctx

    inner class GroupHolder constructor(private val binding: CallRecommendAdapterChildBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Recommendation, num: Int) {

            binding.contactName.text = item.name
            binding.contactNumber.text = item.number
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

//            if (item.recentContact == "") {
//                currentCall.text = "최근 통화 없음"
//            } else {
//                currentCall.text = convertLongToDateString(item.recentContact!!.toLong())
//            }
//            if (item.recentContactCallTime == "") {
//                currentCallTimes.text = ""
//            } else {
//                currentCallTimes.text =
//                    convertLongToTimeString(item.recentContactCallTime!!.toLong())
//            }
//            //이미지 관련
//            if (item.image != null)
//                profile.setImageBitmap(item.image)
//            else
//                profile.setImageDrawable(
//                    ContextCompat.getDrawable(
//                        context,
//                        R.mipmap.ic_launcher_round
//                    )
//                )

            // 전화버튼 클릭 시, 전화걸기
            binding.btnCallDirect.setOnClickListener {
                item.number.let { phoneNumber ->
                    val uri = Uri.parse("tel:${phoneNumber.toString()}")
                    val intent = Intent(Intent.ACTION_CALL, uri)
                    context.startActivity(intent)
                }
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
        holder.bind(getItem(position), position)
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
