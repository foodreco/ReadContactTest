package com.leesangmin89.readcontacttest.group.groupList

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.leesangmin89.readcontacttest.R
import com.leesangmin89.readcontacttest.databinding.FragmentGroupDetailBinding
import com.leesangmin89.readcontacttest.databinding.FragmentGroupListBinding

class GroupDetailFragment : Fragment() {

    private val binding by lazy { FragmentGroupDetailBinding.inflate(layoutInflater) }
    private val args by navArgs<GroupDetailFragmentArgs>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        binding.listDetail.text = args.currentItem.name

        return binding.root
    }

}