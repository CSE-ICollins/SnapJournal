package com.example.snapjournal.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.snapjournal.data.JournalEntry
import com.example.snapjournal.data.MediaType
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private object Routes {
    const val Home = "home"
    const val Create = "create"
    const val Detail = "detail/{entryId}"
    const val Edit = "edit/{entryId}"

    fun detail(entryId: Int) = "detail/$entryId"
    fun edit(entryId: Int) = "edit/$entryId"
}

@Composable
fun SnapJournalApp(viewModel: JournalViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.Home
    ) {
        composable(Routes.Home) {
            HomeScreen(
                viewModel = viewModel,
                onCreateEntry = { navController.navigate(Routes.Create) },
                onEntryClick = { navController.navigate(Routes.detail(it)) }
            )
        }
        composable(Routes.Create) {
            CreateEntryScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onSaved = {
                    navController.popBackStack(
                        route = Routes.Home,
                        inclusive = false
                    )
                }
            )
        }
        composable(
            route = Routes.Detail,
            arguments = listOf(navArgument("entryId") { type = NavType.IntType })
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getInt("entryId") ?: return@composable
            DetailScreen(
                viewModel = viewModel,
                entryId = entryId,
                onBack = { navController.popBackStack() },
                onEdit = { navController.navigate(Routes.edit(entryId)) },
                onDeleted = {
                    navController.popBackStack(
                        route = Routes.Home,
                        inclusive = false
                    )
                }
            )
        }
        composable(
            route = Routes.Edit,
            arguments = listOf(navArgument("entryId") { type = NavType.IntType })
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getInt("entryId") ?: return@composable
            EditEntryScreen(
                viewModel = viewModel,
                entryId = entryId,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
                onDeleted = {
                    navController.popBackStack(
                        route = Routes.Home,
                        inclusive = false
                    )
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreen(
    viewModel: JournalViewModel,
    onCreateEntry: () -> Unit,
    onEntryClick: (Int) -> Unit
) {
    val entries by viewModel.entries.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("SnapJournal")
                        Text(
                            text = "${entries.size} saved ${if (entries.size == 1) "entry" else "entries"}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateEntry,
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null
                    )
                },
                text = { Text("New Journal Entry") }
            )
        }
    ) { innerPadding ->
        if (entries.isEmpty()) {
            EmptyJournal(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = entries,
                    key = { it.id }
                ) { entry ->
                    JournalEntryRow(
                        entry = entry,
                        onClick = { onEntryClick(entry.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyJournal(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "No entries yet",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Tap New Journal Entry to capture your first memory.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun JournalEntryRow(
    entry: JournalEntry,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            JournalMediaThumbnail(
                imagePath = entry.imagePath,
                mediaType = entry.mediaType,
                modifier = Modifier
                    .size(88.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = formatJournalDate(context, entry.date),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateEntryScreen(
    viewModel: JournalViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val context = LocalContext.current
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var imagePath by rememberSaveable { mutableStateOf<String?>(null) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var videoCapture by remember { mutableStateOf<VideoCapture<Recorder>?>(null) }
    var activeRecording by remember { mutableStateOf<Recording?>(null) }
    var cameraError by remember { mutableStateOf<String?>(null) }
    var entrySaved by remember { mutableStateOf(false) }
    var lensFacing by rememberSaveable { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    var mediaTypeName by rememberSaveable { mutableStateOf(MediaType.PHOTO.name) }
    var flashEnabled by rememberSaveable { mutableStateOf(false) }
    var isRecording by rememberSaveable { mutableStateOf(false) }
    val latestImagePath by rememberUpdatedState(imagePath)
    val latestEntrySaved by rememberUpdatedState(entrySaved)
    var hasCameraPermission by remember {
        mutableStateOf(hasCameraPermission(context))
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            activeRecording?.stop()
            if (!latestEntrySaved) {
                latestImagePath?.let { File(it).delete() }
            }
        }
    }

    val selectedMediaType = if (mediaTypeName == MediaType.VIDEO.name) {
        MediaType.VIDEO
    } else {
        MediaType.PHOTO
    }

    val saveEntry = {
        val savedImagePath = imagePath
        if (title.isBlank()) {
            Toast.makeText(context, "Add a title first.", Toast.LENGTH_SHORT).show()
        } else if (savedImagePath == null) {
            Toast.makeText(context, "Capture media first.", Toast.LENGTH_SHORT).show()
        } else {
            entrySaved = true
            viewModel.addEntry(
                title = title,
                description = description,
                imagePath = savedImagePath,
                mediaType = selectedMediaType,
                date = System.currentTimeMillis(),
                onSaved = onSaved
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Journal Entry") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = saveEntry) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Save"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CameraCapturePanel(
                hasCameraPermission = hasCameraPermission,
                imagePath = imagePath,
                cameraError = cameraError,
                lensFacing = lensFacing,
                mediaType = selectedMediaType,
                flashEnabled = flashEnabled,
                isRecording = isRecording,
                onGrantPermission = {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                },
                onImageCaptureReady = {
                    imageCapture = it
                    if (it != null) {
                        videoCapture = null
                    }
                },
                onVideoCaptureReady = {
                    videoCapture = it
                    if (it != null) {
                        imageCapture = null
                    }
                },
                onCameraError = { cameraError = it },
                onMediaTypeChange = {
                    mediaTypeName = it.name
                    imageCapture = null
                    videoCapture = null
                    cameraError = null
                },
                onSwitchCamera = {
                    lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                        CameraSelector.LENS_FACING_FRONT
                    } else {
                        CameraSelector.LENS_FACING_BACK
                    }
                    imageCapture = null
                    videoCapture = null
                    cameraError = null
                },
                onToggleFlash = {
                    flashEnabled = !flashEnabled
                    imageCapture = null
                    videoCapture = null
                    cameraError = null
                },
                onRetakePhoto = {
                    imagePath?.let { File(it).delete() }
                    imagePath = null
                    imageCapture = null
                    videoCapture = null
                },
                onSaveEntry = saveEntry,
                onCapturePhoto = {
                    capturePhoto(
                        context = context,
                        imageCapture = imageCapture,
                        onPhotoSaved = { newPath ->
                            imagePath?.let { File(it).delete() }
                            imagePath = newPath
                            Toast.makeText(context, "Photo captured.", Toast.LENGTH_SHORT).show()
                        },
                        onError = {
                            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                        }
                    )
                },
                onStartVideo = {
                    activeRecording = startVideoRecording(
                        context = context,
                        videoCapture = videoCapture,
                        onRecordingStarted = {
                            isRecording = true
                            Toast.makeText(context, "Recording started.", Toast.LENGTH_SHORT).show()
                        },
                        onVideoSaved = { newPath ->
                            activeRecording = null
                            isRecording = false
                            imagePath?.let { File(it).delete() }
                            imagePath = newPath
                            Toast.makeText(context, "Video captured.", Toast.LENGTH_SHORT).show()
                        },
                        onError = {
                            activeRecording = null
                            isRecording = false
                            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                        }
                    )
                },
                onStopVideo = {
                    activeRecording?.stop()
                    activeRecording = null
                    isRecording = false
                }
            )
            JournalEntryFields(
                title = title,
                description = description,
                onTitleChange = { title = it },
                onDescriptionChange = { description = it }
            )
        }
    }
}

@Composable
private fun CameraCapturePanel(
    hasCameraPermission: Boolean,
    imagePath: String?,
    cameraError: String?,
    lensFacing: Int,
    mediaType: MediaType,
    flashEnabled: Boolean,
    isRecording: Boolean,
    onGrantPermission: () -> Unit,
    onImageCaptureReady: (ImageCapture?) -> Unit,
    onVideoCaptureReady: (VideoCapture<Recorder>?) -> Unit,
    onCameraError: (String) -> Unit,
    onMediaTypeChange: (MediaType) -> Unit,
    onSwitchCamera: () -> Unit,
    onToggleFlash: () -> Unit,
    onRetakePhoto: () -> Unit,
    onSaveEntry: () -> Unit,
    onCapturePhoto: () -> Unit,
    onStartVideo: () -> Unit,
    onStopVideo: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
            contentAlignment = Alignment.Center
        ) {
            when {
                !hasCameraPermission -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Camera permission is needed.")
                        Button(onClick = onGrantPermission) {
                            Text("Grant Permission")
                        }
                    }
                }
                cameraError != null -> {
                    Text(
                        text = cameraError,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.error
                    )
                }
                imagePath != null -> {
                    JournalMediaDisplay(
                        imagePath = imagePath,
                        mediaType = mediaType.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                else -> {
                    CameraPreview(
                        modifier = Modifier.fillMaxSize(),
                        lensFacing = lensFacing,
                        mediaType = mediaType,
                        flashEnabled = flashEnabled,
                        onImageCaptureReady = onImageCaptureReady,
                        onVideoCaptureReady = onVideoCaptureReady,
                        onCameraError = onCameraError
                    )
                }
            }
        }
        if (imagePath == null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { onMediaTypeChange(MediaType.PHOTO) },
                    enabled = !isRecording,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (mediaType == MediaType.PHOTO) "Photo Selected" else "Photo")
                }
                Button(
                    onClick = { onMediaTypeChange(MediaType.VIDEO) },
                    enabled = !isRecording,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (mediaType == MediaType.VIDEO) "Video Selected" else "Video")
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        if (mediaType == MediaType.PHOTO) {
                            onCapturePhoto()
                        } else if (isRecording) {
                            onStopVideo()
                        } else {
                            onStartVideo()
                        }
                    },
                    enabled = hasCameraPermission && cameraError == null,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        when {
                            mediaType == MediaType.PHOTO -> "Capture Photo"
                            isRecording -> "Stop Recording"
                            else -> "Record Video"
                        }
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TextButton(
                    onClick = onSwitchCamera,
                    enabled = hasCameraPermission && !isRecording,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Cameraswitch,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (lensFacing == CameraSelector.LENS_FACING_BACK) "Front Camera" else "Back Camera")
                }
                TextButton(
                    onClick = onToggleFlash,
                    enabled = hasCameraPermission && !isRecording,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (flashEnabled) "Flash On" else "Flash Off")
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onRetakePhoto,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Retake")
                }
                Button(
                    onClick = onSaveEntry,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save")
                }
            }
        }
    }
}

@Composable
private fun JournalEntryFields(
    title: String,
    description: String,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit
) {
    OutlinedTextField(
        value = title,
        onValueChange = onTitleChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Title") },
        singleLine = true
    )
    OutlinedTextField(
        value = description,
        onValueChange = onDescriptionChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        label = { Text("Notes") },
        minLines = 5
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailScreen(
    viewModel: JournalViewModel,
    entryId: Int,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDeleted: () -> Unit
) {
    val entryFlow = remember(entryId) { viewModel.entry(entryId) }
    val entry by entryFlow.collectAsState(initial = null)
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(entry?.title ?: "Entry") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onEdit,
                        enabled = entry != null
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit"
                        )
                    }
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        enabled = entry != null
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        val currentEntry = entry
        if (currentEntry == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Entry not found.")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                JournalMediaDisplay(
                    imagePath = currentEntry.imagePath,
                    mediaType = currentEntry.mediaType,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = currentEntry.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = formatJournalDate(context, currentEntry.date),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Notes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = currentEntry.description.ifBlank { "No notes added." },
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }

    if (showDeleteDialog && entry != null) {
        ConfirmDeleteDialog(
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                val currentEntry = entry
                if (currentEntry != null) {
                    showDeleteDialog = false
                    viewModel.deleteEntry(currentEntry, onDeleted)
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditEntryScreen(
    viewModel: JournalViewModel,
    entryId: Int,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    onDeleted: () -> Unit
) {
    val entryFlow = remember(entryId) { viewModel.entry(entryId) }
    val entry by entryFlow.collectAsState(initial = null)
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(entry?.id) {
        entry?.let {
            title = it.title
            description = it.description
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Entry") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val currentEntry = entry ?: return@IconButton
                            if (title.isBlank()) return@IconButton
                            viewModel.updateEntry(
                                entry = currentEntry,
                                title = title,
                                description = description,
                                onSaved = onSaved
                            )
                        },
                        enabled = entry != null && title.isNotBlank()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Save"
                        )
                    }
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        enabled = entry != null
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        val currentEntry = entry
        if (currentEntry == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Entry not found.")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                JournalMediaThumbnail(
                    imagePath = currentEntry.imagePath,
                    mediaType = currentEntry.mediaType,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                JournalEntryFields(
                    title = title,
                    description = description,
                    onTitleChange = { title = it },
                    onDescriptionChange = { description = it }
                )
            }
        }
    }

    if (showDeleteDialog && entry != null) {
        ConfirmDeleteDialog(
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                val currentEntry = entry
                if (currentEntry != null) {
                    showDeleteDialog = false
                    viewModel.deleteEntry(currentEntry, onDeleted)
                }
            }
        )
    }
}

@Composable
private fun ConfirmDeleteDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete entry?") },
        text = { Text("This removes the journal entry and its stored photo.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun CameraPreview(
    modifier: Modifier = Modifier,
    lensFacing: Int,
    mediaType: MediaType,
    flashEnabled: Boolean,
    onImageCaptureReady: (ImageCapture?) -> Unit,
    onVideoCaptureReady: (VideoCapture<Recorder>?) -> Unit,
    onCameraError: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier
    )

    DisposableEffect(lifecycleOwner, lensFacing, mediaType, flashEnabled) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val mainExecutor = ContextCompat.getMainExecutor(context)
        val listener = Runnable {
            runCatching {
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(lensFacing)
                    .build()

                cameraProvider.unbindAll()
                if (mediaType == MediaType.PHOTO) {
                    val imageCapture = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .setFlashMode(
                            if (flashEnabled) {
                                ImageCapture.FLASH_MODE_ON
                            } else {
                                ImageCapture.FLASH_MODE_OFF
                            }
                        )
                        .build()

                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageCapture
                    )
                    onImageCaptureReady(imageCapture)
                    onVideoCaptureReady(null)
                } else {
                    val recorder = Recorder.Builder()
                        .setQualitySelector(QualitySelector.from(Quality.HD))
                        .build()
                    val videoCapture = VideoCapture.withOutput(recorder)
                    val camera = cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        videoCapture
                    )
                    camera.cameraControl.enableTorch(flashEnabled)
                    onImageCaptureReady(null)
                    onVideoCaptureReady(videoCapture)
                }
            }.onFailure {
                onCameraError("Camera unavailable: ${it.localizedMessage ?: "unknown error"}")
            }
        }

        cameraProviderFuture.addListener(listener, mainExecutor)

        onDispose {
            runCatching {
                cameraProviderFuture.get().unbindAll()
            }
        }
    }
}

