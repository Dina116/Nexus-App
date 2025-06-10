package com.training.graduation.screens.mainscreen

import SignupScreen
import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSavedStateRegistryOwner
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.messaging.FirebaseMessaging
import com.training.graduation.navigation.BottomNavigationBar
import com.training.graduation.onboarding.OnboardingScreen
import com.training.graduation.screens.Authentication.AuthViewModel
import com.training.graduation.screens.Authentication.ForgotPasswordScreen
import com.training.graduation.screens.group.GroupListScreen
import com.training.graduation.screens.Authentication.LoginScreen
import com.training.graduation.screens.group.AddMemberScreen
import com.training.graduation.screens.group.AddMemberViewModel
import com.training.graduation.screens.group.AddMemberViewModelFactory
import com.training.graduation.screens.group.ChatRepository
import com.training.graduation.screens.group.CreateGroupScreen
import com.training.graduation.screens.group.GroupDetailsScreen
import com.training.graduation.screens.group.GroupDetailsViewModel
import com.training.graduation.screens.group.GroupDetailsViewModelFactory
import com.training.graduation.screens.group.GroupScreen
import com.training.graduation.screens.group.GroupScreenViewModel
import com.training.graduation.screens.group.GroupScreenViewModelFactory
import com.training.graduation.screens.schedule.ScheduleMeeting
import com.training.graduation.screens.notification.NotificationScreen
import com.training.graduation.screens.profile.Profile
import com.training.graduation.screens.profile.UserProfileScreen
import com.training.graduation.screens.sharedprefrence.PreferenceManager
import com.training.graduation.screens.sharedprefrence.UpdateLocale
import com.training.graduation.screens.startmeeting.JitsiMeetCompose
import com.training.graduation.screens.startmeeting.PdfReportsScreen
import com.training.graduation.ui.theme.GraduationTheme
import com.training.graduation.uploadvideo.UploadVideoScreen
import com.training.graduation.uploadvideo.VideoResultScreen
import org.jitsi.meet.sdk.JitsiMeetActivityDelegate
import java.net.URLDecoder
import java.net.URLEncoder


