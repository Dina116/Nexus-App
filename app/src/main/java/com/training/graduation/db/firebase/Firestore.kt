package com.training.graduation.db.firebase

import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.Firebase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.ktx.firestore
import com.training.graduation.db.model.User


fun getCollection(CollectionName: String): CollectionReference {
    val firestore = Firebase.firestore

    //create collection
    return firestore.collection(CollectionName)

}






