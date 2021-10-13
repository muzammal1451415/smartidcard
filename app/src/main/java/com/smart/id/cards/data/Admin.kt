package com.smart.id.cards.data
import com.google.gson.annotations.SerializedName
 class AdminUser{
     @SerializedName("username") var username:String = ""
     @SerializedName("password") var password:String = ""
     constructor()
     constructor(username:String,password:String){
         this.username = username
         this.password = password
     }


 }
data class Student(
    var username:String,
    var firstName:String,
    var lastName:String,
    var emiratesID:String,
    var password:String,
    var imageURL:String,
    var userType:String,
    var geder:String

){
    constructor():this("","","","","","","","")
}

