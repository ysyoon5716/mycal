package com.example.mycal.presentation.screens.subscription.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSubscriptionDialog(
    state: com.example.mycal.presentation.screens.subscription.AddSourceDialogState,
    onUrlChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onColorChange: (Color) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Add Calendar Subscription",
                    style = MaterialTheme.typography.headlineSmall
                )

                OutlinedTextField(
                    value = state.url,
                    onValueChange = onUrlChange,
                    label = { Text("ICS URL") },
                    placeholder = { Text("https://example.com/calendar.ics") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    minLines = 2,
                    isError = state.urlError != null,
                    supportingText = {
                        state.urlError?.let { error ->
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )

                OutlinedTextField(
                    value = state.name,
                    onValueChange = onNameChange,
                    label = { Text("Calendar Name") },
                    placeholder = { Text("My Calendar") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                ColorPicker(
                    selectedColor = state.color,
                    onColorSelected = onColorChange
                )

                state.error?.let { error ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        enabled = !isLoading
                    ) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onConfirm,
                        enabled = !isLoading && state.url.isNotBlank() && state.urlError == null
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Add")
                        }
                    }
                }
            }
        }
    }
}