package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FolderCopy
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.auth.attemptAutoSignIn
import com.example.auth.authStateFlow
import com.example.data.repository.AiNursingRepository
import com.example.data.repository.NursingDataRepository
import com.example.ui.auth.AuthScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.FilesScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ToolsScreen
import com.example.ui.viewmodel.ChatViewModel
import com.example.ui.viewmodel.FilesViewModel
import com.example.ui.viewmodel.ProfileViewModel
import com.example.ui.viewmodel.ToolsViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

enum class NursingTab(
    val labelBn: String,
    val icon: ImageVector,
    val testTagName: String
) {
    CHAT("চ্যাট", Icons.Filled.AutoAwesome, "nav_tab_chat"),
    TOOLS("টুলস", Icons.Filled.Medication, "nav_tab_tools"),
    FILES("ফাইল", Icons.Filled.FolderCopy, "nav_tab_files"),
    PROFILE("প্রোফাইল", Icons.Filled.Person, "nav_tab_profile")
}

@Composable
fun NursingAiRoot() {
    val context = LocalContext.current
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val currentUser by remember { authStateFlow() }
        .collectAsStateWithLifecycle(initialValue = Firebase.auth.currentUser)

    var checkingAutoSignIn by remember { mutableStateOf(Firebase.auth.currentUser == null) }

    LaunchedEffect(Unit) {
        if (Firebase.auth.currentUser == null) {
            attemptAutoSignIn(
                context = context,
                credentialManager = credentialManager,
                onAuthSuccess = { checkingAutoSignIn = false },
                onUnauthenticated = { checkingAutoSignIn = false },
                scope = this
            )
        } else {
            checkingAutoSignIn = false
        }
    }

    if (checkingAutoSignIn && currentUser == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (currentUser == null) {
        AuthScreen(onAuthSuccess = { checkingAutoSignIn = false })
    } else {
        val uid = currentUser!!.uid
        key(uid) {
            AuthenticatedNursingShell(uid = uid)
        }
    }
}

@Composable
private fun AuthenticatedNursingShell(uid: String) {
    val context = LocalContext.current
    val aiRepository = remember { AiNursingRepository() }
    val dataRepository = remember(context) { NursingDataRepository(context) }

    val chatViewModel: ChatViewModel = viewModel(
        key = "${uid}_chat",
        factory = ChatViewModel.factory(aiRepository, dataRepository)
    )
    val toolsViewModel: ToolsViewModel = viewModel(
        key = "${uid}_tools",
        factory = ToolsViewModel.factory(dataRepository)
    )
    val filesViewModel: FilesViewModel = viewModel(
        key = "${uid}_files",
        factory = FilesViewModel.factory(dataRepository)
    )
    val profileViewModel: ProfileViewModel = viewModel(
        key = "${uid}_profile",
        factory = ProfileViewModel.factory(dataRepository)
    )

    val notesState by filesViewModel.notesState.collectAsStateWithLifecycle()
    val medsState by toolsViewModel.medicationsState.collectAsStateWithLifecycle()

    var selectedTab by rememberSaveable { mutableStateOf(NursingTab.CHAT) }

    BackHandler(enabled = selectedTab != NursingTab.CHAT) {
        selectedTab = NursingTab.CHAT
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 600.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                if (!useRail) {
                    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                        NursingTab.entries.forEach { tab ->
                            NavigationBarItem(
                                selected = selectedTab == tab,
                                onClick = { selectedTab = tab },
                                icon = {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = tab.labelBn
                                    )
                                },
                                label = { Text(tab.labelBn) },
                                modifier = Modifier.testTag(tab.testTagName)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useRail) {
                    NavigationRail(containerColor = MaterialTheme.colorScheme.surface) {
                        NursingTab.entries.forEach { tab ->
                            NavigationRailItem(
                                selected = selectedTab == tab,
                                onClick = { selectedTab = tab },
                                icon = {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = tab.labelBn
                                    )
                                },
                                label = { Text(tab.labelBn) },
                                modifier = Modifier.testTag(tab.testTagName)
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                    when (selectedTab) {
                        NursingTab.CHAT -> ChatScreen(viewModel = chatViewModel)
                        NursingTab.TOOLS -> ToolsScreen(viewModel = toolsViewModel)
                        NursingTab.FILES -> FilesScreen(viewModel = filesViewModel)
                        NursingTab.PROFILE -> ProfileScreen(
                            viewModel = profileViewModel,
                            notesState = notesState,
                            medsState = medsState
                        )
                    }
                }
            }
        }
    }
}
