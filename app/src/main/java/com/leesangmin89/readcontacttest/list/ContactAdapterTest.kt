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

class ContactAdapterTest(ctx: Context) :
    RecyclerView.Adapter<ContactAdapterTest.TestHolder>() {

    private var userList = emptyList<ContactBase>()

    private var checkBoxControlNumber: Int = 0
    private var checkboxList = arrayListOf<CheckBoxData>()
    private var context: Context = ctx

    fun setData(user: List<ContactBase>) {
        this.userList = user
        notifyDataSetChanged()
    }

    inner class TestHolder constructor(private val binding: ContactChildBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ContactBase, num: Int) {
            val name = binding.tvName
            val number = binding.tvNumber
            val profile = binding.ivProfile
            val update = binding.contactChildEachList
            val call = binding.btnCall
            val group = binding.textGroup
            val checkBox = binding.checkBoxListRecycler

            name.text = item.name
            number.text = item.number
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
            // 체크박스 on-off 코드
            if (checkBoxControlNumber == 1) {
                // checkBox를 표시하고 코드 진행
                checkBox.visibility = View.VISIBLE
                if (num >= checkboxList.size) {
                    checkboxList.add(num, CheckBoxData(item.id, false))
                }
                checkBox.isChecked = checkboxList[num].checked
                checkBox.setOnClickListener {
                    checkboxList[num].checked = checkBox.isChecked
                }
            } else {
                checkboxList.clear()
                checkBox.visibility = View.GONE
            }

            // 전화버튼 클릭 시, 전화걸기
            call.setOnClickListener {
                if (checkBoxControlNumber == 1) {
                } else {
                    // checkBox 해제 시, 작동
                    item.number.let { phoneNumber ->
                        val uri = Uri.parse("tel:${phoneNumber.toString()}")
                        val intent = Intent(Intent.ACTION_CALL, uri)
                        context.startActivity(intent)
                    }
                }
            }

            //리싸이클러 터치 시, update 이동
            update.setOnClickListener {
                if (checkBoxControlNumber == 1) {
                } else {
                    // checkBox 해제 시, 작동
                    // 지정 group 이 없으면, ""를 넘겨주고,
                    // 있으면, 해당 전화번호를 넘겨주는 코드
                    if (item.group == "") {
                        val action =
                            ListFragmentDirections.actionListFragmentToUpdateFragment(item, "")
                        update.findNavController().navigate(action)
                    } else {
                        val action =
                            ListFragmentDirections.actionListFragmentToUpdateFragment(
                                item,
                                item.number
                            )
                        update.findNavController().navigate(action)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TestHolder {
        val binding =
            ContactChildBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TestHolder(binding)
    }

    @SuppressLint("MissingPermission")
    override fun onBindViewHolder(holder: TestHolder, position: Int) {
        holder.bind(userList[position], position)
    }

    fun onCheckBox(number: Int) {
        checkBoxControlNumber = number
        notifyDataSetChanged()
        Log.i("확인", "checkBoxControlNumber : $checkBoxControlNumber")
    }

    override fun getItemCount(): Int {
        return userList.size
    }

}
