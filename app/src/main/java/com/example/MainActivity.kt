package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.data.model.User
import com.example.data.repository.CallRepository
import com.example.data.repository.ChatRepository
import com.example.data.repository.ModerationRepository
import com.example.data.repository.StatusRepository
import com.example.data.repository.UserRepository
import com.example.ui.auth.AuthScreen
import com.example.ui.navigation.AppNavigation
import com.example.ui.theme.FtblChatsTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = FirebaseFirestore.getInstance(getString(R.string.firestore_database_id))
        userRepository = UserRepository(db)
        val chatRepository = ChatRepository(db)
        val statusRepository = StatusRepository(db)
        val callRepository = CallRepository(db)
        val moderationRepository = ModerationRepository(db)

        setContent {
            FtblChatsTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppContent(
                        userRepository = userRepository,
                        chatRepository = chatRepository,
                        statusRepository = statusRepository,
                        callRepository = callRepository,
                        moderationRepository = moderationRepository
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::userRepository.isInitialized && Firebase.auth.currentUser != null) {
            // Update online presence
            kotlinx.coroutines.GlobalScope.launch {
                userRepository.updatePresence(true)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (::userRepository.isInitialized && Firebase.auth.currentUser != null) {
            kotlinx.coroutines.GlobalScope.launch {
                userRepository.updatePresence(false)
            }
        }
    }
}

@Composable
fun AppContent(
    userRepository: UserRepository,
    chatRepository: ChatRepository,
    statusRepository: StatusRepository,
    callRepository: CallRepository,
    moderationRepository: ModerationRepository
) {
    val coroutineScope = rememberCoroutineScope()
    var currentFirebaseUser by remember { mutableStateOf(Firebase.auth.currentUser) }
    var userProfile by remember { mutableStateOf<User?>(null) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentFirebaseUser = auth.currentUser
            if (auth.currentUser == null) {
                userProfile = null
            } else {
                coroutineScope.launch {
                    val profile = userRepository.getUser(auth.currentUser!!.uid)
                    if (profile != null) {
                        userProfile = profile
                    }
                }
            }
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    if (currentFirebaseUser == null || userProfile == null) {
        AuthScreen(
            userRepository = userRepository,
            onAuthSuccess = { user ->
                userProfile = user
            }
        )
    } else {
        AppNavigation(
            currentUser = userProfile!!,
            userRepository = userRepository,
            chatRepository = chatRepository,
            statusRepository = statusRepository,
            callRepository = callRepository,
            moderationRepository = moderationRepository,
            onSignOut = {
                userProfile = null
                currentFirebaseUser = null
            }
        )
    }
}
