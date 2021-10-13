package com.smart.id.cards

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.smart.id.cards.data.Student
import kotlinx.android.synthetic.main.activity_delete_user.*

class EditUserUsername : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_user_username)

    }

    fun onClickNext(view: View) {
        disableAllHelperTexts()
        if(isAllFieldsValid()) {
            var username = editTextUsername.text.toString()
            Intent(this, UpdateUser::class.java).also {
                it.putExtra("username", username)
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