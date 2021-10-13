package com.smart.id.cards

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun onClickAdmin(view: View) {
        Intent(this,AdminLogin::class.java).also{
            it.putExtra("from","admin")
            startActivity(it)
        }
    }
    fun onClickStudent(view: View) {
        Intent(this,AdminLogin::class.java).also {
            it.putExtra("from","student")
            startActivity(it)
        }
    }
}