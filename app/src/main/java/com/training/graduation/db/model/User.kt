package com.training.graduation.db.model

data class User (

    var id: String?=null,
    var name: String? = null,
    var email: String? = null,
    var password: String? = null,
    var host: Boolean = false,
    var hostORfoundation:String?=null,
    var imageUrl: String? = null
){
    companion object{
        const val CollectionNameUser = "users"

    }
}



