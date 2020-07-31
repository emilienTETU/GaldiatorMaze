package com.example.gladiateur

import androidx.lifecycle.MutableLiveData

object Mode {
    var mode : GameMode = GameMode.HISTORY
    //var mazeSelected = 1
    var mazeSelected: MutableLiveData<Int> = MutableLiveData()
}