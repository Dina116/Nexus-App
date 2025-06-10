package com.training.graduation.screens.profile


import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.delay
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.training.graduation.R
import com.training.graduation.db.model.User
import com.training.graduation.navigation.BottomNavigationBar
import com.training.graduation.screens.Authentication.AuthState
import com.training.graduation.screens.Authentication.AuthViewModel
import com.training.graduation.screens.sharedprefrence.PreferenceManager
import com.training.graduation.screens.sharedprefrence.UpdateLocale
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.delay
import java.util.Locale

@Composable
fun UserProfileScreen(navController: NavController,preferenceManager: PreferenceManager){

    val layoutDirection = LocalLayoutDirection.current
    val authViewModel: AuthViewModel = viewModel()

    val scrollState = rememberScrollState()
    val currentUser by authViewModel.currentUser.observeAsState()

    LaunchedEffect(Unit) {
        authViewModel.fetchCurrentUser()
        authViewModel.resetAuthState()

    }


    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(scrollState)

    )
    {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, top = 50.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfileImage1(imageUrl = currentUser?.imageUrl)

            Text(
                text = currentUser?.name ?: "Loading...",
                modifier = Modifier.padding(25.dp),
                fontSize = 17.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
        }

        EditProfile(Modifier.padding( start = 20.dp),navController)

        AddAccount(Modifier.padding(top = 20.dp, start = 20.dp),layoutDirection)

        ContactInfo(Modifier.padding(top = 30.dp, start = 20.dp))

        Info(Modifier.padding(start =40.dp,top = 20.dp),currentUser)
        Settings(Modifier.padding(start =20.dp,top = 20.dp))

        Language(Modifier.padding(top = 30.dp, start= 20.dp),layoutDirection,preferenceManager)

        Notification(Modifier.padding(start = 20.dp,top= 30.dp))

        Privacy(Modifier.padding(top = 30.dp, start = 20.dp))

        SignOut(
            Modifier.padding(start = 20.dp, top = 30.dp), layoutDirection,
            authViewModel = authViewModel,
            navController = navController
        )

    }





}

@Composable
fun ProfileImage1(imageUrl: String?) {
    val painter = rememberAsyncImagePainter(model = imageUrl ?: R.drawable.default_avatar_profile)

    Image(
        painter = painter,
        contentScale = ContentScale.Crop,
        contentDescription = "Profile Image",
        modifier = Modifier
            .size(70.dp)
            .clip(CircleShape)
            .background(Color.LightGray)
    )
}

@Composable
fun EditProfile(modifier: Modifier = Modifier, navController: NavController) {
    Box(
        modifier = modifier
            .border(
                width = 1.dp,
                color = Color.LightGray,
                shape = RoundedCornerShape(10.dp)

            )
            .clip(RoundedCornerShape(10.dp))
            .background(color = colorResource(R.color.basic_color))
            .padding(7.dp)
            .clickable { navController.navigate("editProfile") }

    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                stringResource(id = R.string.Edit_Profile),
                fontSize = 16.sp,
                color = Color.White
            )
        }
    }
}

@Composable
fun ContactInfo(modifier: Modifier = Modifier){

    Text(
        stringResource(id = R.string.ContactInformation),
        fontSize = 20.sp,
        color = Color.Black,
        fontWeight = FontWeight.Bold,
        modifier = modifier
    )

}

@Composable
fun Info(modifier: Modifier = Modifier, user: User?) {

    val context= LocalContext.current
    Row(modifier = modifier) {
        Icon(
            imageVector = Icons.Default.Email,
            contentDescription = "Email Icon",
            tint = Color.Gray,
            modifier = Modifier.size(30.dp)
        )
        Column {
            Text(
                stringResource(id = R.string.Email),
                fontSize = 20.sp,
                modifier = Modifier.padding(horizontal = 10.dp)
            )
            Text(
                text = user?.email ?: "Loading...",
                fontSize = 17.sp,
                color = Color.Blue,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .clickable {
                        user?.email?.let { email ->
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:$email")
                            }
                            context.startActivity(intent)
                        }
                    }
            )
        }
    }
}

