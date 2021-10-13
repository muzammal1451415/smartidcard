package com.smart.id.cards

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.gson.Gson
import com.smart.id.cards.data.Student
import java.util.*
import java.util.concurrent.Executor

class FingerPrintScan : AppCompatActivity() {
    var topText:String? = null
    var bottomText:String? = null
    var fromActivity:String? = null
    var topScreenTextView:TextView? = null
    var bottomScreenTextView:TextView? = null
    var fingerPrintImageView:ImageView? = null
    var tickImageView:ImageView? = null
    var users:MutableList<Student>  = arrayListOf()
    var TAG = "FingerPrintScan"
    var isUserExtracted = false
    // instance for firebase storage and StorageReference
    var storage: FirebaseStorage? = null
    var storageReference: StorageReference? = null
    var database: FirebaseDatabase? = null
    var databaseReference: DatabaseReference? = null

    // fingerprints auth required variables
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finger_print_scan)
        // get the Firebase  storage reference
        storage = FirebaseStorage.getInstance("gs://smart-id-card-f2eab.appspot.com");
        database = FirebaseDatabase.getInstance("https://smart-id-card-f2eab-default-rtdb.firebaseio.com/")
        databaseReference = database?.getReference("students")
        storageReference = storage?.getReference();
        topScreenTextView = findViewById(R.id.text_screen_name)
        bottomScreenTextView = findViewById(R.id.text_status)
        fingerPrintImageView = findViewById(R.id.iv_finger_print)
        tickImageView = findViewById(R.id.iv_tick)
        topText = intent.getStringExtra("top")
        bottomText = intent.getStringExtra("bottom")
        fromActivity = intent.getStringExtra("from")
        topScreenTextView?.text = topText
        initFingerprintVerification()
        getUser()
        if(checkIfDeviceEnabledBioMetrics()){

            biometricPrompt.authenticate(promptInfo)
        }

    }
    fun initFingerprintVerification(){
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int,
                                                   errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(applicationContext,
                        "Authentication error: $errString", Toast.LENGTH_SHORT)
                        .show()
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)

                    if(fromActivity.equals("student")){
                        Intent(applicationContext,SmartCard::class.java).also {
                            startActivity(it)
                        }
                    }
                    if(fromActivity.equals("add_user")){
                        var student= Gson().fromJson<Student>(intent.getStringExtra("data"),Student::class.java)
                        createUser(student)

                    }
                    if(fromActivity.equals("update_user")){
                        var student= Gson().fromJson<Student>(intent.getStringExtra("data"),Student::class.java)
                        var isUri = intent.getBooleanExtra("isUri",false);
                        updateUser(student,isUri)
                    }
                    if(fromActivity.equals("delete_user")){
                        var username = intent.getStringExtra("username");
                        deleteUser(username!!)
                    }
                    Toast.makeText(applicationContext,
                        "Authentication succeeded!", Toast.LENGTH_SHORT)
                        .show()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(applicationContext, "Authentication failed",
                        Toast.LENGTH_SHORT)
                        .show()
                }
            })

        var sub = "Scan your fingerprint to $topText"
        if(fromActivity!!.equals("student")){
            sub = "Scan your fingerprint to generate student smart card"
        }
        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Fingerprints required!")
            .setSubtitle(sub)
            .setNegativeButtonText("Cancel")
            .build()

    }
    fun checkIfDeviceEnabledBioMetrics():Boolean{
        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS ->
            {

                Log.i(TAG, "App can authenticate using biometrics.")
                return true}
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->{

                Log.e(TAG, "No biometric features available on this device.")
                return false
            }

            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->{
                Log.e(TAG, "Biometric features are currently unavailable.")
                return false
            }

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                // Prompts the user to create credentials that your app accepts.
                val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                    putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                        BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
                }
                startActivityForResult(enrollIntent, 10)
                return false
            }

        }
        return true
    }

    // This method creates User(Student/Teacher in database)
    private fun createUser(student:Student) {
            if(isUserExtracted) {
                var isUserAlreadyExist = false
                for (i in users){
                    if(i.username.equals(student.username)){
                        isUserAlreadyExist=true
                        break
                    }
                }

                if(isUserAlreadyExist){
                    fingerPrintImageView?.visibility = View.GONE
                    tickImageView?.setImageDrawable( getDrawable(R.drawable.ic_error))
                    tickImageView?.visibility = View.VISIBLE
                    bottomScreenTextView?.text = "'${student.username}' already exist, please choose another username"
                }
                else{
                    // Code for showing progressDialog while uploading
                    val progressDialog = ProgressDialog(this)
                    progressDialog.setTitle("Uploading...")
                    progressDialog.show()

                    // Defining the child of storageReference
                    val ref = storageReference
                        ?.child(
                            "studentImages/"
                                    + UUID.randomUUID().toString()
                        )

                    // adding listeners on upload
                    // or failure of image
                    ref?.putFile(Uri.parse(student.imageURL))
                        ?.addOnSuccessListener { it ->// Image uploaded successfully
                            // Dismiss dialog
                            ref.downloadUrl.addOnSuccessListener {
                                progressDialog.setMessage("Uploading data...")
                                var std = student
                                std.imageURL = it.toString()
                                databaseReference?.child(std.username)?.setValue(std)
                                    ?.addOnSuccessListener {
                                        progressDialog.dismiss()
                                        Toast
                                            .makeText(
                                                this@FingerPrintScan,
                                                "User Created Successfully!",
                                                Toast.LENGTH_SHORT
                                            )
                                            .show()
                                        fingerPrintImageView?.visibility = View.GONE
                                        tickImageView?.setImageDrawable( getDrawable(R.drawable.green_tick))
                                        tickImageView?.visibility = View.VISIBLE
                                        bottomScreenTextView?.text = bottomText

                                    }?.addOnFailureListener {
                                        progressDialog.dismiss()
                                        Toast
                                            .makeText(
                                                this@FingerPrintScan,
                                                "Failed: " + it.message,
                                                Toast.LENGTH_SHORT
                                            )
                                            .show()
                                    }
                                Log.i("image", "URL:" + it.toString())
                            }


                        }
                        ?.addOnFailureListener { e -> // Error, Image not uploaded
                            progressDialog.dismiss()
                            Toast
                                .makeText(
                                    this@FingerPrintScan,
                                    "Failed " + e.message,
                                    Toast.LENGTH_SHORT
                                )
                                .show()
                        }
                        ?.addOnProgressListener { taskSnapshot ->

                            // Progress Listener for loading
                            // percentage on the dialog box
                            val progress = (100.0
                                    * taskSnapshot.bytesTransferred
                                    / taskSnapshot.totalByteCount)
                            progressDialog.setMessage(
                                "Uploaded "
                                        + progress.toInt() + "%"
                            )
                        }
                }
            }else{
                getUser()
            }
        }
    private fun updateUser(student:Student,isUri:Boolean) {
        // Code for showing progressDialog while uploading
        val progressDialog = ProgressDialog(this)
        if(isUserExtracted) {
            var isUserAlreadyExist = false
            for (i in users){
                if(i.username.equals(student.username)){
                    isUserAlreadyExist=true
                    break
                }
            }

//            if(isUserAlreadyExist){
//                fingerPrintImageView?.visibility = View.GONE
//                tickImageView?.setImageDrawable( getDrawable(R.drawable.ic_error))
//                tickImageView?.visibility = View.VISIBLE
//                bottomScreenTextView?.text = "'${student.username}' already exist, please choose another username"
//            }
//            else{
                // If user has changed the profile image
                if(isUri) {


                    progressDialog.setTitle("Uploading...")
                    progressDialog.show()

                    // Defining the child of storageReference
                    val ref = storageReference
                            ?.child(
                                    "studentImages/"
                                            + UUID.randomUUID().toString()
                            )

                    // Uploading image and then adding data to the database along with image url
                    ref?.putFile(Uri.parse(student.imageURL))
                            ?.addOnSuccessListener { it ->// Image uploaded successfully
                                // Dismiss dialog
                                ref.downloadUrl.addOnSuccessListener {
                                    progressDialog.setMessage("Uploading data...")
                                    var std = student
                                    std.imageURL = it.toString()
                                    databaseReference?.child(std.username)?.setValue(std)
                                            ?.addOnSuccessListener {
                                                progressDialog.dismiss()
                                                Toast
                                                        .makeText(
                                                                this@FingerPrintScan,
                                                                "User Updated Successfully!",
                                                                Toast.LENGTH_SHORT
                                                        )
                                                        .show()
                                                fingerPrintImageView?.visibility = View.GONE
                                                tickImageView?.setImageDrawable(getDrawable(R.drawable.green_tick))
                                                tickImageView?.visibility = View.VISIBLE
                                                bottomScreenTextView?.text = bottomText

                                            }?.addOnFailureListener {
                                                progressDialog.dismiss()
                                                Toast
                                                        .makeText(
                                                                this@FingerPrintScan,
                                                                "Failed: " + it.message,
                                                                Toast.LENGTH_SHORT
                                                        )
                                                        .show()
                                            }
                                    Log.i("image", "URL:" + it.toString())
                                }


                            }
                            ?.addOnFailureListener { e -> // Error, Image not uploaded
                                progressDialog.dismiss()
                                Toast
                                        .makeText(
                                                this@FingerPrintScan,
                                                "Failed " + e.message,
                                                Toast.LENGTH_SHORT
                                        )
                                        .show()
                            }
                            ?.addOnProgressListener { taskSnapshot ->

                                // Progress Listener for loading
                                // percentage on the dialog box
                                val progress = (100.0
                                        * taskSnapshot.bytesTransferred
                                        / taskSnapshot.totalByteCount)
                                progressDialog.setMessage(
                                        "Uploaded "
                                                + progress.toInt() + "%"
                                )
                            }
                }
                // this block will run when user didn't change its profile image
                else{
                    progressDialog.setMessage("Updating user info...")
                    progressDialog.show()
                    databaseReference?.child(student.username)?.setValue(student)
                            ?.addOnSuccessListener {
                                progressDialog.dismiss()
                                Toast
                                        .makeText(
                                                this@FingerPrintScan,
                                                "User Updated Successfully!",
                                                Toast.LENGTH_SHORT
                                        )
                                        .show()
                                fingerPrintImageView?.visibility = View.GONE
                                tickImageView?.setImageDrawable( getDrawable(R.drawable.green_tick))
                                tickImageView?.visibility = View.VISIBLE
                                bottomScreenTextView?.text = bottomText

                            }?.addOnFailureListener {
                                progressDialog.dismiss()
                                Toast
                                        .makeText(
                                                this@FingerPrintScan,
                                                "Failed: " + it.message,
                                                Toast.LENGTH_SHORT
                                        )
                                        .show()
                            }
                }
           // }
        }else{
            getUser()
        }
    }

    // Getting all Users list from database
    private fun getUser(){
        databaseReference?.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                Toast
                    .makeText(
                        this@FingerPrintScan,
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
            }
        })
    }
    private fun deleteUser(username:String){
        if(isUserExtracted) {
            var isUserAlreadyExist = false
            for (i in users) {
                if (i.username.equals(username)) {
                    isUserAlreadyExist = true
                    break
                }
            }
            if(isUserAlreadyExist){
                val progressDialog = ProgressDialog(this)
                progressDialog.setMessage("Deleting record...")
                progressDialog.show()
                databaseReference?.child(username)?.removeValue()?.addOnSuccessListener {
                    progressDialog.dismiss()
                    Toast
                            .makeText(
                                    this@FingerPrintScan,
                                    "User deleted Successfully!",
                                    Toast.LENGTH_SHORT
                            )
                            .show()
                    fingerPrintImageView?.visibility = View.GONE
                    tickImageView?.setImageDrawable( getDrawable(R.drawable.green_tick))
                    tickImageView?.visibility = View.VISIBLE
                    bottomScreenTextView?.text = bottomText

                }?.addOnFailureListener {
                    progressDialog.dismiss()
                    Toast
                            .makeText(
                                    this@FingerPrintScan,
                                    "Failed: " + it.message,
                                    Toast.LENGTH_SHORT
                            )
                            .show()
                }
            }
            else{
                Toast
                        .makeText(
                                this@FingerPrintScan,
                                "User does not exist!",
                                Toast.LENGTH_SHORT
                        )
                        .show()
                onBackPressed()
            }
        }


    }


}