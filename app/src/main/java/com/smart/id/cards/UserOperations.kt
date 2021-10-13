package com.smart.id.cards

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class UserOperations : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_operations)
    }

    fun onClickAddUserProfile(view: View) {
        Intent(this,AddUser::class.java).also {
            startActivity(it)
        }
    }
    fun onClickEditUserProfile(view: View) {
        Intent(this,EditUserUsername::class.java).also {
            startActivity(it)
        }
    }
    fun onClickDeleteUserProfile(view: View) {
        Intent(this,DeleteUser::class.java).also {
            startActivity(it)
        }
    }

    fun onClickLogout(view: View) {}
}