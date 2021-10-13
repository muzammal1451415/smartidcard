package com.smart.id.cards

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import com.smart.id.cards.data.AdminUser
import kotlinx.android.synthetic.main.activity_admin_login.*


class AdminLogin : AppCompatActivity() {
    // FirebaseDatabase variables declaration
    var database: FirebaseDatabase? = null
    private var adminRef: DatabaseReference? = null
    var TAG = "AdminLogin"
    var from:String? = null
    var admins:MutableList<AdminUser> = arrayListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_login)
        from = intent.getStringExtra("from")
        database = FirebaseDatabase.getInstance("https://smart-id-card-f2eab-default-rtdb.firebaseio.com/")
        adminRef = database!!.getReference("admins")
    }
    fun onClickLogin(view: View) {
        disableAllHelperTexts()
        if(isAllFieldsValid()) {
           loginAdmin()
        }
    }
    private fun loginAdmin(){
        var username = editTextUserName.text.toString()
        var password = editTextPassword.text.toString()
        var isUserExist = false
        var user:AdminUser? = null
        admins.clear()
        adminRef!!.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                dataSnapshot.children.forEach {
                    admins.add(it.getValue(AdminUser::class.java)!!)
                }

                for (it in admins){
                    if(it.username.equals(username)){
                        user = it
                        isUserExist = true
                        break;
                    }
                }

                if(isUserExist){
                    if(password.equals(user?.password)){
                        Intent(this@AdminLogin, UserOperations::class.java).also {
                            startActivity(it)
                        }
                    }else{
                        textInput_password.helperText = "Password doesn't match"

                    }
                }else{
                    textInput_username.helperText = "User not exist"
                }



            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException())
            }
        })
    }
    private fun isAllFieldsValid():Boolean{
        var status = true;
        var username = editTextUserName.text.toString()
        var password = editTextPassword.text.toString()
        if(username.isEmpty()){
            textInput_username.helperText = "Cannot be empty"
            status = false
        }
        if(password.isEmpty()){
            textInput_password.helperText = "Cannot be empty"
            status = false
        }
        return status
    }
    private fun disableAllHelperTexts(){
        textInput_password.isHelperTextEnabled = false
        textInput_username.isHelperTextEnabled = false
    }

}