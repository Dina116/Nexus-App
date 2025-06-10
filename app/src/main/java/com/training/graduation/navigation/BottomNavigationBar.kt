package com.training.graduation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

import com.training.graduation.R



@Preview(showBackground = true, showSystemUi = true)
@Composable
fun BottomNavigationBarPreview() {
    BottomNavigationBar(navController = NavController(LocalContext.current))
}
@Composable
fun BottomNavigationBar(navController: NavController) {
    val currentRoute = navController.currentDestination?.route

    Surface(
        shape = RoundedCornerShape(25.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 20.dp)
            .height(100.dp), // قلل الارتفاع لو كان كبير
        color = Color.White,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 10.dp), // padding داخلي لضبط المحاذاة
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color.Black, Color(0xFF3533CD)),
                            start = Offset(0f, 0f),
                            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                        ),
                        shape = RoundedCornerShape(25.dp)
                    )
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomNavItem(
                    iconRes = R.drawable.home_icon,
                    label = stringResource(R.string.home),
                    isSelected = currentRoute == "homescreen"
                ) {
                    navController.navigate("homescreen") {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }

                BottomNavItem(
                    iconRes = R.drawable.group_icon,
                    label = stringResource(R.string.group),
                    isSelected = currentRoute == "group"
                ) {
                    navController.navigate("group") {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }

                BottomNavItem(
                    iconRes = R.drawable.profile_icon,
                    label = stringResource(R.string.profile),
                    isSelected = currentRoute == "userprofile"
                ) {
                    navController.navigate("userprofile") {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavItem(
    iconRes: Int,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp) // حجم الخلفية
                .background(
                    color = if (isSelected) Color.White else Color.Transparent,
                    shape = RoundedCornerShape(10.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                modifier = Modifier.size(24.dp),
                tint = if (isSelected) Color(0xFF3533CD) else Color.White
            )
        }

        if (isSelected) {
            Text(text = label, color = Color.White)
        }
    }
}



/*
//                NavigationBarItem(
//                    icon = {
//                        Icon(
//                            painter = painterResource(id = R.drawable.message_icon),
//                            contentDescription = "Chat",
//                            tint = if (currentRoute == "chat") Color.White else Color.White,
//                            modifier = Modifier
//                                .wrapContentSize(Alignment.Center)
//                                .size(25.dp)
//                        )
//                    },
//                    label = {
//                        if (currentRoute == "chat") {
//                            Text(
//                                text = stringResource(R.string.chat),
//                                color = Color.White
//                            )
//                        }
//                    },
//                    selected = currentRoute == "chat",
//                    onClick = {
//                        navController.navigate("chat") {
//                            popUpTo(0) { inclusive = true }
//                        }
//                    }
//                )

 */