@Composable
private fun JournalMediaDisplay(
    imagePath: String,
    mediaType: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    if (isVideo(mediaType)) {
        JournalVideo(
            videoPath = imagePath,
            modifier = modifier
        )
    } else {
        JournalMediaThumbnail(
            imagePath = imagePath,
            mediaType = mediaType,
            modifier = modifier,
            contentScale = contentScale
        )
    }
}

@Composable
private fun JournalMediaThumbnail(
    imagePath: String,
    mediaType: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    var imageBitmap by remember(imagePath, mediaType) {
        mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null)
    }

    LaunchedEffect(imagePath, mediaType) {
        imageBitmap = withContext(Dispatchers.IO) {
            if (isVideo(mediaType)) {
                loadVideoFrame(imagePath)?.asImageBitmap()
            } else {
                BitmapFactory.decodeFile(imagePath)?.asImageBitmap()
            }
        }
    }

    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        val bitmap = imageBitmap
        if (bitmap == null) {
            Text(
                text = if (isVideo(mediaType)) "No video" else "No photo",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    bitmap = bitmap,
                    contentDescription = "Journal media",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = contentScale
                )
                if (isVideo(mediaType)) {
                    Text(
                        text = "VIDEO",
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.55f))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun JournalVideo(
    videoPath: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    AndroidView(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        factory = {
            VideoView(context).apply {
                setVideoURI(Uri.fromFile(File(videoPath)))
                setOnPreparedListener { player ->
                    player.isLooping = true
                    start()
                }
            }
        },
        update = { videoView ->
            videoView.setVideoURI(Uri.fromFile(File(videoPath)))
            videoView.setOnPreparedListener { player ->
                player.isLooping = true
                videoView.start()
            }
        }
    )
}

private fun capturePhoto(
    context: Context,
    imageCapture: ImageCapture?,
    onPhotoSaved: (String) -> Unit,
    onError: (String) -> Unit
) {
    val capture = imageCapture
    if (capture == null) {
        onError("Camera is still getting ready.")
        return
    }

    val photoFile = createPhotoFile(context)
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    capture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                onPhotoSaved(photoFile.absolutePath)
            }

            override fun onError(exception: ImageCaptureException) {
                photoFile.delete()
                onError("Photo failed: ${exception.localizedMessage ?: "unknown error"}")
            }
        }
    )
}

