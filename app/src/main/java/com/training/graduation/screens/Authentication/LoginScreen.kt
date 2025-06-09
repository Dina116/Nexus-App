package com.training.graduation.screens.Authentication

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.android.gms.common.util.DataUtils
import com.google.firebase.auth.FirebaseAuth
import com.training.graduation.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen( navController: NavController,authViewModel: AuthViewModel, innerPadding: PaddingValues) {
    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val authState = authViewModel.authState.observeAsState()

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Authenticated -> {
                Toast.makeText(
                    context,
                    "Login successful",
                    Toast.LENGTH_SHORT
                ).show()
                navController.navigate("homescreen")
            }

            is AuthState.Error -> Toast.makeText(
                context,
                (authState.value as AuthState.Error).message,
                Toast.LENGTH_SHORT
            ).show()

            else -> Unit
        }
    }



    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(50.dp))
        Text(
            text = stringResource(R.string.welcome),
            color = Color.Black,
            fontSize = 40.sp,
            modifier = Modifier.align(Alignment.Start).padding(start = 20.dp)
        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = stringResource(R.string.back),
            color = Color(0xFF3533CD),
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start).padding(start = 20.dp)
        )
        Spacer(modifier = Modifier.height(100.dp))
        OutlinedTextField(
            label = { Text(stringResource(R.string.enter_your_email)) },
            value = email,
            onValueChange = {
                email = it
                emailError = it.isBlank()
            },
            isError = emailError,
            supportingText = {
                if (emailError) Text(
                    stringResource(R.string.please_enter_email),
                    color = Color.Red
                )
            },
            leadingIcon = {
                Icon(
                    ImageVector.vectorResource(R.drawable.ic_email),
                    contentDescription = null
                )
            },
            shape = RoundedCornerShape(25.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 30.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))
        OutlinedTextField(
            label = { Text(stringResource(R.string.enter_your_password)) },
            value = password,
            visualTransformation = PasswordVisualTransformation(),
            onValueChange = {
                password = it
                passwordError = it.isBlank()
            },
            isError = passwordError,
            supportingText = {
                if (passwordError) Text(
                    stringResource(R.string.please_enter_password),
                    color = Color.Red
                )
            },
            leadingIcon = {
                Icon(
                    ImageVector.vectorResource(R.drawable.ic_lock),
                    contentDescription = null
                )
            },
            shape = RoundedCornerShape(25.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 30.dp)
        )
        Spacer(modifier = Modifier.height(50.dp))
        Button(
            onClick = {
                val isValid = email.isNotBlank() && password.isNotBlank()
                emailError = email.isBlank()
                passwordError = password.isBlank()
                if (isValid) {

                    authViewModel.login(email, password)

                }
            },
            enabled = authState.value != AuthState.Loading,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 30.dp)
                .height(40.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color.Black, Color(0xFF3533CD)),
                        start = Offset.Zero,
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
                text = "Login",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
        }

        if (authState.value == AuthState.Loading) {
            CircularProgressIndicator(
                color = Color(0xFF3533CD),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.forgot_password),
            fontSize = 15.sp,
            color = Color.Black,
            modifier = Modifier.align(Alignment.End).padding(end = 20.dp).clickable {
                navController.navigate("forgotpassword") { popUpTo("startscreen") }
            }
        )
        Spacer(modifier = Modifier.height(40.dp))
        Text(
            text = stringResource(R.string.don_t_have_an_account),
            fontSize = 18.sp,
            color = Color.Black,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Text(
            text = stringResource(R.string.sign_up_now),
            color = Color(0xFF3533CD),
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally).clickable {
                navController.navigate("signupscreen")
            }
        )
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen(navController = NavController(LocalContext.current), authViewModel = AuthViewModel(), innerPadding = PaddingValues())
}
