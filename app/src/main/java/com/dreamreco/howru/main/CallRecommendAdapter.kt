package com.dreamreco.howru.main

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dreamreco.howru.data.entity.Recommendation
import com.dreamreco.howru.databinding.CallRecommendAdapterChildBinding

class CallRecommendAdapter(ctx: Context, fragmentManager: FragmentManager) :
    ListAdapter<Recommendation, CallRecommendAdapter.GroupHolder>(RecoDiffCallback()) {

    private var mFragmentManager: FragmentManager = fragmentManager
    private var context: Context = ctx

    private val _checkAndCall = MutableLiveData<String?>()
    val checkAndCall : LiveData<String?> = _checkAndCall

    inner class GroupHolder constructor(private val binding: CallRecommendAdapterChildBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Recommendation, fragmentManager: FragmentManager) {
            with(binding) {
                contactName.text = item.name
                contactGroup.text = item.group

                if (item.numberOfCallingBelow) {
                    textRecommendationReason.text = "최근 통화가 없습니다."
                } else {
                    if (item.recentCallExcess) {
                        textRecommendationReason.text = "마지막 연락이 1년 경과되었습니다."
                    } else {
                        textRecommendationReason.text = "연락 빈도를 초과했습니다."
                    }
                }

                // 레이아웃 터치 시, 통화기록으로 이동
                recommendLayout.setOnClickListener {
                    val action = MainFragmentDirections.actionMainFragmentToGroupDetailFragment(item.number,item.name)
                    it.findNavController().navigate(action)
                }

                // 전화버튼 클릭 시, 스낵바 메세지 띄우기
                btnCallDirect.setOnClickListener {
                    Toast.makeText(context, "전화하려면 길게 터치하세요.", Toast.LENGTH_SHORT).show()
                }

                // 전화버튼 롱클릭 시, 전화걸기
                btnCallDirect.setOnLongClickListener {
                    _checkAndCall.value = item.number
                    return@setOnLongClickListener true
                }
            }
        }
    }

    fun checkAndCallClear() {
        _checkAndCall.value = null
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
