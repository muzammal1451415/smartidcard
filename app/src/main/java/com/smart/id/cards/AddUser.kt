package com.smart.id.cards

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.gson.Gson
import com.smart.id.cards.data.Student
import kotlinx.android.synthetic.main.activity_add_user.*
import java.io.IOException


class AddUser : AppCompatActivity() {
    var TAG = "AddUser"
    var filePath:Uri? = null
    var PICK_IMAGE_REQUEST = 11

    // instance for firebase storage and StorageReference
    var storage: FirebaseStorage? = null
    var storageReference: StorageReference? = null
    var database:FirebaseDatabase? = null
    var databaseReference:DatabaseReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_user)
        // get the Firebase  storage reference
        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance("https://smart-id-card-f2eab-default-rtdb.firebaseio.com/")
        databaseReference = database?.getReference("students")
        storageReference = storage?.getReference();

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
    fun onClickAdd(view: View) {
        disableAllTextInputLayouts()
        if(isAllFieldsValid()) {
            var username:String = editTextUserID.text.toString()
            var firstName:String = editTextFirstName.text.toString()
            var lastName:String = editTextLastName.text.toString()
            var emiratesID:String = editTextEmiratedID.text.toString()
            var password:String = editTextPassword.text.toString()
            var userTypeRadioID = radioGroupUserType.checkedRadioButtonId
            var userType = findViewById<RadioButton>(userTypeRadioID).text.toString()
            var genderRadioID = radioGroupGender.checkedRadioButtonId
            var gender = findViewById<RadioButton>(genderRadioID).text.toString()
            var uri = filePath.toString()
            var user = Student(username,firstName,lastName,emiratesID,password,uri,userType,gender)
            var userString:String = Gson().toJson(user)
            Intent(this, FingerPrintScan::class.java).also {
                it.putExtra("from", "add_user")
                it.putExtra("top", "Add User")
                it.putExtra("data",userString)
                it.putExtra("bottom", "User Added Successfully")
                startActivity(it)}
        }
        else{
            updateHelperText()
        }
    }
    fun isAllFieldsValid():Boolean{
        var username:String = editTextUserID.text.toString()
        var firstName:String = editTextFirstName.text.toString()
        var lastName:String = editTextLastName.text.toString()
        var emiratesID:String = editTextEmiratedID.text.toString()
        var password:String = editTextPassword.text.toString()
        Log.i(TAG, "username: $username")
        Log.i(TAG, "firstName: $firstName")
        Log.i(TAG, "lastName: $lastName")
        Log.i(TAG, "emiratesID: $emiratesID")
        Log.i(TAG, "password: $password")
        if(filePath==null){
            return false
        }
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
        Log.i(TAG, "username: $username")
        if(filePath==null){
            Toast.makeText(this,"Please select image",Toast.LENGTH_SHORT).show()
        }
        if(username.equals("")){
            Log.i(TAG, "Enter username")
            textInput_username.helperText = "Required"

        }
        if(firstName.equals("")){
            Log.i(TAG, "Enter firstName")
            textInput_firstname.helperText = "Required"

        }
        if(lastName.equals("")){
            Log.i(TAG, "Enter lastName")
            textInput_lastname.helperText = "Required"

        }
        if(emiratesID.equals("")){
            Log.i(TAG, "Enter emiratesID")
            textInput_emiratesid.helperText = "Required"

        }
        if(password.equals("")){
            Log.i(TAG, "Enter password")
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

    fun onClickProfileImage(view: View) {
        selectImage()

    }

}