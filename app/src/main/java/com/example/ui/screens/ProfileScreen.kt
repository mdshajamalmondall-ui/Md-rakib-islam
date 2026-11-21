package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MedicalInformation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.auth.signOut
import com.example.data.model.MedicationItem
import com.example.data.model.SavedNote
import com.example.data.model.UiState
import com.example.data.model.UserProfile
import com.example.ui.theme.ClinicalMonoFontFamily
import com.example.ui.viewmodel.ProfileViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    notesState: UiState<List<SavedNote>>,
    medsState: UiState<List<MedicationItem>>,
    modifier: Modifier = Modifier
) {
    val profileState by viewModel.profileState.collectAsStateWithLifecycle()
    val saveState by viewModel.saveState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val currentUser = Firebase.auth.currentUser

    var showEditDialog by remember { mutableStateOf(false) }
    var showSignOutConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(saveState.feedbackMessage) {
        val msg = saveState.feedbackMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearFeedback()
        }
    }

    val notesCount = (notesState as? UiState.Success)?.data?.size ?: 0
    val activeMedsCount = (medsState as? UiState.Success)?.data?.count { it.active } ?: 0
    val dosesGivenTotal = (medsState as? UiState.Success)?.data?.sumOf { it.dosesGivenCount } ?: 0L

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 760.dp)
                .fillMaxSize()
        ) {
            Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 1.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "নার্সিং প্রোফাইল",
                        style = MaterialTheme.typography.titleLarge
                    )

                    val currentProfile = (profileState as? UiState.Success)?.data
                    TextButton(
                        onClick = { showEditDialog = true },
                        enabled = profileState is UiState.Success,
                        modifier = Modifier.testTag("profile_edit_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "প্রোফাইল সম্পাদনা",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (currentProfile == null) "তৈরি করুন" else "সম্পাদনা")
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (val state = profileState) {
                    is UiState.Loading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }

                    is UiState.Error -> {
                        Box(
                            Modifier.fillMaxSize().padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    is UiState.Success -> {
                        val profile = state.data ?: UserProfile(
                            userId = currentUser?.uid.orEmpty(),
                            displayName = currentUser?.displayName ?: "নার্সিং ব্যবহারকারী",
                            email = currentUser?.email.orEmpty(),
                            designation = "সিনিয়র স্টাফ নার্স / শিক্ষার্থী"
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Profile Header Card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.large,
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(18.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val photoUrl = currentUser?.photoUrl?.toString()
                                    if (!photoUrl.isNullOrBlank()) {
                                        AsyncImage(
                                            model = photoUrl,
                                            contentDescription = "প্রোফাইল ছবি",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(68.dp)
                                                .clip(CircleShape)
                                        )
                                    } else {
                                        Surface(
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            shape = CircleShape,
                                            modifier = Modifier.size(68.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Filled.Person,
                                                    contentDescription = "প্রোফাইল আইকন",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(34.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(
                                            text = profile.displayName,
                                            style = MaterialTheme.typography.headlineSmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        if (profile.designation.isNotBlank()) {
                                            Text(
                                                text = profile.designation,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        if (profile.email.isNotBlank()) {
                                            Text(
                                                text = profile.email,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }

                            // Quick Stats Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                StatMiniCard(
                                    label = "সেভ করা ফাইল",
                                    value = "$notesCount",
                                    modifier = Modifier.weight(1f)
                                )
                                StatMiniCard(
                                    label = "সক্রিয় ওষুধ",
                                    value = "$activeMedsCount",
                                    modifier = Modifier.weight(1f)
                                )
                                StatMiniCard(
                                    label = "মোট ডোজ লগ",
                                    value = "$dosesGivenTotal",
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // Personal & Clinical Details Card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.medium,
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = "ব্যক্তিগত ও কর্মস্থলের তথ্য",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    ProfileDetailRow(
                                        icon = Icons.Filled.LocalHospital,
                                        label = "প্রতিষ্ঠান / হাসপাতাল",
                                        value = profile.institution.ifBlank { "যোগ করা হয়নি" }
                                    )
                                    ProfileDetailRow(
                                        icon = Icons.Filled.MedicalInformation,
                                        label = "ওয়ার্ড / ডিপার্টমেন্ট",
                                        value = profile.ward.ifBlank { "যোগ করা হয়নি" }
                                    )
                                    ProfileDetailRow(
                                        icon = Icons.Filled.Badge,
                                        label = "BNMC / স্টুডেন্ট আইডি",
                                        value = profile.registrationId.ifBlank { "যোগ করা হয়নি" }
                                    )
                                    if (profile.bio.isNotBlank()) {
                                        HorizontalDivider(
                                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                        )
                                        Text(
                                            text = profile.bio,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            OutlinedButton(
                                onClick = { showSignOutConfirm = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("sign_out_button"),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Logout,
                                    contentDescription = "সাইন-আউট"
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("অ্যাকাউন্ট থেকে সাইন-আউট করুন")
                            }
                        }

                        if (showEditDialog) {
                            EditProfileDialog(
                                initial = profile,
                                isSaving = saveState.isSaving,
                                onDismiss = { showEditDialog = false },
                                onSave = { name, designation, institution, ward, regId, bio ->
                                    viewModel.updateProfile(
                                        displayName = name,
                                        email = profile.email.ifBlank { currentUser?.email.orEmpty() },
                                        designation = designation,
                                        institution = institution,
                                        ward = ward,
                                        registrationId = regId,
                                        bio = bio,
                                        onComplete = { showEditDialog = false }
                                    )
                                }
                            )
                        }
                    }
                }

                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }

    if (showSignOutConfirm) {
        AlertDialog(
            onDismissRequest = { showSignOutConfirm = false },
            title = { Text("সাইন-আউট নিশ্চিত করুন") },
            text = { Text("আপনি কি নিশ্চিতভাবে আপনার Google অ্যাকাউন্ট থেকে সাইন-আউট করতে চান?") },
            confirmButton = {
                Button(
                    onClick = {
                        showSignOutConfirm = false
                        signOut(credentialManager = credentialManager, scope = scope)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("সাইন-আউট")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showSignOutConfirm = false }) {
                    Text("থাকুন")
                }
            }
        )
    }
}

@Composable
private fun StatMiniCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = ClinicalMonoFontFamily,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun ProfileDetailRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun EditProfileDialog(
    initial: UserProfile,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        designation: String,
        institution: String,
        ward: String,
        regId: String,
        bio: String
    ) -> Unit
) {
    var displayName by remember { mutableStateOf(initial.displayName) }
    var designation by remember { mutableStateOf(initial.designation) }
    var institution by remember { mutableStateOf(initial.institution) }
    var ward by remember { mutableStateOf(initial.ward) }
    var regId by remember { mutableStateOf(initial.registrationId) }
    var bio by remember { mutableStateOf(initial.bio) }
    var validationError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("প্রোফাইল তথ্য আপডেট") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("পূর্ণ নাম *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("profile_name_input")
                )
                OutlinedTextField(
                    value = designation,
                    onValueChange = { designation = it },
                    label = { Text("পদবি / কোর্স") },
                    placeholder = { Text("যেমন: সিনিয়র স্টাফ নার্স / BSc Nursing") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("profile_designation_input")
                )
                OutlinedTextField(
                    value = institution,
                    onValueChange = { institution = it },
                    label = { Text("হাসপাতাল / নার্সিং কলেজ") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("profile_institution_input")
                )
                OutlinedTextField(
                    value = ward,
                    onValueChange = { ward = it },
                    label = { Text("ওয়ার্ড / ডিপার্টমেন্ট") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("profile_ward_input")
                )
                OutlinedTextField(
                    value = regId,
                    onValueChange = { regId = it },
                    label = { Text("BNMC রেজিস্ট্রেশন / স্টুডেন্ট আইডি") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("profile_regid_input")
                )
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("নিজের সম্পর্কে নোট") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                if (!validationError.isNullOrBlank()) {
                    Text(
                        text = validationError.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !isSaving,
                onClick = {
                    if (displayName.isBlank()) {
                        validationError = "নাম অবশ্যই লিখতে হবে।"
                    } else {
                        validationError = null
                        onSave(displayName, designation, institution, ward, regId, bio)
                    }
                },
                modifier = Modifier.testTag("profile_save_button")
            ) {
                Text(if (isSaving) "সেভ হচ্ছে..." else "সংরক্ষণ করুন")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("বাতিল")
            }
        }
    )
}
