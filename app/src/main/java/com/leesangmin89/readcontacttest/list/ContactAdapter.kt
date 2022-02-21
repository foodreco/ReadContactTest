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
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.leesangmin89.readcontacttest.R
import com.leesangmin89.readcontacttest.data.entity.ContactBase
import com.leesangmin89.readcontacttest.databinding.ContactChildBinding

//class ContactAdapter(ctx: Context) :
//    ListAdapter<ContactBase, ContactAdapter.Holder>(ContactDiffCallback()) {
//
//    private var checkBoxControlNumber : Int = 0
//    private var checkboxList = arrayListOf<CheckBoxData>()
//
//    private var context: Context = ctx
//
//    inner class Holder constructor(private val binding: ContactChildBinding) :
//        RecyclerView.ViewHolder(binding.root) {
//        fun bind(item: ContactBase, num: Int) {
//            val name = binding.tvName
//            val number = binding.tvNumber
//            val profile = binding.ivProfile
//            val update = binding.contactChildEachList
//            val call = binding.btnCall
//            val group = binding.textGroup
//            val checkBox = binding.checkBoxListRecycler
//
//            name.text = item.name
//            number.text = item.number
//            group.text = item.group
//
//            // 이미지 관련
//            if (item.image != null)
//                profile.setImageBitmap(item.image)
//            else
//                profile.setImageDrawable(
//                    ContextCompat.getDrawable(
//                        context,
//                        R.mipmap.ic_launcher_round
//                    )
//                )
//            // 체크박스 on-off 코드
//            if (checkBoxControlNumber == 1) {
//                Log.i("확인","checkBoxControlNumber 진입")
//                // checkBox를 표시하고 코드 진행
//                checkBox.visibility = View.VISIBLE
//                Log.i("확인","checkBoxControlNumber 작동")
//                if (num >= checkboxList.size) {
//                    checkboxList.add(num, CheckBoxData(item.id, false))
//                }
//                checkBox.isChecked = checkboxList[num].checked
//                checkBox.setOnClickListener {
//                    checkboxList[num].checked = checkBox.isChecked
//                }
//            } else {
//                checkboxList.clear()
//                checkBox.visibility = View.GONE
//            }
//
//            // 전화버튼 클릭 시, 전화걸기
//            call.setOnClickListener {
//                item.number.let { phoneNumber ->
//                    val uri = Uri.parse("tel:${phoneNumber.toString()}")
//                    val intent = Intent(Intent.ACTION_CALL, uri)
//                    context.startActivity(intent)
//                }
//            }
//
//            //리싸이클러 터치 시, update 이동
//            update.setOnClickListener {
//                // 지정 group 이 없으면, ""를 넘겨주고,
//                // 있으면, 해당 전화번호를 넘겨주는 코드
//                if (item.group == "") {
//                    val action = ListFragmentDirections.actionListFragmentToUpdateFragment(item, "")
//                    update.findNavController().navigate(action)
//                } else {
//                    val action =
//                        ListFragmentDirections.actionListFragmentToUpdateFragment(item, item.number)
//                    update.findNavController().navigate(action)
//                }
//            }
//        }
//    }
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
//        val binding =
//            ContactChildBinding.inflate(LayoutInflater.from(parent.context), parent, false)
//        return Holder(binding)
//    }
//    @SuppressLint("MissingPermission")
//    override fun onBindViewHolder(holder: Holder, position: Int) {
//        holder.bind(getItem(position), position)
//    }
//    fun onCheckBox(number : Int) {
//        checkBoxControlNumber = number
//        Log.i("확인","checkBoxControlNumber : $checkBoxControlNumber")
//    }
//}
//
//class ContactDiffCallback : DiffUtil.ItemCallback<ContactBase>() {
//    override fun areItemsTheSame(oldItem: ContactBase, newItem: ContactBase): Boolean {
//        return oldItem.number == newItem.number
//    }
//    override fun areContentsTheSame(oldItem: ContactBase, newItem: ContactBase): Boolean {
//        return oldItem == newItem
//    }
//}

//data class CheckBoxData(
//    var id: Int,
//    var checked: Boolean
//)