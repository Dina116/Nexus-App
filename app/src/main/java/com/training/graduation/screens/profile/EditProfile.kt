package com.training.graduation.screens.profile

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import com.training.graduation.R
import com.training.graduation.screens.Authentication.AuthViewModel
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import uploadImageToCloudinary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Profile(navController: NavController) {
    val authViewModel: AuthViewModel = viewModel()
    val currentUser by authViewModel.currentUser.observeAsState()
    val context = LocalContext.current

    var showSuccessDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        authViewModel.fetchCurrentUser()
    }

    val nameState = rememberSaveable { mutableStateOf("") }
    val emailState = rememberSaveable { mutableStateOf("") }
    val passwordState = rememberSaveable { mutableStateOf("") }
    var isUploading by remember { mutableStateOf(false) }
    val imageUri = rememberSaveable { mutableStateOf(currentUser?.imageUrl ?: "") }


    LaunchedEffect(currentUser) {
        nameState.value = currentUser?.name ?: ""
        emailState.value = currentUser?.email ?: ""
        passwordState.value = currentUser?.password ?: ""
        imageUri.value = currentUser?.imageUrl ?: ""

    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imageUri.value = it.toString()
        }
    }
    val painter = rememberAsyncImagePainter(imageUri.value.ifEmpty { R.drawable.default_avatar_profile })

    Box(modifier = Modifier.fillMaxSize()) {
        BackgroundContent(navController)

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(top = 150.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
            shape = RoundedCornerShape(topStart = 50.dp, topEnd = 50.dp),
            colors = CardDefaults.cardColors(Color.White)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(60.dp))
                EditPhoto(modifier = Modifier.padding(bottom = 20.dp), launcher)

                OutlinedTextField(
                    value = nameState.value,
                    onValueChange = { nameState.value = it },
                    shape = RoundedCornerShape(25.dp),
                    label = { Text(stringResource(R.string.username)) },
                    modifier = Modifier.fillMaxWidth().padding(25.dp)
                )

                OutlinedTextField(
                    value = emailState.value,
                    onValueChange = { emailState.value = it },
                    shape = RoundedCornerShape(25.dp),
                    label = { Text(stringResource(R.string.Email)) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 25.dp, vertical = 10.dp)
                )

                OutlinedTextField(
                    value = passwordState.value,
                    onValueChange = { passwordState.value = it },
                    shape = RoundedCornerShape(25.dp),
                    label = { Text(stringResource(R.string.Password)) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 25.dp, vertical = 10.dp)
                )

                Button(
                    onClick = {
                        isUploading = true
                        if (imageUri.value.startsWith("content://")) {
                            uploadImageToCloudinary(context, Uri.parse(imageUri.value),
                                onSuccess = { imageUrl ->
                                    val updatedUser = currentUser?.copy(
                                        name = nameState.value,
                                        email = emailState.value,
                                        password = passwordState.value,
                                        imageUrl = imageUrl
                                    )
                                    updatedUser?.let {
                                        authViewModel.updateUser(it)
                                        showSuccessDialog = true
                                        isUploading = false
                                    }
                                },
                                onFailure = {
                                    Toast.makeText(context, "Image upload failed", Toast.LENGTH_SHORT).show()
                                    isUploading = false
                                }
                            )
                        } else {
                            val updatedUser = currentUser?.copy(
                                name = nameState.value,
                                email = emailState.value,
                                password = passwordState.value,
                                imageUrl = imageUri.value
                            )
                            updatedUser?.let {
                                authViewModel.updateUser(it)
                                showSuccessDialog = true
                                isUploading = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 80.dp, end = 80.dp, top = 50.dp)
                        .height(40.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF000000), Color(0xFF3533CD)),
                                start = Offset(0f, 0f),
                                end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                            ),
                            shape = RoundedCornerShape(30.dp)
                        ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Edit",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                }
                if (isUploading) {
                    CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
                }

            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 100.dp)
                .zIndex(1f)
        ) {
            ProfileImage(painter)
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = { Text("Profile Updated") },
            text = { Text("Your profile has been updated successfully.") },
            confirmButton = {
                Button(onClick = { showSuccessDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}


@Composable
fun ProfileImage(painter: AsyncImagePainter) {

    Image(
        painter = painter,
        contentScale = ContentScale.Crop,
        contentDescription = "Circular Image",
        modifier = Modifier
            .size(100.dp)
            .clip(CircleShape)
            .background(Color.LightGray)
    )
}

@Composable
fun EditPhoto(modifier: Modifier,launcher: androidx.activity.compose.ManagedActivityResultLauncher<String, Uri?>) {
    Box(
        modifier = modifier
            .border(
                width = 1.dp,
                color = Color.LightGray,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(7.dp)
            .clickable {
                launcher.launch("image/*")
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Photo(id= R.drawable.ic_addphoto)

            Spacer(modifier = Modifier.width(5.dp))

            Text(
                stringResource(R.string.Edit_photo),
                fontSize = 16.sp,
                color = Color.Black
            )
        }
    }
}

@Composable
fun BackgroundContent(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colorResource(R.color.basic_color))

    ) {
        Column( modifier = Modifier.padding(top = 30.dp, start = 10.dp),
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        navController.navigateUp()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier.padding(end = 8.dp)
                    )

                }
                Text(
                    stringResource(R.string.edit_your_profile),
                    style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp)
                )

            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun Edit(){

    val navController = rememberNavController()
    Profile(navController = navController)
}
