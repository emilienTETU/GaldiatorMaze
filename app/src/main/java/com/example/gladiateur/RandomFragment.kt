package com.example.gladiateur

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment

class RandomFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Mode.mode = GameMode.EXTREME
        val root = inflater.inflate(R.layout.fragment_random, container, false)
        return root
    }
}
