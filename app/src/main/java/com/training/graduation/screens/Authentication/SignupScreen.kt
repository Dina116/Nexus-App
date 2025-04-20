

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import coil.compose.rememberAsyncImagePainter
import com.training.graduation.R
import com.training.graduation.screens.Authentication.AuthState
import com.training.graduation.screens.Authentication.AuthViewModel


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SignupScreenPreview(){
    SignupScreen(modifier = Modifier, navController= NavController(LocalContext.current), authViewModel = AuthViewModel(), innerpadding = PaddingValues())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(modifier: Modifier, navController: NavController, authViewModel: AuthViewModel, innerpadding: PaddingValues){

    var selectedRole by remember { mutableStateOf("User") }
    var userName by remember { mutableStateOf("") }
    var userNameError by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf(false) }
    var confirmPassword by remember { mutableStateOf("") }
    var confirmPasswordError by remember { mutableStateOf(false) }


    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Authenticated -> {
                Toast.makeText(
                    context,
                    "Register Successfully",
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
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Spacer(modifier = Modifier.height(40.dp))
        Text(
            text = stringResource(R.string.lets),
            color = Color.Black,
            fontSize = 40.sp,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(start = 20.dp)

        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = stringResource(R.string.create),
            color = Color(0xFF3533CD),
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(start = 20.dp)
        )
        Text(
            text = stringResource(R.string.your),
            color = Color(0xFF3533CD),
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(start = 20.dp)
        )
        Text(
            text = stringResource(R.string.account),
            color = Color(0xFF3533CD),
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(start = 20.dp)
        )
        Spacer(modifier = Modifier.height(15.dp))

        Text(
            text = stringResource(R.string.select_your_role),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(start = 30.dp)
        )
        Spacer(modifier = Modifier.height(5.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 30.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selectedRole == stringResource(R.string.user),
                onClick = { "User".also { selectedRole = it } },
                colors = RadioButtonDefaults.colors(
                    selectedColor = Color(0xFF3533CD),
                    unselectedColor = Color.Gray
                )
            )
            Text(text = stringResource(R.string.user), fontSize = 18.sp)
            Spacer(modifier = Modifier.width(5.dp))

            RadioButton(
                selected = selectedRole == stringResource(R.string.instructor),
                onClick = { "Foundation".also { selectedRole = it } },
                colors = RadioButtonDefaults.colors(
                    selectedColor = Color(0xFF3533CD),
                    unselectedColor = Color.Gray
                )
            )
            Text(text = stringResource(R.string.instructor), fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(5.dp))

        OutlinedTextField(
            label = { Text(stringResource(R.string.username)) },
            value = userName,
            onValueChange = {
                userName = it
                userNameError = it.isBlank()
            },
            isError = userNameError,
            supportingText = {
                if (userNameError) {
                    Text(stringResource(R.string.please_enter_username), color = Color.Red)
                }

            },
            leadingIcon = {
                Icon(
                    ImageVector.vectorResource(R.drawable.ic_user),
                    contentDescription = null
                )
            },
            shape = RoundedCornerShape(25.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 30.dp, end = 30.dp)

        )
        Spacer(modifier = Modifier.height(5.dp))

        OutlinedTextField(
            label = { Text(stringResource(R.string.enter_your_email)) },
            value = email,
            onValueChange = {
                email = it
                emailError = it.isBlank()
            },
            isError = emailError,
            supportingText = {
                if (emailError) {
                    Text(stringResource(R.string.please_enter_email), color = Color.Red)
                }

            },
            leadingIcon = {
                Icon(
                    ImageVector.vectorResource(R.drawable.ic_email),
                    contentDescription = null
                )
            },
            shape = RoundedCornerShape(25.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 30.dp, end = 30.dp)
        )
        Spacer(modifier = Modifier.height(5.dp))

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
                if (passwordError) {
                    Text(stringResource(R.string.please_enter_password), color = Color.Red)
                }
            },
            leadingIcon = {
                Icon(
                    ImageVector.vectorResource(R.drawable.ic_lock),
                    contentDescription = null
                )
            },
            trailingIcon = {
                Icon(
                    ImageVector.vectorResource(R.drawable.ic_eye),
                    contentDescription = null
                )
            },
            shape = RoundedCornerShape(25.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 30.dp, end = 30.dp)
        )

        Spacer(modifier = Modifier.height(5.dp))

        OutlinedTextField(
            label = { Text(stringResource(R.string.confirm_your_password)) },
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                confirmPasswordError = it.isBlank()
            },
            isError = confirmPasswordError,
            supportingText = {
                if (confirmPasswordError) {
                    Text(stringResource(R.string.please_enter_confirm_password), color = Color.Red)
                }
            },
            leadingIcon = {
                Icon(
                    ImageVector.vectorResource(R.drawable.ic_lock),
                    contentDescription = null
                )
            },
            trailingIcon = {
                Icon(
                    ImageVector.vectorResource(R.drawable.ic_eye),
                    contentDescription = null
                )
            },
            shape = RoundedCornerShape(25.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 30.dp, end = 30.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                var isValid = true

                if (userName.isBlank()) {
                    userNameError = true
                    isValid = false
                } else {
                    userNameError = false
                }

                if (email.isBlank()) {
                    emailError = true
                    isValid = false
                } else {
                    emailError = false
                }

                if (password.isBlank()) {
                    passwordError = true
                    isValid = false
                } else {
                    passwordError = false
                }

                if (confirmPassword.isBlank()) {
                    confirmPasswordError = true
                    isValid = false
                } else {
                    confirmPasswordError = false
                }

                if (password != confirmPassword) {
                    confirmPasswordError = true
                    isValid = false
                    Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                }

                val defaultImageUrl="https://res.cloudinary.com/daclkwzzc/image/upload/v1744581440/default/default-avatar-profile.jpg"
                if (isValid) {
                     authViewModel.signup(
                            email = email,
                            password = password,
                            userName = userName,
                            selectedRole = selectedRole,
                            defaultImageUrl = defaultImageUrl
                        )
                    }
            },
            enabled = authState.value != AuthState.Loading,

            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 30.dp, end = 30.dp)
                .height(50.dp)
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
                    ),
                    shape = RoundedCornerShape(30.dp)
                ),

            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color.White

            ),

            ) {
            Text(
                text = stringResource(R.string.sign_up),
                color = Color.White,

                fontSize = 15.sp
            )

        }
        if (authState.value == AuthState.Loading) {
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator(
                color = Color(0xFF3533CD),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }


    }
}




