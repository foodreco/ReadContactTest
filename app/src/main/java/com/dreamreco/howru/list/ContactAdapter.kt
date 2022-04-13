package com.dreamreco.howru.list

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dreamreco.howru.*
import com.dreamreco.howru.customDialog.UpdateDialog
import com.dreamreco.howru.data.entity.ContactBase
import com.dreamreco.howru.databinding.CallLogHeaderBinding
import com.dreamreco.howru.databinding.ContactChildBinding
import com.dreamreco.howru.databinding.ListFragmentEmptyHeaderBinding
import com.dreamreco.howru.util.ContactBaseItem
import com.dreamreco.howru.util.transformingToInitialSpell

class ContactAdapter(ctx: Context, fragmentManager: FragmentManager) :
    ListAdapter<ContactBaseItem, RecyclerView.ViewHolder>(ContactDiffCallback()) {

    private var mFragmentManager: FragmentManager = fragmentManager
    private var context: Context = ctx

    override fun getItemViewType(position: Int): Int = getItem(position).layoutId

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ContactBaseItem.Header.VIEW_TYPE -> ContactBaseHeaderViewHolder.from(parent)
            ContactBaseItem.Item.VIEW_TYPE -> ContactBaseItemViewHolder.from(parent)
            ContactBaseItem.EmptyHeader.VIEW_TYPE -> EmptyHeaderViewHolder.from(parent)
            else -> throw IllegalArgumentException("Cannot create ViewHolder for view type: $viewType")
        }
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        when (viewHolder) {
            is ContactBaseHeaderViewHolder -> {
                val item = getItem(position) as ContactBaseItem.Header
                viewHolder.bind(item)
            }
            is ContactBaseItemViewHolder -> {
                val item = getItem(position) as ContactBaseItem.Item
                val contactBase = item.contactBase
                viewHolder.bind(contactBase, mFragmentManager, context)
            }
        }
    }

    // 헤더용 뷰홀더
    class ContactBaseHeaderViewHolder constructor(private val binding: CallLogHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): ContactBaseHeaderViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = CallLogHeaderBinding.inflate(layoutInflater, parent, false)
                return ContactBaseHeaderViewHolder(binding)
            }
        }

        fun bind(item: ContactBaseItem) {
            val contactBase = (item as ContactBaseItem.Header).contactBase
            binding.textDate.text = transformingToInitialSpell(contactBase.name)
        }
    }

    // empty 헤더용 뷰홀더
    class EmptyHeaderViewHolder constructor(private val binding: ListFragmentEmptyHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): EmptyHeaderViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListFragmentEmptyHeaderBinding.inflate(layoutInflater, parent, false)
                return EmptyHeaderViewHolder(binding)
            }
        }
    }

    // 리스트용 뷰홀더
    class ContactBaseItemViewHolder constructor(private val binding: ContactChildBinding) :
        RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): ContactBaseItemViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ContactChildBinding.inflate(layoutInflater, parent, false)
                return ContactBaseItemViewHolder(binding)
            }
        }

        fun bind(item: ContactBase, fragmentManager: FragmentManager, context: Context) {
            binding.apply {
                tvName.text = item.name
                textGroup.text = item.group

                //리싸이클러 터치 시, 해당 ContactBase 정보를 bundle 로 넘기고 updateDialog show
                contactChildEachList.setOnClickListener {
                    // updateDialog 띄우고, item 넘겨줌
                    // 해당 전화번호를 넘겨주는 코드
                    // RecyclerView 위치 고수하기 위해, show 형태로 출력한다.
                    val bundle = bundleOf()
                    bundle.putParcelable("contactBase", item)
                    val dialog = UpdateDialog()
                    dialog.arguments = bundle
                    dialog.show(fragmentManager, "UpdateDialog")
                }
                // 이미지 관련
                if (item.image != null)
                    ivProfile.setImageBitmap(item.image)
                else
                    ivProfile.setImageDrawable(
                        ContextCompat.getDrawable(
                            context,
                            R.mipmap.none_profile
                        )
                    )
            }
        }
    }
}

class ContactDiffCallback : DiffUtil.ItemCallback<ContactBaseItem>() {
    override fun areItemsTheSame(oldItem: ContactBaseItem, newItem: ContactBaseItem): Boolean {
        return oldItem.layoutId == newItem.layoutId
    }

    override fun areContentsTheSame(oldItem: ContactBaseItem, newItem: ContactBaseItem): Boolean {
        return oldItem == newItem
    }
}