private fun startVideoRecording(
    context: Context,
    videoCapture: VideoCapture<Recorder>?,
    onRecordingStarted: () -> Unit,
    onVideoSaved: (String) -> Unit,
    onError: (String) -> Unit
): Recording? {
    val capture = videoCapture
    if (capture == null) {
        onError("Video camera is still getting ready.")
        return null
    }

    val videoFile = createVideoFile(context)
    val outputOptions = FileOutputOptions.Builder(videoFile).build()

    return capture.output
        .prepareRecording(context, outputOptions)
        .start(ContextCompat.getMainExecutor(context)) { event ->
            when (event) {
                is VideoRecordEvent.Start -> onRecordingStarted()
                is VideoRecordEvent.Finalize -> {
                    if (event.hasError()) {
                        videoFile.delete()
                        onError("Video failed with error code ${event.error}.")
                    } else {
                        onVideoSaved(videoFile.absolutePath)
                    }
                }
            }
        }
}

private fun createPhotoFile(context: Context): File {
    val photoDirectory = File(context.filesDir, "journal_photos").apply {
        mkdirs()
    }
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(Date())
    return File(photoDirectory, "Photo_$timestamp.jpg")
}

private fun createVideoFile(context: Context): File {
    val videoDirectory = File(context.filesDir, "journal_videos").apply {
        mkdirs()
    }
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(Date())
    return File(videoDirectory, "Video_$timestamp.mp4")
}

private fun loadVideoFrame(videoPath: String): android.graphics.Bitmap? {
    val retriever = MediaMetadataRetriever()
    return try {
        retriever.setDataSource(videoPath)
        retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
    } catch (_: RuntimeException) {
        null
    } finally {
        retriever.release()
    }
}

private fun isVideo(mediaType: String): Boolean {
    return mediaType == MediaType.VIDEO.name
}

private fun hasCameraPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED
}

private fun formatJournalDate(context: Context, millis: Long): String {
    val locale = context.resources.configuration.locales[0] ?: Locale.getDefault()
    return SimpleDateFormat("MMMM d, yyyy", locale).format(Date(millis))
}
