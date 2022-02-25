package com.leesangmin89.readcontacttest.protomain

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.leesangmin89.readcontacttest.R
import com.leesangmin89.readcontacttest.databinding.FragmentMainBinding
import com.leesangmin89.readcontacttest.databinding.FragmentMainProtoBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainProtoFragment : Fragment() {

    private val binding by lazy { FragmentMainProtoBinding.inflate(layoutInflater) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        return binding.root
    }

}