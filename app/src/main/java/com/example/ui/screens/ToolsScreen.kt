package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.MedicationFrequency
import com.example.data.model.MedicationItem
import com.example.data.model.MedicationRoute
import com.example.data.model.UiState
import com.example.ui.theme.ClinicalMonoFontFamily
import com.example.ui.viewmodel.ToolsViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsScreen(
    viewModel: ToolsViewModel,
    modifier: Modifier = Modifier
) {
    val medsState by viewModel.medicationsState.collectAsStateWithLifecycle()
    val feedback by viewModel.feedback.collectAsStateWithLifecycle()
    var selectedSubTab by rememberSaveable { mutableIntStateOf(0) }
    var editingMed by remember { mutableStateOf<MedicationItem?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var activeOnlyFilter by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(feedback.message) {
        val msg = feedback.message
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessage()
        }
    }

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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "ক্লিনিক্যাল ও মেডিসিন টুলস",
                        style = MaterialTheme.typography.titleLarge
                    )
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        SegmentedButton(
                            selected = selectedSubTab == 0,
                            onClick = { selectedSubTab = 0 },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.Medication,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            modifier = Modifier.testTag("tools_tab_meds")
                        ) {
                            Text("ওষুধ ও ডোজ")
                        }
                        SegmentedButton(
                            selected = selectedSubTab == 1,
                            onClick = { selectedSubTab = 1 },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.Calculate,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            modifier = Modifier.testTag("tools_tab_calc")
                        ) {
                            Text("ডোজ ক্যালকুলেটর")
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (selectedSubTab == 0) {
                    MedicationsPane(
                        medsState = medsState,
                        activeOnlyFilter = activeOnlyFilter,
                        onToggleFilter = { activeOnlyFilter = !activeOnlyFilter },
                        onMarkDoseGiven = { viewModel.markDoseGiven(it) },
                        onToggleActive = { id, active -> viewModel.toggleActive(id, active) },
                        onEdit = {
                            editingMed = it
                            showAddDialog = true
                        },
                        onDelete = { viewModel.deleteMedication(it) }
                    )
                } else {
                    ClinicalCalculatorsPane()
                }

                if (selectedSubTab == 0) {
                    FloatingActionButton(
                        onClick = {
                            editingMed = null
                            showAddDialog = true
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(18.dp)
                            .testTag("add_medication_fab")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "নতুন ওষুধ যোগ করুন"
                        )
                    }
                }

                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }

    if (showAddDialog) {
        MedicationEditorDialog(
            initial = editingMed,
            isSubmitting = feedback.isSubmitting,
            onDismiss = { showAddDialog = false },
            onSave = { name, dose, route, freq, time, patient, notes, active ->
                viewModel.saveMedication(
                    existingId = editingMed?.id,
                    medicineName = name,
                    dose = dose,
                    route = route,
                    frequency = freq,
                    scheduleTime = time,
                    patientLabel = patient,
                    notes = notes,
                    active = active,
                    onComplete = { showAddDialog = false }
                )
            }
        )
    }
}

