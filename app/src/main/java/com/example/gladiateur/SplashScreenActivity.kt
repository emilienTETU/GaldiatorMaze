package com.example.gladiateur

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import kotlinx.android.synthetic.main.activity_splash_screen.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class SplashScreenActivity : AppCompatActivity() {

    private val job: Job = Job()
    private val coroutineContext: CoroutineContext = job + Dispatchers.IO
    private val coroutineScope = CoroutineScope(coroutineContext)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        progressBar.visibility = View.VISIBLE
        coroutineScope.launch {
            Thread.sleep(1500)
            changeView()
        }
    }

    private fun changeView(){
        startActivity(Intent(this,GameActivity::class.java))
    }
}
