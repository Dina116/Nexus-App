package com.training.graduation.screens.group

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupScreen(
    viewModel: CreateGroupViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onGroupCreated: (String,String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.creationSuccessEvent.collectLatest { (groupId ,groupName)->
            onGroupCreated(groupId,groupName)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create New Group", fontWeight = FontWeight.Bold, color = Color(0xFF3533CD)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF3533CD))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = uiState.groupName,
                onValueChange = { viewModel.updateGroupName(it) },
                label = { Text("Group Name") },
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.error != null,
                singleLine = true,

            )

            if (uiState.error != null) {
                Text(text = uiState.error ?: "", color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = {
                    viewModel.createGroup()
                },
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF000000), Color(0xFF3533CD)),
                            start = Offset(
                                0f,
                                0f
                            ),
                            end = Offset(
                                Float.POSITIVE_INFINITY,
                                Float.POSITIVE_INFINITY
                            )
                        )
                    ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.White

                ),
                shape = RoundedCornerShape(25.dp),

            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text("Create Group")
                }
            }
        }
    }
}
