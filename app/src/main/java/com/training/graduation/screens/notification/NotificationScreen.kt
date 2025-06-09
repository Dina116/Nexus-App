package com.training.graduation.screens.notification

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.training.graduation.R

@Composable
fun NotificationScreen(navController:NavController , innerpadding: PaddingValues) {


    Column(
        modifier = Modifier.padding(top = 30.dp, start = 10.dp),
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
                stringResource(R.string.notifications),
                style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp)
            )

        }
    }
    No_Notification()



}


@Composable
fun No_Notification(){
    Column (
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){



        val image = painterResource(id = R.drawable.notification_)

        Image(
            painter = image,
            contentDescription = "Circular Image",
            modifier = Modifier.size(300.dp)

        )
        Text(stringResource(R.string.you_don_t_have_any_notifications)
            , style = TextStyle(fontWeight=FontWeight.Bold, fontSize = 20.sp)
        )
    }
}

//@Preview(showBackground = true, showSystemUi = true)
//@Composable
//fun NotificationScreenPreview() {
//    val navController = rememberNavController()
//
//    NotificationScreen(navController = navController)
//}

