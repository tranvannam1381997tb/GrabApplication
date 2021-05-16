package com.example.grabapplication.firebase

import com.google.firebase.database.DataSnapshot

class FirebaseUtils {
    companion object {

        fun getDoubleFromDataSnapshot(snapshot: DataSnapshot, key: String): Double {
            return (snapshot.child(key).getValue(Double::class.java)) ?: (-1).toDouble()
        }

        fun getIntFromDataSnapshot(snapshot: DataSnapshot, key: String): Int {
            return (snapshot.child(key).getValue(Int::class.java)) ?: -1
        }

        fun getStringFromDataSnapshot(snapshot: DataSnapshot, key: String): String {
            return (snapshot.child(key).getValue(String()::class.java)) ?: ""
        }
    }
}