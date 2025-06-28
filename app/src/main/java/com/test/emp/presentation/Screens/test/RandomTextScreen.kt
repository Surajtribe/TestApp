package com.test.emp.presentation.screens.splash

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.test.emp.presentation.viewmodel.RandomTextViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RandomTextScreen(viewModel: RandomTextViewModel = hiltViewModel()) {

    val generatedStrings by viewModel.generatedStrings.collectAsState()
    val error by viewModel.error.collectAsState()

    var lengthInput by remember { mutableStateOf("") }
    var inputError by remember { mutableStateOf<String?>(null) }

    val isLoading by viewModel.isLoading.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 50.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        OutlinedTextField(
            value = lengthInput,
            onValueChange = { text ->
                if (text.all { it.isDigit() } || text.isEmpty()) {
                    lengthInput = text
                    inputError = null
                } else {
                    inputError = "Only numbers allowed"
                }
            },
            placeholder = { Text("Enter a numbers between 1 to 1000 only") },
            label = { Text("Length of string") },
            isError = inputError != null,
            supportingText = {
                inputError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                val len = lengthInput.toIntOrNull()
                if (len != null && len in 1..1000) {
                    viewModel.generateString(len)
                    lengthInput = ""
                } else {
                    inputError = "Please enter a number between 1–1000"
                }
            },
            enabled = !isLoading && inputError == null && (lengthInput.toIntOrNull() in 1..1000)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Generate Random String")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { viewModel.deleteAll() },
            enabled = generatedStrings.isNotEmpty()
        ) {
            Text("Delete All")
        }
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            items(generatedStrings) { item ->
                Card(
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                    ) {
                        Log.e("xxx", "RandomTextScreen: "+item.value )
                        Text(
                            "Value: ${item.value}",
                            textAlign = TextAlign.Start
                        )
                        Text(
                            "Length: ${item.length}",
                            textAlign = TextAlign.Start
                        )
                        Text(
                            "Created: ${item.created}",
                            textAlign = TextAlign.Start
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(onClick = { viewModel.deleteOne(item) }) {
                            Text("Delete")
                        }
                    }
                }
            }
        }

        error?.let {
            Snackbar(
                action = {
                    TextButton(onClick = { viewModel.clearError() }) { Text("Dismiss") }
                },
                modifier = Modifier.padding(8.dp)
            ) {
                Text(it)
            }
        }
    }
}
