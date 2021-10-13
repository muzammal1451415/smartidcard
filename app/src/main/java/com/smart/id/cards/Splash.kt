package com.smart.id.cards

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler

class Splash : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        // added comments
        Handler().postDelayed({
              Intent(this,MainActivity::class.java).also {
                  startActivity(it)
              }

        },2000L)
    }
}