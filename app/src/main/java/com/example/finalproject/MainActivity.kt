package com.example.finalproject

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.example.finalproject.Navigation.navigation
import com.example.finalproject.Screen.login
import com.example.finalproject.Screen.splash
import com.example.finalproject.ViewModel.UserViewModel
import com.example.finalproject.ui.theme.FinalProjectTheme
import com.google.firebase.auth.FirebaseAuth
import android.Manifest
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.finalproject.Model.SharedPref
import com.example.finalproject.ViewModel.ThreadViewModel
import com.onesignal.OneSignal

class MainActivity : ComponentActivity() {
    private val userViewModel: UserViewModel by viewModels()
    private val threadsViewModel: ThreadViewModel by viewModels()
    private lateinit var auth: FirebaseAuth

    // Register the permission callback
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Start listening for followers.
                startListening()
            } else {
                // Permission is denied. Handle accordingly (e.g., show a message).
                // You might want to explain why the permission is needed.
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        auth = FirebaseAuth.getInstance()

        setContent {

            FinalProjectTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    navigation()
                }
            }

            val lifecycleOwner = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    when (event) {
                        Lifecycle.Event.ON_START -> startListening()
                        Lifecycle.Event.ON_STOP -> stopListening()
                        else -> {}
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Check if the user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // User is signed in, check for notification permission
            checkNotificationPermission()
        } else {
            // No user is signed in, handle accordingly (e.g., navigate to login screen)
        }
    }


    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // Permission is already granted. Start listening for followers.
            } else {
                // Permission is not granted. Request it.
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun startListening() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            threadsViewModel.listenForNewThreads(this)
            userViewModel.listenForNewFollowers(currentUser.uid,this)
        }
    }
    private fun stopListening() {

    }

}

