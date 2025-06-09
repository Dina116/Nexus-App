package com.training.graduation.screens.Authentication

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.training.graduation.R
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(navController: NavController, innerpadding: PaddingValues) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(100.dp))
            Text(
                text = "Forgot",
                color = Color.Black,
                fontSize = 40.sp,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(start = 20.dp)

            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = "Password?",
                color = Color(0xFF3533CD),
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(start = 20.dp)
            )
            Spacer(modifier = Modifier.height(100.dp))
            OutlinedTextField(
                label = { Text("Enter your email") },
                value = "",
                onValueChange = {},
                colors = TextFieldDefaults.colors(
                    cursorColor = Color.LightGray
                ),
//                    containerColor = Color.LightGray
//                ),

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
            Spacer(modifier = Modifier.height(80.dp))
            Button(
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 30.dp, end = 30.dp)
                    .height(55.dp)
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
                    text = stringResource(R.string.change_password),
                    color = Color.White,

                    fontSize = 15.sp
                )

            }

            Text(
                stringResource(R.string.back_to_login),
                style = TextStyle(fontSize = 20.sp),
                textDecoration = TextDecoration.Underline ,

                        modifier = Modifier
                            .padding(top = 30.dp)
                            .clickable(
                                onClick = {
                                    navController.navigate("loginscreen") {
                                        popUpTo(0)
                                    }
                                }
                            ))
        }




}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ForgotPasswordScreenPreview() {
    ForgotPasswordScreen(navController= NavController(LocalContext.current), innerpadding = PaddingValues())
}