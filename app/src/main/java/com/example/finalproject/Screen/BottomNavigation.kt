package com.example.finalproject.Screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.finalproject.Model.BottomIcon

@Composable
fun bottomNavigation(navController: NavController,innerPaddingValues: PaddingValues = PaddingValues()){
    Scaffold(
        bottomBar = {myBottomBar(navController)}
    ){
        LazyColumn(modifier = Modifier.padding(it)){

        }
    }
}


@Composable
fun myBottomBar(navController: NavController){
    val backStack = navController.currentBackStackEntryAsState().value

    val listOfIcon = listOf(
        BottomIcon(
            title = "home",
            route = Screens.Home.route,
            icon = Icons.Default.Home
        ),
        BottomIcon(
            title = "notification",
            route = Screens.Notification.route,
            icon = Icons.Default.Notifications
        ),
        BottomIcon(
            title = "Add thread",
            route = Screens.AddThread.route,
            icon = Icons.Filled.Add
        ),
        BottomIcon(
            title = "Search",
            route = Screens.Search.route,
            icon = Icons.Default.Search
        ),
        BottomIcon(
            title = "Profile",
            route = Screens.Profile.route,
            icon = Icons.Default.AccountCircle
        )
    )

    BottomAppBar{
        listOfIcon.forEach{ item->
            val isSelected = item.route == backStack?.destination?.route
            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    navController.navigate(item.route)
                },
                icon = {
                    Icon(imageVector = item.icon, contentDescription = null)
                }
            )

        }
    }
}
