package com.leesangmin89.readcontacttest.list

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.leesangmin89.readcontacttest.R
import com.leesangmin89.readcontacttest.data.entity.ContactBase
import com.leesangmin89.readcontacttest.databinding.ContactChildBinding

class ContactAdapter(ctx: Context) : ListAdapter<ContactBase, Holder>(ContactDiffCallback()) {

    private var context: Context = ctx

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding =
            ContactChildBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    @SuppressLint("MissingPermission")
    override fun onBindViewHolder(holder: Holder, position: Int) {
        val safePosition = holder.adapterPosition
        val item = getItem(safePosition)

        holder.name.text = item.name
        holder.number.text = item.number
        holder.group.text = item.group
        // 이미지 관련

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

        //리싸이클러 터치 시, update 이동
        holder.update.setOnClickListener {
            // 지정 group 이 없으면, ""를 넘겨주고,
            // 있으면, 해당 전화번호를 넘겨주는 코드
            if (item.group == "") {
                val action = ListFragmentDirections.actionListFragmentToUpdateFragment(item,"")
                holder.update.findNavController().navigate(action)
            } else {
                val action = ListFragmentDirections.actionListFragmentToUpdateFragment(item,item.number)
                holder.update.findNavController().navigate(action)
            }
        }
    }
}

class Holder constructor(private val binding: ContactChildBinding) :
    RecyclerView.ViewHolder(binding.root) {

    val name = binding.tvName
    val number = binding.tvNumber
    val profile = binding.ivProfile
    val update = binding.contactChildEachList
    val call = binding.btnCall
    val group = binding.textGroup
}

class ContactDiffCallback : DiffUtil.ItemCallback<ContactBase>() {

    override fun areItemsTheSame(oldItem: ContactBase, newItem: ContactBase): Boolean {
        return oldItem.number == newItem.number
    }

    override fun areContentsTheSame(oldItem: ContactBase, newItem: ContactBase): Boolean {
        return oldItem == newItem
    }

}