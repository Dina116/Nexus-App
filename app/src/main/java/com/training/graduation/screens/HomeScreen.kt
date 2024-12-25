package com.training.graduation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.training.graduation.R
import com.training.graduation.navigation.BottomNavigationBar


@Composable
fun HomeScreen(modifier: Modifier,navController:NavController, innerpadding: PaddingValues) {
    Box(modifier = modifier.padding(innerpadding).fillMaxSize(), contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,

                ) {
                Spacer(modifier = Modifier.height(100.dp))
                SearchBar(
                    onSearch = { query ->
                        println("Search query: $query")
                    },
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    painter = painterResource(id = R.drawable.ic_menu),
                    contentDescription = "Menu Icon",
                    modifier = Modifier
                        .size(30.dp)
                        .wrapContentSize(Alignment.TopEnd)
                        .clickable { },
                    tint = Color.Black
                )
            }
            Card(onClick = {},modifier= Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp)
                .height(120.dp),
                shape = RoundedCornerShape(30.dp),
                colors = CardDefaults.cardColors(Color.Blue.copy(alpha = 0.15f))
            ){
                Box( modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_video),
                            contentDescription = "null",
                            modifier = Modifier.size(20.dp),
                            tint = Color.Black
                        )
                        Text(stringResource(R.string.start_meeting), fontWeight = FontWeight.Bold)
                    }

                }

            }
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp)
            ) {
                Card(onClick = {},
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 10.dp)
                        .height(120.dp),
                    shape = RoundedCornerShape(
                        topStart = 30.dp,
                        topEnd = 30.dp,
                        bottomEnd = 30.dp,
                        bottomStart = 30.dp
                    ),
                    colors = CardDefaults.cardColors(Color(0xF4C2C2).copy(alpha = 1f))

                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column (horizontalAlignment = Alignment.CenterHorizontally){
                            Icon(
                                painter = painterResource(id = R.drawable.icon_schedule),
                                contentDescription = "null",
                                modifier = Modifier.size(20.dp),
                                tint = Color.Black
                            )
                            Text(stringResource(R.string.schedule), fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Card(onClick = {},
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 10.dp)
                        .height(120.dp),
                    shape = RoundedCornerShape(30.dp),
                    colors = CardDefaults.cardColors(Color.Gray.copy(alpha = 0.1f))
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val image = painterResource(id =R.drawable.ai_assest)

                            Image(
                                painter = image,
                                contentDescription = "ai asset",
                                modifier = Modifier.size(60.dp)

                            )
                            //Text("AI Asset", fontWeight = FontWeight.Bold)
                            Text(stringResource(R.string.comming_soon), fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
            }

            Spacer(modifier = Modifier.height(70.dp))
            BottomNavigationBar(navController = NavController(LocalContext.current))


        }
    }



@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(modifier = Modifier, navController = NavController(LocalContext.current), innerpadding = PaddingValues())

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    placeholder: String = stringResource(R.string.search),
    onSearch: (String) -> Unit
) {
    var searchText by remember { mutableStateOf("") }

    OutlinedTextField(
        value = searchText,
        onValueChange = {
            searchText = it
            onSearch(it)
        },
        placeholder = { Text(text = placeholder) },
        leadingIcon = {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_search),
                contentDescription = "Search Icon",
                tint = Color.Gray
            )
        },
        colors = TextFieldDefaults.outlinedTextFieldColors(
            containerColor = Color.White,
            focusedBorderColor = Color(0xFF3533CD),
            unfocusedBorderColor = Color.Gray,
            cursorColor = Color.Black
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(56.dp),
        shape = RoundedCornerShape(25.dp),
    )
}