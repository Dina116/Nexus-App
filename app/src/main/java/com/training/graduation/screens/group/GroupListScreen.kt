package com.training.graduation.screens.group

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.training.graduation.chat_token.ChatTokenRequest
import com.training.graduation.chat_token.ChatTokenResponse
import com.training.graduation.chat_token.RetrofitClient
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun GroupListScreen(navController: NavController) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val uid = currentUser?.uid
    val displayName = currentUser?.displayName ?: "Guest"

    val channels = remember { mutableStateListOf<Channel>() }

    LaunchedEffect(uid) {
        if (uid != null) {
            val request = ChatTokenRequest(uid)

            RetrofitClient.instance.getChatToken(request).enqueue(object :
                Callback<ChatTokenResponse> {
                override fun onResponse(
                    call: Call<ChatTokenResponse>,
                    response: Response<ChatTokenResponse>
                ) {
                    if (response.isSuccessful) {
                        val token = response.body()?.chat_token
                        val user = User(
                            id = uid ?: "",
                            extraData = mutableMapOf(
                                "name" to displayName
                            )
                        )

                        ChatClient.instance().connectUser(user, token!!).enqueue { result ->
                            if (result.isSuccess) {
                                Log.d("Stream", "Connected to chat!")
                                val queryChannelsRequest = QueryChannelsRequest(
                                    filter = Filters.and(
                                        Filters.eq("type", "messaging")
                                    ),
                                    limit = 10
                                )

                                ChatClient.instance().queryChannels(queryChannelsRequest).enqueue { queryResult ->
                                    if (queryResult.isSuccess) {
                                        val channelsResponse = queryResult.getOrNull()
                                        if (channelsResponse != null) {
                                            channels.clear()
                                            channels.addAll(channelsResponse)
                                        }
                                        Log.d("Stream", "Channels retrieved successfully")
                                    } else {
                                        Log.e("Stream", "Channels result is null")
                                    }
                                }
                            } else {
                                Log.e("Stream", "Failed: ${result.errorOrNull()?.message}")
                            }
                        }

                    } else {
                        Log.e("TokenAPI", "Error: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<ChatTokenResponse>, t: Throwable) {
                    Log.e("TokenAPI", "Network error: ${t.message}")
                }
            })
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (channels.isEmpty()) {
            Text(text = "Connecting to chat...", style = MaterialTheme.typography.bodyLarge)
        } else {
            LazyColumn(modifier = Modifier.padding(16.dp)) {
                items(channels) { channel ->
                    Text(
                        text = channel.name ?: "No Name",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}