class MainActivity : ComponentActivity() {

    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                Toast.makeText(this, "Camera permission is required for this feature", Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestCameraPermission()
        JitsiMeetActivityDelegate.onHostResume(this)

        val authViewModel : AuthViewModel by viewModels()
        val preferenceManager = PreferenceManager(this)

        UpdateLocale(this, preferenceManager.getLanguage())

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d("FCM_TOKEN", token)
            } else {
                Log.e("FCM_TOKEN", "Token fetch failed", task.exception)
            }
        }

        setContent {
            GraduationTheme {

                    AppNavigation(preferenceManager,authViewModel)

            }
        }
    }
}
@Composable
fun AppNavigation(preferenceManager:PreferenceManager,authViewModel:AuthViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val bottomNavRoutes = listOf("homescreen", "group", "userprofile")

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomNavRoutes) {
                BottomNavigationBar(navController = navController)
            }
        }
    ){ paddingValues ->
        NavHost(navController = navController, startDestination = "Onboarding",modifier = Modifier.padding(paddingValues )) {
            composable(route = "Onboarding") {
                OnboardingScreen (navController, innerpadding = paddingValues)
            }
            composable(route = "loginscreen") {
                LoginScreen( modifier = Modifier,navController,authViewModel,paddingValues)
            }
            composable(route = "signupscreen") {
                SignupScreen(modifier = Modifier,navController,authViewModel, innerpadding = paddingValues)
            }
            composable(route = "homescreen") {
                HomeScreen(
                    Modifier,
                    navController, innerpadding = paddingValues,
                    authViewModel =authViewModel
                )
            }
            composable(route = "forgotpassword") {
                ForgotPasswordScreen(navController, innerpadding = paddingValues)

            }
            composable(route = "group") {
                GroupListScreen(
                    onGroupClick = { Group ->
                        val groupId = Group.groupId
                        val groupName = Group.groupName
                        val encodedGroupName = URLEncoder.encode(groupName, "UTF-8")
                        navController.navigate("chat/$groupId/$encodedGroupName")
                    },
                    onCreateGroupClick = {
                        navController.navigate("create_group")
                    },
                    innerpadding =paddingValues
                )
            }

            composable(
                route = "chat/{groupId}/{groupName}",
                arguments = listOf(
                    navArgument("groupId") { type = NavType.StringType },
                    navArgument("groupName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val groupId = backStackEntry.arguments?.getString("groupId")
                val encodedGroupName = backStackEntry.arguments?.getString("groupName")
                val groupName = encodedGroupName?.let { URLDecoder.decode(it, "UTF-8") } ?: "Chat"
                val repository = ChatRepository()
                val owner = LocalSavedStateRegistryOwner.current
                val factory = GroupScreenViewModelFactory(repository, owner, backStackEntry.arguments)
                val viewModel: GroupScreenViewModel = viewModel(factory = factory)
                if (groupId != null) {
                    GroupScreen(
                        viewModel = viewModel,
                        groupName = groupName,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToGroupDetails = { currentGroupId ->
                            navController.navigate("group_details/$currentGroupId/$encodedGroupName")
                        }
                        , innerpadding = paddingValues
                    )
                } else {
                    Text("Error: Group ID not found")
                }
            }

            composable(
                route = "group_details/{groupId}/{groupName}",
                arguments = listOf(navArgument("groupId") { type = NavType.StringType },
                    navArgument("groupName") { type = NavType.StringType }
                )

            ) { detailsBackStackEntry ->
                val encodedGroupName = detailsBackStackEntry.arguments?.getString("groupName")
                val groupName = encodedGroupName?.let { URLDecoder.decode(it, "UTF-8") } ?: "Chat"
                val detailsGroupId = detailsBackStackEntry.arguments?.getString("groupId")
                val detailsGroupName =groupName
                val detailsRepository = ChatRepository()
                val detailsOwner = LocalSavedStateRegistryOwner.current
                val detailsFactory = GroupDetailsViewModelFactory(detailsRepository, detailsOwner, detailsBackStackEntry.arguments)
                val detailsViewModel: GroupDetailsViewModel = viewModel(factory = detailsFactory)

                if (detailsGroupId != null) {
                    GroupDetailsScreen(
                        viewModel = detailsViewModel,
                        groupName = detailsGroupName,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToAddMember = { gid ->
                            navController.navigate("add_member/$gid")
                        }
                        , innerpadding = paddingValues
                    )
                }
            }

            composable(
                route = "add_member/{groupId}",
                arguments = listOf(navArgument("groupId") { type = NavType.StringType })
            ) { addMemberBackStackEntry ->
                val addMemberGroupId = addMemberBackStackEntry.arguments?.getString("groupId")

                val addMemberRepository = ChatRepository()
                val addMemberOwner = LocalSavedStateRegistryOwner.current
                val addMemberFactory = AddMemberViewModelFactory(addMemberRepository, addMemberOwner, addMemberBackStackEntry.arguments)
                val addMemberViewModel: AddMemberViewModel = viewModel(factory = addMemberFactory)

                AddMemberScreen(
                    viewModel = addMemberViewModel,
                    onNavigateBack = { navController.popBackStack() }
                    , innerpadding = paddingValues
                )
            }

            composable(route = "create_group") {
                CreateGroupScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onGroupCreated = { newGroupId, newGroupName  ->
                        navController.popBackStack()
                        val encodedGroupName = URLEncoder.encode(newGroupName, "UTF-8")
                        navController.navigate("chat/$newGroupId/$encodedGroupName")
                    }
                )
            }
            composable(route = "userprofile") {
                UserProfileScreen(navController,preferenceManager =preferenceManager)
            }
            composable(route = "schedule") {
                ScheduleMeeting(navController)
            }
            composable(route="editProfile"){
                Profile(navController)
            }
            composable(route="notification_screen"){
                NotificationScreen( navController)
            }
            composable(route="start_meeting") {
                JitsiMeetCompose(navController, innerpadding = paddingValues)
            }
            composable("pdf_reports") {
                PdfReportsScreen(navController, innerpadding = paddingValues)
            }
            composable("upload_video_screen") {
                UploadVideoScreen(navController)
            }
            composable("video_result_screen") {
                VideoResultScreen()
            }

        }
    }
}




