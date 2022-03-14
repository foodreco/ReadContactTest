package com.leesangmin89.readcontacttest.list

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import com.google.android.material.transition.MaterialFade
import com.leesangmin89.readcontacttest.R
import com.leesangmin89.readcontacttest.customDialog.UpdateDialog
import com.leesangmin89.readcontacttest.data.entity.ContactBase
import com.leesangmin89.readcontacttest.databinding.ContactChildBinding

class ContactAdapter(ctx: Context, fragmentManager: FragmentManager) :
    ListAdapter<ContactBase, ContactAdapter.Holder>(ContactDiffCallback()) {

    private var mFragmentManager: FragmentManager = fragmentManager
    private var context: Context = ctx

    inner class Holder constructor(private val binding: ContactChildBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ContactBase,fragmentManager: FragmentManager) {
            val name = binding.tvName
            val profile = binding.ivProfile
            val update = binding.contactChildEachList
            val call = binding.btnCall
            val group = binding.textGroup

            name.text = item.name
            group.text = item.group

            // 이미지 관련
            if (item.image != null)
                profile.setImageBitmap(item.image)
            else
                profile.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.mipmap.ic_launcher_round
                    )
                )

            // 전화버튼 클릭 시, 전화걸기
            call.setOnClickListener {
                item.number.let { phoneNumber ->
                    val uri = Uri.parse("tel:${phoneNumber.toString()}")
                    val intent = Intent(Intent.ACTION_CALL, uri)
                    context.startActivity(intent)
                }
            }

            //리싸이클러 터치 시, 해당 ContactBase 정보를 bundle 로 넘기고 updateDialog show
            update.setOnClickListener {
                // updateDialog 띄우고, item 넘겨줌
                // 해당 전화번호를 넘겨주는 코드
                // RecyclerView 위치 고수하기 위해, show 형태로 출력한다.
                val bundle = bundleOf()
                val list : ContactBase = item
                bundle.putParcelable("contactBase", list)
                val dialog = UpdateDialog()
                dialog.arguments = bundle
                dialog.show(fragmentManager, "UpdateDialog")
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding =
            ContactChildBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    @SuppressLint("MissingPermission")
    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position), mFragmentManager)
    }

}

class ContactDiffCallback : DiffUtil.ItemCallback<ContactBase>() {

    override fun areItemsTheSame(oldItem: ContactBase, newItem: ContactBase): Boolean {
        return oldItem.number == newItem.number
    }

    override fun areContentsTheSame(oldItem: ContactBase, newItem: ContactBase): Boolean {
        return oldItem == newItem
    }
}