@Composable
fun Settings(modifier: Modifier){
    Row(
        modifier=modifier
    ) {

        Photo(id=R.drawable.ic_setting)

        Text(
            stringResource(id = R.string.Settings),
            fontSize = 20.sp,
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(horizontal = 10.dp)
        )
    }
}


@Composable
fun Language(modifier: Modifier, localDirection: LayoutDirection,preferenceManager: PreferenceManager){

    val context = LocalContext.current

    Row(modifier=modifier) {

        Photo(R.drawable.language)

        var showDialog by remember { mutableStateOf(false) }
        var selectedTheme by remember { mutableStateOf("Light") }
//        var selectedLanguage by remember { mutableStateOf(Locale.getDefault().language) }
        var selectedLanguage by remember { mutableStateOf(preferenceManager.getLanguage()) }


        Column(
            modifier = Modifier
            ,horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                stringResource(R.string.language),
                fontSize = 20.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .clickable { showDialog = true }
            )

            if (showDialog) {

                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text(stringResource(id = R.string.Settings)) },
                    text = {
                        Column {
                            Text(text = stringResource(id = R.string.Themes), fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = selectedTheme == stringResource(id = R.string.Light),
                                    onClick = { selectedTheme = "Light"
                                    },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = colorResource(R.color.basic_color),
                                    )
                                )
                                Text(stringResource(id = R.string.Light), modifier = Modifier.padding(start = 8.dp))
                                Spacer(modifier = Modifier.width(16.dp))
                                RadioButton(
                                    selected = selectedTheme == stringResource(id = R.string.Dark),
                                    onClick = { selectedTheme = "Dark"
                                    },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = colorResource(R.color.basic_color),
                                    )
                                )
                                Text(stringResource(id = R.string.Dark), modifier = Modifier.padding(start = 8.dp))
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(stringResource(id = R.string.Languages), fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = selectedLanguage =="en",
                                    onClick = { selectedLanguage ="en"
                                        UpdateLocale(context,"en")
                                        preferenceManager.saveLanguage("en")


                                    },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = colorResource(R.color.basic_color),
                                    )
                                )
                                Text(stringResource(id = R.string.English), modifier = Modifier.padding(start = 8.dp))
                                Spacer(modifier = Modifier.width(16.dp))
                                RadioButton(
                                    selected = selectedLanguage =="ar",
                                    onClick = { selectedLanguage ="ar"
                                        UpdateLocale(context,"ar")
                                        preferenceManager.saveLanguage("ar")

                                    },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = colorResource(R.color.basic_color),
                                    )
                                )
                                Text(stringResource(id = R.string.Arabic), modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                    },

                    confirmButton = {


                        Button(
                            onClick = {
                                showDialog = false

                            },
                            modifier = Modifier.padding(end = if (localDirection == LayoutDirection.Rtl) 8.dp else 0.dp) ,
                            colors = ButtonDefaults.buttonColors(
                                colorResource( R.color.basic_color)
                            )
                        ) {
                            Text(stringResource(id = R.string.Save))
                        }
                    },
                    dismissButton = {
                        Button(onClick = { showDialog = false },
                            modifier = Modifier.padding(end = if (localDirection == LayoutDirection.Rtl) 8.dp else 0.dp) ,

                            colors = ButtonDefaults.buttonColors(
                                colorResource( R.color.basic_color)
                            )) {
                            Text(stringResource(id = R.string.Cancel))
                        }
                    }
                )
            }
        }
    }
}



@Composable
fun Notification(modifier: Modifier){
    Row(modifier=modifier) {
        Photo(R.drawable.ic_notification)

        Text(
            stringResource(id = R.string.Notification),
            fontSize = 20.sp,
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            modifier= Modifier.padding(horizontal = 10.dp)
        )

    }
}

@Composable
fun Privacy(modifier: Modifier){
    Row(modifier=modifier) {
        Photo(R.drawable.ic_privacy)

        Text(
            stringResource(id = R.string.Privacy),
            fontSize = 20.sp,
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            modifier= Modifier.padding(horizontal = 10.dp)
        )

    }
}

