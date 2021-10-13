package com.smart.id.cards

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_delete_user.*

class DeleteUser : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delete_user)
    }

    fun onClickNext(view: View) {
        disableAllHelperTexts()
        if(isAllFieldsValid()) {
            var username = editTextUsername.text.toString()
            Intent(this, FingerPrintScan::class.java).also {
                it.putExtra("from", "delete_user")
                it.putExtra("top", "Delete User")
                it.putExtra("username",username)
                it.putExtra("bottom", "User Deleted Successfully")
                startActivity(it)
            }
        }
    }
    fun isAllFieldsValid():Boolean{
        var status = true
        var username = editTextUsername.text.toString()
        if(username.isEmpty()){
            textInput_username.helperText = "Cannot be empty"
            return false
        }
        return status
    }
    fun disableAllHelperTexts(){
        textInput_username.isHelperTextEnabled = false
    }
}