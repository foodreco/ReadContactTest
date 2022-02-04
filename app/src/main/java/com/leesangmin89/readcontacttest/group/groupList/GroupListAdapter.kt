package com.leesangmin89.readcontacttest.group.groupList

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
import com.leesangmin89.readcontacttest.data.ContactBase
import com.leesangmin89.readcontacttest.databinding.ContactChildBinding
import com.leesangmin89.readcontacttest.databinding.GroupChildBinding
import com.leesangmin89.readcontacttest.list.ContactDiffCallback
import com.leesangmin89.readcontacttest.list.Holder
import com.leesangmin89.readcontacttest.list.ListFragmentDirections
import dagger.hilt.android.AndroidEntryPoint

class GroupListAdapter(ctx: Context) : ListAdapter<ContactBase, Holder>(ContactDiffCallback()) {

    private var context: Context = ctx

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding =
            ContactChildBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    @SuppressLint("MissingPermission")
    override fun onBindViewHolder(holder: Holder, position: Int) {
        var safePosition = holder.adapterPosition
        val item = getItem(safePosition)

        holder.name.text = item.name
        holder.number.text = item.number
        holder.group.text = item.group
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

        //리싸이클러 터치 시, update 이동
        holder.update.setOnClickListener {
            val action = GroupListFragmentDirections.actionGroupListFragmentToGroupFragment()
            holder.update.findNavController().navigate(action)
        }
    }
}