package com.training.graduation.screens.chat

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.training.graduation.chat_token.ChatTokenRequest
import com.training.graduation.chat_token.ChatTokenResponse
import com.training.graduation.chat_token.RetrofitClient
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.models.User
//import io.getstream.chat.android.client.models.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.lifecycle.viewmodel.compose.viewModel
import com.training.graduation.navigation.BottomNavigationBar
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.compose.ui.channels.list.ChannelList
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.channels.ChannelListViewModel
import io.getstream.chat.android.compose.viewmodel.channels.ChannelViewModelFactory
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.models.querysort.QuerySorter


@Composable
fun ChatListScreen(navController: NavController) {
    val context = LocalContext.current
    val currentUser = FirebaseAuth.getInstance().currentUser
    val uid = currentUser?.uid
    val displayName = currentUser?.displayName ?: "Guest"

    val chatClient = ChatClient.Builder("8udrnnksucjd",context)
        .logLevel(ChatLogLevel.ALL)
        .build()

    //val chatClient = ChatClient.instance()

//    val filter = Filters.and(
//        Filters.eq("type", "messaging"),  // نوع القناة
//        Filters.`in`("members", listOf(uid ?: ""))  // أعضاء القناة
//    )

    // إنشاء الـ ViewModel يدويًا
    val viewModel: ChannelListViewModel = viewModel(
        factory=ChannelViewModelFactory(
            chatClient = chatClient,
        )
    )

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
                                Log.d("Stream", "✅ Connected to chat!")
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

    ChatTheme {
        ChannelList(
            viewModel = viewModel,
            onChannelClick = { channel ->
                navController.navigate("messages/${channel.cid}")
            }
        )
        BottomNavigationBar(navController = navController)
    }


}





