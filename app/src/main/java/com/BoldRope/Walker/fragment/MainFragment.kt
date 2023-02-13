package com.BoldRope.Walker.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.BoldRope.Walker.R
import com.BoldRope.Walker.databinding.FragmentMainBinding
import com.BoldRope.Walker.utils.Constants.KEY_BEST_SCORE
import com.BoldRope.Walker.utils.getString
import com.BoldRope.Walker.utils.saveString
import com.BoldRope.Walker.utils.viewBinding

class MainFragment : Fragment(R.layout.fragment_main) {

    private val binding by viewBinding { FragmentMainBinding.bind(it) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (getString(KEY_BEST_SCORE) == "") {
            saveString(KEY_BEST_SCORE, "0")
        }

        with(binding) {
            btnStart.setOnClickListener {
                findNavController().navigate(R.id.action_mainFragment_to_gameFragment)
            }

            btnSetting.setOnClickListener {
                findNavController().navigate(R.id.action_mainFragment_to_settingsFragment)
            }
        }

    }

}