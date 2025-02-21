package com.example.finalproject

import android.app.Application
import com.onesignal.OneSignal
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.auth.FirebaseUser


class MyApplication : Application() {

    private val ONESIGNAL_APP_ID = "f63b86c5-27be-4584-bc86-08255699eb18" // Replace with your OneSignal App ID
    private var currentUser: FirebaseUser? = null

    override fun onCreate() {
        super.onCreate()

        // Enable verbose OneSignal logging to debug issues
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE)

        // OneSignal Initialization
        OneSignal.initWithContext(this)
        OneSignal.setAppId(ONESIGNAL_APP_ID)

        Log.d("OneSignal", "MyApplication onCreate: OneSignal initialized.")

        // Initialize Firebase Authentication
        val auth = FirebaseAuth.getInstance()

        // Set a listener for authentication state changes
        auth.addAuthStateListener { firebaseAuth ->
            currentUser = firebaseAuth.currentUser
            Log.d("OneSignal", "AuthStateListener triggered.")

            if (currentUser != null) {
                Log.d("OneSignal", "AuthStateListener: User is signed in. UID: ${currentUser!!.uid}")

                // User is signed in, get the OneSignal ID and save it to the database
                OneSignal.addSubscriptionObserver { stateChanges ->
                    Log.d("OneSignal", "SubscriptionObserver triggered.")

                    Log.d("OneSignal", "SubscriptionObserver: from.isSubscribed: ${stateChanges.from.isSubscribed}")
                    Log.d("OneSignal", "SubscriptionObserver: to.isSubscribed: ${stateChanges.to.isSubscribed}")

                    if (stateChanges.from.isSubscribed != stateChanges.to.isSubscribed && stateChanges.to.isSubscribed) {
                        val oneSignalId = stateChanges.to.userId
                        Log.d("OneSignal", "SubscriptionObserver: oneSignalId: $oneSignalId")

                        if (oneSignalId != null) {
                            saveOneSignalIdToDatabase(currentUser!!.uid, oneSignalId)
                        } else {
                            Log.e("OneSignal", "SubscriptionObserver: oneSignalId is null!")
                        }
                    } else {
                        Log.d("OneSignal", "SubscriptionObserver: Subscription state did not change to subscribed.")
                    }
                }
            } else {
                Log.d("OneSignal", "AuthStateListener: User is not signed in.")
            }
        }
    }

     fun saveOneSignalIdToDatabase(userId: String, oneSignalId: String) {
        Log.d("OneSignal", "saveOneSignalIdToDatabase: Attempting to save oneSignalId: $oneSignalId for userId: $userId")

        val database = FirebaseDatabase.getInstance()
        val userRef = database.getReference("users").child(userId)

        userRef.child("oneSignalId").setValue(oneSignalId)
            .addOnSuccessListener {
                Log.d("OneSignal", "saveOneSignalIdToDatabase: OneSignal ID saved successfully for user: $userId")
            }
            .addOnFailureListener { e ->
                Log.e("OneSignal", "saveOneSignalIdToDatabase: Failed to save OneSignal ID for user: $userId", e)
            }
    }
}