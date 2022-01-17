package com.leesangmin89.readcontacttest

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.leesangmin89.readcontacttest.data.ContactDTO
import com.leesangmin89.readcontacttest.databinding.ContactChildBinding

class ContactAdapter(ctx: Context) : ListAdapter<ContactDTO, Holder>(ContactDiffCallback()) {

    private var context: Context = ctx

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding =
            ContactChildBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = getItem(position)

        holder.name.text = item.name
        holder.number.text = item.number
        if (item.image != null)
            holder.profile.setImageBitmap(item.image)
        else
            holder.profile.setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.mipmap.ic_launcher_round
                )
            )
    }
}

class Holder constructor(private val binding: ContactChildBinding) :
    RecyclerView.ViewHolder(binding.root) {
    val name = binding.tvName
    val number = binding.tvNumber
    val profile = binding.ivProfile
}

class ContactDiffCallback : DiffUtil.ItemCallback<ContactDTO>() {
    override fun areItemsTheSame(oldItem: ContactDTO, newItem: ContactDTO): Boolean {
        return oldItem.number == newItem.number
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: ContactDTO, newItem: ContactDTO): Boolean {
        return oldItem == newItem
    }

}