@Composable
private fun MedicationsPane(
    medsState: UiState<List<MedicationItem>>,
    activeOnlyFilter: Boolean,
    onToggleFilter: () -> Unit,
    onMarkDoseGiven: (String) -> Unit,
    onToggleActive: (String, Boolean) -> Unit,
    onEdit: (MedicationItem) -> Unit,
    onDelete: (String) -> Unit
) {
    when (medsState) {
        is UiState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is UiState.Error -> {
            Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Text(
                    text = medsState.message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        is UiState.Success -> {
            val allMeds = medsState.data
            val displayed = if (activeOnlyFilter) allMeds.filter { it.active } else allMeds
            val activeCount = allMeds.count { it.active }
            val totalDoses = allMeds.sumOf { it.dosesGivenCount }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 92.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "সক্রিয়: $activeCount টি • মোট ডোজ: $totalDoses বার",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        FilterChip(
                            selected = activeOnlyFilter,
                            onClick = onToggleFilter,
                            label = { Text("শুধু সক্রিয়") },
                            modifier = Modifier.testTag("meds_active_filter_chip")
                        )
                    }
                }

                if (displayed.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Medication,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(40.dp)
                                )
                                Text(
                                    text = "এখনো কোনো ওষুধ যুক্ত করা হয়নি",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "নিচের '+' বাটনে ট্যাপ করে ওষুধের নাম, ডোজ, রুট ও সময়সূচি সেভ করে রাখুন।",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(displayed, key = { it.id }) { item ->
                        MedicationCard(
                            item = item,
                            onMarkDoseGiven = { onMarkDoseGiven(item.id) },
                            onToggleActive = { onToggleActive(item.id, it) },
                            onEdit = { onEdit(item) },
                            onDelete = { onDelete(item.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MedicationCard(
    item: MedicationItem,
    onMarkDoseGiven: () -> Unit,
    onToggleActive: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val timeFormatter = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }
    val lastGivenText = item.lastGivenAt?.toDate()?.let { timeFormatter.format(it) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("medication_card_${item.id}"),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.medicineName,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (item.patientLabel.isNotBlank()) {
                        Text(
                            text = item.patientLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Switch(
                    checked = item.active,
                    onCheckedChange = onToggleActive,
                    modifier = Modifier.testTag("med_active_switch")
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = item.dose,
                        style = MaterialTheme.typography.labelLarge.copy(fontFamily = ClinicalMonoFontFamily),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${item.resolvedRoute().name} • ${item.resolvedFrequency().name}",
                        style = MaterialTheme.typography.labelMedium.copy(fontFamily = ClinicalMonoFontFamily),
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            if (item.scheduleTime.isNotBlank()) {
                Text(
                    text = "সময়সূচি: ${item.scheduleTime}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (item.notes.isNotBlank()) {
                Text(
                    text = item.notes,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = buildString {
                    append("মোট ডোজ দেওয়া হয়েছে: ${item.dosesGivenCount} বার")
                    if (!lastGivenText.isNullOrBlank()) {
                        append(" (সর্বশেষ: $lastGivenText)")
                    }
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onMarkDoseGiven,
                    enabled = item.active,
                    modifier = Modifier.testTag("med_give_dose_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "ডোজ দিয়েছি",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ডোজ দিয়েছি")
                }

                Row {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(48.dp).testTag("med_edit_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "ওষুধ সম্পাদনা করুন"
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(48.dp).testTag("med_delete_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "ওষুধ মুছে ফেলুন",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MedicationEditorDialog(
    initial: MedicationItem?,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        dose: String,
        route: MedicationRoute,
        freq: MedicationFrequency,
        time: String,
        patient: String,
        notes: String,
        active: Boolean
    ) -> Unit
) {
    var name by remember { mutableStateOf(initial?.medicineName.orEmpty()) }
    var dose by remember { mutableStateOf(initial?.dose.orEmpty()) }
    var route by remember { mutableStateOf(initial?.resolvedRoute() ?: MedicationRoute.IV) }
    var frequency by remember { mutableStateOf(initial?.resolvedFrequency() ?: MedicationFrequency.BD) }
    var scheduleTime by remember { mutableStateOf(initial?.scheduleTime.orEmpty()) }
    var patientLabel by remember { mutableStateOf(initial?.patientLabel.orEmpty()) }
    var notes by remember { mutableStateOf(initial?.notes.orEmpty()) }
    var active by remember { mutableStateOf(initial?.active ?: true) }
    var validationError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (initial == null) "নতুন ওষুধ যোগ করুন" else "ওষুধের তথ্য আপডেট")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("ওষুধের নাম *") },
                    placeholder = { Text("যেমন: Inj. Ceftriaxone") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("med_name_input")
                )
                OutlinedTextField(
                    value = dose,
                    onValueChange = { dose = it },
                    label = { Text("ডোজ *") },
                    placeholder = { Text("যেমন: 1 g / 500 mg / 10 IU") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("med_dose_input")
                )

                Text("রুট (Route):", style = MaterialTheme.typography.labelLarge)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    MedicationRoute.entries.forEach { r ->
                        FilterChip(
                            selected = route == r,
                            onClick = { route = r },
                            label = { Text(r.name) }
                        )
                    }
                }

                Text("ফ্রিকোয়েন্সি (Frequency):", style = MaterialTheme.typography.labelLarge)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    MedicationFrequency.entries.forEach { f ->
                        FilterChip(
                            selected = frequency == f,
                            onClick = { frequency = f },
                            label = { Text(f.name) }
                        )
                    }
                }

                OutlinedTextField(
                    value = scheduleTime,
                    onValueChange = { scheduleTime = it },
                    label = { Text("সময়সূচি") },
                    placeholder = { Text("যেমন: 8 AM - 8 PM") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = patientLabel,
                    onValueChange = { patientLabel = it },
                    label = { Text("রোগী / ওয়ার্ড / বেড নং") },
                    placeholder = { Text("যেমন: সার্জিক্যাল ওয়ার্ড, বেড-১২") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("সতর্কতা বা নোট") },
                    placeholder = { Text("যেমন: ধীরে ধীরে IV দিতে হবে") },
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
                enabled = !isSubmitting,
                onClick = {
                    if (name.isBlank() || dose.isBlank()) {
                        validationError = "ওষুধের নাম এবং ডোজ অবশ্যই পূরণ করুন।"
                    } else {
                        validationError = null
                        onSave(name, dose, route, frequency, scheduleTime, patientLabel, notes, active)
                    }
                },
                modifier = Modifier.testTag("med_save_button")
            ) {
                Text(if (isSubmitting) "সেভ হচ্ছে..." else "সংরক্ষণ করুন")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("বাতিল")
            }
        }
    )
}

@Composable
private fun ClinicalCalculatorsPane() {
    var ivVolumeMl by rememberSaveable { mutableStateOf("500") }
    var ivHours by rememberSaveable { mutableStateOf("4") }
    var dropFactor by rememberSaveable { mutableIntStateOf(20) }

    var weightKg by rememberSaveable { mutableStateOf("60") }
    var mgPerKg by rememberSaveable { mutableStateOf("15") }
    var concentrationMgPerMl by rememberSaveable { mutableStateOf("250") }

    var burnWeightKg by rememberSaveable { mutableStateOf("60") }
    var burnTbsaPercent by rememberSaveable { mutableStateOf("25") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. IV Drip Rate Calculator
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.WaterDrop,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "IV ফ্লুইড ড্রিপ রেট ক্যালকুলেটর",
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = ivVolumeMl,
                        onValueChange = { ivVolumeMl = it },
                        label = { Text("মোট ফ্লুইড (mL)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("calc_iv_volume_input")
                    )
                    OutlinedTextField(
                        value = ivHours,
                        onValueChange = { ivHours = it },
                        label = { Text("সময় (ঘণ্টা)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("calc_iv_hours_input")
                    )
                }

                Text("ড্রপ ফ্যাক্টর (gtt/mL):", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(10, 15, 20, 60).forEach { factor ->
                        FilterChip(
                            selected = dropFactor == factor,
                            onClick = { dropFactor = factor },
                            label = { Text("$factor") }
                        )
                    }
                }

                val vol = ivVolumeMl.toDoubleOrNull()
                val hrs = ivHours.toDoubleOrNull()
                val resultText = if (vol != null && hrs != null && vol > 0 && hrs > 0) {
                    val mlPerHr = vol / hrs
                    val gttPerMin = (vol * dropFactor) / (hrs * 60.0)
                    "${gttPerMin.roundToInt()} drops/min  •  ${mlPerHr.roundToInt()} mL/hr"
                } else {
                    "সঠিক সংখ্যা দিন"
                }

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = resultText,
                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = ClinicalMonoFontFamily),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        // 2. Weight-Based Drug Dose Calculator
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Medication,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ওজন অনুযায়ী ওষুধের ডোজ (mg/kg)",
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = weightKg,
                        onValueChange = { weightKg = it },
                        label = { Text("ওজন (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("calc_weight_input")
                    )
                    OutlinedTextField(
                        value = mgPerKg,
                        onValueChange = { mgPerKg = it },
                        label = { Text("ডোজ (mg/kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("calc_mgkg_input")
                    )
                }

                OutlinedTextField(
                    value = concentrationMgPerMl,
                    onValueChange = { concentrationMgPerMl = it },
                    label = { Text("ভায়ালের ঘনত্ব (mg per mL)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                val w = weightKg.toDoubleOrNull()
                val d = mgPerKg.toDoubleOrNull()
                val conc = concentrationMgPerMl.toDoubleOrNull()
                val doseResult = if (w != null && d != null && w > 0 && d > 0) {
                    val totalMg = w * d
                    val mlText = if (conc != null && conc > 0) {
                        String.format(Locale.US, "  •  প্রয়োজন: %.2f mL", totalMg / conc)
                    } else ""
                    String.format(Locale.US, "মোট ডোজ: %.1f mg%s", totalMg, mlText)
                } else {
                    "সঠিক সংখ্যা দিন"
                }

                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = doseResult,
                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = ClinicalMonoFontFamily),
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        // 3. Parkland Burn Fluid Formula (BSN 3411 Unit 3 Lesson 2)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.LocalFireDepartment,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "বার্ন ফ্লুইড ক্যালকুলেটর (Parkland সূত্র)",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                Text(
                    text = "সূত্র: 4 mL × ওজন (kg) × %TBSA (Ringer's Lactate)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = burnWeightKg,
                        onValueChange = { burnWeightKg = it },
                        label = { Text("ওজন (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = burnTbsaPercent,
                        onValueChange = { burnTbsaPercent = it },
                        label = { Text("পোড়া অংশ (% TBSA)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                val bw = burnWeightKg.toDoubleOrNull()
                val tbsa = burnTbsaPercent.toDoubleOrNull()
                val burnResult = if (bw != null && tbsa != null && bw > 0 && tbsa in 1.0..100.0) {
                    val total24h = 4.0 * bw * tbsa
                    val first8h = total24h / 2.0
                    val next16h = total24h / 2.0
                    "২৪ ঘণ্টায় মোট: ${total24h.roundToInt()} mL\n" +
                        "প্রথম ৮ ঘণ্টায়: ${first8h.roundToInt()} mL (${(first8h / 8.0).roundToInt()} mL/hr)\n" +
                        "পরবর্তী ১৬ ঘণ্টায়: ${next16h.roundToInt()} mL (${(next16h / 16.0).roundToInt()} mL/hr)"
                } else {
                    "ওজন (>0) এবং ১-১০০% TBSA দিন"
                }

                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = burnResult,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = ClinicalMonoFontFamily,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}
