package db

import com.google.cloud.firestore.FirestoreOptions

object FirebaseEngine {

    private val db = FirestoreOptions.getDefaultInstance().service

    fun saveMessageToAdmin(userName: String, userFirstName: String, userLastName: String, messageText: String) {

        val timeStampUTC = System.currentTimeMillis() / 1000L

        val docRef = db
            .collection("messages_log")
            .document("$timeStampUTC")
        val data = hashMapOf<String, Any>()
        data["username"] = userName
        data["firstname"] = userFirstName
        data["lastname"] = userLastName
        data["message"] = messageText
        data["UTCTimeStamp"] = timeStampUTC
        data["ServerTimeStamp"] = com.google.cloud.firestore.FieldValue.serverTimestamp()
        val result = docRef.set(data, com.google.cloud.firestore.SetOptions.merge())
        result.get()
    }

    fun logAdConditionsRequest(userName: String, userFirstName: String, userLastName: String) {
        val timeStampUTC = System.currentTimeMillis() / 1000L
        val docRef = db
            .collection("ad_view_log")
            .document("$timeStampUTC")
        val data = hashMapOf<String, Any>()
        data["username"] = userName
        data["firstname"] = userFirstName
        data["lastname"] = userLastName
        data["UTCTimeStamp"] = timeStampUTC
        data["ServerTimeStamp"] = com.google.cloud.firestore.FieldValue.serverTimestamp()
        val result = docRef.set(data, com.google.cloud.firestore.SetOptions.merge())
        result.get()
    }
}