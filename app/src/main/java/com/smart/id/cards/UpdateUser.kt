package com.smart.id.cards

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.gson.Gson
import com.smart.id.cards.data.Student
import kotlinx.android.synthetic.main.activity_add_user.*
import kotlinx.android.synthetic.main.activity_add_user.editTextEmiratedID
import kotlinx.android.synthetic.main.activity_add_user.editTextFirstName
import kotlinx.android.synthetic.main.activity_add_user.editTextLastName
import kotlinx.android.synthetic.main.activity_add_user.editTextPassword
import kotlinx.android.synthetic.main.activity_add_user.editTextUserID
import kotlinx.android.synthetic.main.activity_add_user.iv_profile_image
import kotlinx.android.synthetic.main.activity_add_user.textInput_emiratesid
import kotlinx.android.synthetic.main.activity_add_user.textInput_firstname
import kotlinx.android.synthetic.main.activity_add_user.textInput_lastname
import kotlinx.android.synthetic.main.activity_add_user.textInput_password
import kotlinx.android.synthetic.main.activity_add_user.textInput_username
import kotlinx.android.synthetic.main.activity_update_user.*
import java.io.IOException

class UpdateUser : AppCompatActivity() {
    var TAG = "UpdateUser"
    var PICK_IMAGE_REQUEST = 12
    var filePath: Uri? = null
    var users:MutableList<Student>  = arrayListOf()
    var isUserExtracted = false
    // instance for firebase storage and StorageReference
    var storage: FirebaseStorage? = null
    var profileImageURL:String? = null
    var storageReference: StorageReference? = null
    var database: FirebaseDatabase? = null
    var databaseReference: DatabaseReference? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_user)
        storage = FirebaseStorage.getInstance("gs://smart-id-card-f2eab.appspot.com");
        database = FirebaseDatabase.getInstance("https://smart-id-card-f2eab-default-rtdb.firebaseio.com/")
        databaseReference = database?.getReference("students")
        storageReference = storage?.getReference();
        getUser()
    }

    fun onClickUpdate(view: View) {
        disableAllTextInputLayouts()
        if(isAllFieldsValid()) {
            Intent(this, FingerPrintScan::class.java).also {
                var username:String = editTextUserID.text.toString()
                var firstName:String = editTextFirstName.text.toString()
                var lastName:String = editTextLastName.text.toString()
                var emiratesID:String = editTextEmiratedID.text.toString()
                var password:String = editTextPassword.text.toString()
                var userTypeRadioID = findViewById<RadioGroup>(R.id.radioGroupUserType).checkedRadioButtonId
                var userType = findViewById<RadioButton>(userTypeRadioID).text.toString()
                var genderRadioID = findViewById<RadioGroup>(R.id.radioGroupGender).checkedRadioButtonId
                var gender = findViewById<RadioButton>(genderRadioID).text.toString()
                Log.i(TAG,"Gender: $gender \n UserType: $userType")
                it.putExtra("from", "update_user")
                it.putExtra("top", "Update User")
                it.putExtra("bottom", "User Updated Successfully")

                if(filePath != null){
                    var uri = filePath.toString()
                    var user = Student(username,firstName,lastName,emiratesID,password,uri,userType,gender)
                    var userString:String = Gson().toJson(user)
                    it.putExtra("isUri",true)
                    it.putExtra("data",userString)
                    startActivity(it)
                }else{
                    var uri = profileImageURL!!
                    var user = Student(username,firstName,lastName,emiratesID,password,uri,userType,gender)
                    var userString:String = Gson().toJson(user)
                    it.putExtra("isUri",false)
                    it.putExtra("data",userString)
                    startActivity(it)
                }

            }
        }else{
            updateHelperText()
        }
    }
    fun isAllFieldsValid():Boolean{
        var username:String = editTextUserID.text.toString()
        var firstName:String = editTextFirstName.text.toString()
        var lastName:String = editTextLastName.text.toString()
        var emiratesID:String = editTextEmiratedID.text.toString()
        var password:String = editTextPassword.text.toString()
        Log.i(TAG,"username: $username")
        Log.i(TAG,"firstName: $firstName")
        Log.i(TAG,"lastName: $lastName")
        Log.i(TAG,"emiratesID: $emiratesID")
        Log.i(TAG,"password: $password")
        if(username.equals("")){

            return false
        }
        if(firstName.equals("")){

            return false
        }
        if(lastName.equals("")){

            return false
        }
        if(emiratesID.equals("")){

            return false
        }
        if(password.equals("")){

            return false
        }
        return true
    }
    fun updateHelperText(){

        var username:String = editTextUserID.text.toString()
        var firstName:String = editTextFirstName.text.toString()
        var lastName:String = editTextLastName.text.toString()
        var emiratesID:String = editTextEmiratedID.text.toString()
        var password:String = editTextPassword.text.toString()
        Log.i(TAG,"username: $username")
        if(username.equals("")){
            Log.i(TAG,"Enter username")
            textInput_username.helperText = "Required"

        }
        if(firstName.equals("")){
            Log.i(TAG,"Enter firstName")
            textInput_firstname.helperText = "Required"

        }
        if(lastName.equals("")){
            Log.i(TAG,"Enter lastName")
            textInput_lastname.helperText = "Required"

        }
        if(emiratesID.equals("")){
            Log.i(TAG,"Enter emiratesID")
            textInput_emiratesid.helperText = "Required"

        }
        if(password.equals("")){
            Log.i(TAG,"Enter password")
            textInput_password.helperText = "Required"

        }

    }
    fun disableAllTextInputLayouts(){
        textInput_username.isHelperTextEnabled = false
        textInput_firstname.isHelperTextEnabled = false
        textInput_lastname.isHelperTextEnabled = false
        textInput_emiratesid.isHelperTextEnabled = false
        textInput_password.isHelperTextEnabled = false
    }

    private fun selectImage() {

        // Defining Implicit Intent to mobile gallery
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
                Intent.createChooser(
                        intent,
                        "Select Image from here..."),
                PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int,
                                  resultCode: Int,
                                  data: Intent?) {
        super.onActivityResult(requestCode,
                resultCode,
                data)

        // checking request code and result code
        // if request code is PICK_IMAGE_REQUEST and
        // resultCode is RESULT_OK
        // then set image in the image view
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {

            // Get the Uri of data
            filePath = data.data!!
            try {

                // Setting image on image view using Bitmap
                val bitmap = MediaStore.Images.Media
                        .getBitmap(
                                contentResolver,
                                filePath!!)
                iv_profile_image.setImageBitmap(bitmap)
            } catch (e: IOException) {
                // Log the exception
                e.printStackTrace()
            }
        }
    }

    // Getting all Users list from database
    private fun getUser(){
        databaseReference?.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Toast
                    .makeText(
                        this@UpdateUser,
                        "Failed: " + error.message,
                        Toast.LENGTH_SHORT
                    )
                    .show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                isUserExtracted = true
                snapshot.children.forEach {

                    var std = it.getValue(Student::class.java)
                    users.add(std!!)

                    Log.i(TAG,"USERID: "+std?.username)
                }
                var username = intent.getStringExtra("username")
                username?.let{
                    for(i in users){
                        Log.i(TAG,"username: ${i.username}")
                        if(i.username.equals(username)){
                            Log.i(TAG,"username enter: ${i.username}")
                            profileImageURL = i.imageURL
                            Glide.with(applicationContext)
                                .load(i.imageURL)
                                .placeholder(R.drawable.profile_image)
                                .into(iv_profile_image)
                            editTextUserID.setText(i.username)
                            editTextFirstName.setText(i.firstName)
                            editTextLastName.setText(i.lastName)
                            editTextEmiratedID.setText(i.emiratesID)
                            editTextPassword.setText(i.password)
                            var userRadioGroup:RadioGroup = findViewById(R.id.radioGroupUserType)
                            var userRadioGender:RadioGroup= findViewById(R.id.radioGroupGender)
                            if(i.userType.equals("Teacher")){
                                userRadioGroup.check(R.id.teacher)
                            }else if(i.userType.equals("Student")){
                                userRadioGroup.check(R.id.student)
                            }else{
                                userRadioGroup.check(R.id.staff)
                            }
                            if(i.geder.equals("Male")){
                                userRadioGender.check(R.id.male)
                            }
                            else{
                                userRadioGender.check(R.id.female)
                            }

                            break
                        }
                        else{
                            Toast.makeText(this@UpdateUser,"username does not exit",Toast.LENGTH_SHORT).show()
                            onBackPressed()
                        }
                    }
                }

            }
        })
    }

    fun onClickProfileImage(view: View) {
        selectImage()
    }
}