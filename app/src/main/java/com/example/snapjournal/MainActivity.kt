package com.example.snapjournal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.snapjournal.ui.JournalViewModel
import com.example.snapjournal.ui.SnapJournalApp
import com.example.snapjournal.ui.theme.SnapJournalTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SnapJournalTheme {
                val context = LocalContext.current
                val journalViewModel: JournalViewModel = viewModel(
                    factory = JournalViewModel.factory(context)
                )
                SnapJournalApp(viewModel = journalViewModel)
            }
        }
    }
}

@Composable
private fun SnapJournalPreviewContent() {
    SnapJournalTheme {
        // The full app preview is backed by Room at runtime.
    }
}

@Preview(showBackground = true)
@Composable
fun SnapJournalPreview() {
    SnapJournalPreviewContent()
}
