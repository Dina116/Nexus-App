package com.training.graduation.screens.group

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest

import com.training.graduation.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailsScreen(
    viewModel: GroupDetailsViewModel,
    groupName: String,
    onNavigateBack: () -> Unit,
    onNavigateToAddMember: (groupId: String) -> Unit
    , innerpadding: PaddingValues
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showRemoveConfirmDialog by remember { mutableStateOf<User?>(null) }
    var showLeaveConfirmDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    LaunchedEffect(uiState.navigationEvent) {
        when (uiState.navigationEvent) {
            is NavigationEvent.NavigateBack -> {
                onNavigateBack()
                viewModel.consumeNavigationEvent()
            }
            null -> { }
        }
    }
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.error) {
        if (uiState.error != null) {
            snackbarHostState.showSnackbar(
                message = uiState.error ?: "An unknown error occurred",
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(groupName, color = Color(0xFF3533CD), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            if (uiState.isCurrentUserCreator) {
                FloatingActionButton(onClick = { onNavigateToAddMember(viewModel.groupId) }, modifier = Modifier.padding(bottom = 100.dp)) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Member")
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.group != null) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = "Members (${uiState.members.size})",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(uiState.members, key = { it.uid }) { member ->
                            MemberItem(
                                user = member,
                                isRemovable = uiState.isCurrentUserCreator && member.uid != uiState.currentUserId,
                                onRemoveClick = { showRemoveConfirmDialog = member }
                            )
                        }
                    }
                    Column(modifier = Modifier.padding(16.dp)) {
                        Button(
                            onClick = { showLeaveConfirmDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor =Color.LightGray.copy(0.7f))
                        ) {
                            Icon(Icons.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Text("Leave Group", color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        if (uiState.isCurrentUserCreator) {
                            Button(
                                onClick = { showDeleteConfirmDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Blue.copy(1f))
                            ) {
                                Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                                Text("Delete Group Permanently", color = MaterialTheme.colorScheme.onError)
                            }
                        }
                    }
                }
            }
            if (uiState.actionInProgress) {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
    if (showRemoveConfirmDialog != null) {
        val memberToRemove = showRemoveConfirmDialog!!
        AlertDialog(
            onDismissRequest = { showRemoveConfirmDialog = null },
            title = { Text("Remove Member") },
            text = { Text("Are you sure you want to remove ${memberToRemove.displayName}?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.removeMember(memberToRemove.uid)
                    showRemoveConfirmDialog = null
                }) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveConfirmDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showLeaveConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveConfirmDialog = false },
            title = { Text("Leave Group") },
            text = { Text("Are you sure you want to leave this group?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.leaveGroup()
                    showLeaveConfirmDialog = false
                }) {
                    Text("Leave")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Group") },
            text = { Text("Are you sure you want to permanently delete this group and all its messages? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteGroup()
                    showDeleteConfirmDialog = false
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun MemberItem(
    user: User,
    isRemovable: Boolean,
    onRemoveClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = rememberAsyncImagePainter(
                ImageRequest.Builder(LocalContext.current)
                    .data(data = user.photoUrl ?: R.drawable.default_avatar_profile)
                    .apply(block = fun ImageRequest.Builder.() {
                        crossfade(true)
                        placeholder(R.drawable.default_avatar_profile)
                        error(R.drawable.default_avatar_profile)
                    }).build()
            ),
            contentDescription = "Profile picture",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = user.displayName.ifEmpty { "User" },
                fontWeight = FontWeight.Bold
            )
            Text(
                text = user.email,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
        if (isRemovable) {
            IconButton(onClick = onRemoveClick) {
                Icon(
                    Icons.Filled.PersonRemove,
                    contentDescription = "Remove Member",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
