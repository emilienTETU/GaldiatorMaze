package com.example.gladiateur

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProviders.of(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        //SET TEXT VIEW
        val textTitle: TextView = root.findViewById(R.id.text_title)
        homeViewModel.textTitle.observe(viewLifecycleOwner, Observer {
            textTitle.text = it
        })

        homeViewModel.textDescription.observe(viewLifecycleOwner, Observer {
            text_description.text = it
        })

        homeViewModel.textPlayer.observe(viewLifecycleOwner, Observer {
            player.text = it
        })

        homeViewModel.textGlad.observe(viewLifecycleOwner, Observer {
            glad.text = it
        })

        homeViewModel.textGoodLuck.observe(viewLifecycleOwner, Observer {
            text_good_luck.text = it
        })
        return root
    }
}