@Composable
fun AddAccount(modifier: Modifier, localDirection: LayoutDirection){
    Row(modifier=modifier, verticalAlignment = Alignment.CenterVertically) {
        val image = painterResource(id = R.drawable.addaccount)

        Image(
            painter = image,
            contentDescription = "Circular Image",
            modifier = Modifier
                .size(35.dp)
                .clip(CircleShape)
                .background(Color.White)
        )

        var showDialog by remember { mutableStateOf(false) }
        var email by remember { mutableStateOf("") }  // لتخزين قيمة البريد الإلكتروني

        Column(
            modifier = Modifier
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                stringResource(id = R.string.Add_another_account),
                fontSize = 20.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable { showDialog = true }
            )

            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = {
                        Text(stringResource(id = R.string.Add_another_account))
                    },
                    text = {
                        Column {
                            Text(stringResource(id = R.string.Enter_Email))
                            TextField(
                                value = email,
                                onValueChange = { email = it },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                        }
                    },
                    confirmButton = {
                        Button (onClick = {
                            // قم بإضافة الحساب هنا
                            showDialog = false
                            // منطق حفظ الحساب يمكن إضافته هنا
                        },
                            modifier = Modifier.padding(end = if (localDirection == LayoutDirection.Rtl) 8.dp else 0.dp) ,
                            colors = ButtonDefaults.buttonColors(
                                colorResource( R.color.basic_color)
                            )) {
                            Text(stringResource(id = R.string.Add))
                        }
                    },
                    dismissButton = {
                        Button(onClick = { showDialog = false },
                            modifier = Modifier.padding(end = if (localDirection == LayoutDirection.Rtl) 8.dp else 0.dp) ,
                            colors = ButtonDefaults.buttonColors(
                                colorResource( R.color.basic_color)
                            )) {
                            Text(stringResource(id = R.string.Cancel))
                        }
                    }
                )
            }
        }

    }


}

@Composable
fun SignOut(
    modifier: Modifier,
    localDirection: LayoutDirection,
    authViewModel: AuthViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    Row(modifier = modifier) {
        Photo(R.drawable.ic_logout, Color.Red)

        Column(
            modifier = Modifier.padding(horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                stringResource(id = R.string.SignOut),
                fontSize = 20.sp,
                color = Color.Red,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { showDialog = true }
            )

            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = {
                        Text(stringResource(id = R.string.SignOut))
                    },
                    text = {
                        Text(stringResource(id = R.string.Are_you_sure_you_want_to_sign_out))
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showDialog = false
                                isLoading = true
                                authViewModel.signout()
                                Toast.makeText(context, "SignOut!!", Toast.LENGTH_SHORT).show()

                                coroutineScope.launch {
                                    delay(100)
                                    isLoading = false
                                    authViewModel.resetAuthState()
                                    navController.navigate("loginscreen") {
                                        popUpTo("homescreen") { inclusive = true }
                                    }
                                }

                            },
                            modifier = Modifier.padding(end = if (localDirection == LayoutDirection.Rtl) 8.dp else 0.dp),
                            colors = ButtonDefaults.buttonColors(colorResource(R.color.basic_color))
                        ) {
                            Text(stringResource(id = R.string.Yes))
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = { showDialog = false },
                            modifier = Modifier.padding(end = if (localDirection == LayoutDirection.Rtl) 8.dp else 0.dp),
                            colors = ButtonDefaults.buttonColors(colorResource(R.color.basic_color))
                        ) {
                            Text(stringResource(id = R.string.No))
                        }
                    }
                )
            }

            if (isLoading) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator(
                    color = Color(0xFF3533CD),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
fun Photo(id:Int,tint: Color = Color.Black){
    val image = painterResource(id =id)

    Icon(
        painter = image,
        contentDescription = "Circular Image",
        modifier = Modifier.size(24.dp),
        tint = tint

    )
}



@Preview(showBackground = true, showSystemUi = true)
@Composable
fun UserProfileScreenPreview() {
    UserProfileScreen(navController = NavController(LocalContext.current), preferenceManager=PreferenceManager(LocalContext.current) )


}