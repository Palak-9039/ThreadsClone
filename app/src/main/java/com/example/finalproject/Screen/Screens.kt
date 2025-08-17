package com.example.finalproject.Screen

sealed class Screens(
    val route : String
){
    object Home:Screens("home")
    object Profile:Screens("profile")
    object Notification:Screens("notification")
    object Search:Screens("search")
    object AddThread:Screens("add-thread")
    object Splash:Screens("splash")
    object BottomNavigation:Screens("bottom-navigation")
    object SignIn:Screens("sign-in")
    object SignUp:Screens("sign-up")
    object OtherProfile:Screens("other-profile")
    object CommentsScreen:Screens("comments")
    object ProfileSettingScreen: Screens("profile_settings")
}