package com.training.graduation.screens.group

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.training.graduation.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMemberScreen(
    viewModel: AddMemberViewModel,
    onNavigateBack: () -> Unit
    ,innerpadding: PaddingValues
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(uiState.addSuccess) {
        if (uiState.addSuccess) {
            onNavigateBack()
            viewModel.consumeAddSuccess()
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
                title = { Text("Add Members", color = Color(0xFF3533CD), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            if (uiState.selectedUserIds.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    text = { Text("Add Selected (${uiState.selectedUserIds.size})") },
                    icon = { Icon(Icons.Filled.Done, contentDescription = null) },
                    onClick = { viewModel.addSelectedMembers() },
                    expanded = true
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search users by email...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(25.dp)
            )
            Box(modifier = Modifier.weight(1f)) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (uiState.searchResults.isEmpty() && uiState.searchQuery.length > 1) {
                    Text("No users found", modifier = Modifier.align(Alignment.Center).padding(16.dp))
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(uiState.searchResults, key = { it.uid }) { user ->
                            val isExistingMember = uiState.existingMemberIds.contains(user.uid)
                            val isSelected = uiState.selectedUserIds.contains(user.uid)
                            UserSearchResultItem(
                                user = user,
                                isSelected = isSelected,
                                isEnabled = !isExistingMember,
                                onToggleSelection = { viewModel.toggleUserSelection(user.uid) }
                            )
                        }
                    }
                }
                if (uiState.addInProgress) {
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
    }
}
@Composable
fun UserSearchResultItem(
    user: User,
    isSelected: Boolean,
    isEnabled: Boolean,
    onToggleSelection: () -> Unit
) {
    Log.d("AddMemberScreen", "User in item: $user")
    Log.d("AddMemberScreen", "User displayName: ${user.displayName}")
    val context = LocalContext.current
    val alpha = if (isEnabled) 1f else 0.5f
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isEnabled, onClick = onToggleSelection)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Image(
            painter = rememberAsyncImagePainter(
                ImageRequest.Builder(context)
                    .data(data = user.photoUrl?: R.drawable.default_avatar_profile)
                    .apply(block = fun ImageRequest.Builder.() {
                        crossfade(true)
                        placeholder(R.drawable.default_avatar_profile)
                        error(R.drawable.default_avatar_profile)
                    }).build()
            ),
            contentDescription = "Profile picture",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .graphicsLayer(alpha = alpha),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = user.displayName?: "",
            color = Color.Black
        )
        if (isSelected) {
            Icon(
                Icons.Filled.Check,
                contentDescription = "Selected",
                tint = MaterialTheme.colorScheme.primary
            )
        } else if (!isEnabled) {
            Text("Member", style = MaterialTheme.typography.bodySmall, color = Color.Black)
        }
    }
}
