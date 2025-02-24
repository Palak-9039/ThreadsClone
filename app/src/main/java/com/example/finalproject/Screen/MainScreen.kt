@file:Suppress("DEPRECATION")

package com.example.finalproject.Screen

import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabPosition
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.finalproject.R
import com.example.finalproject.ViewModel.HomeViewModel
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import com.example.finalproject.ViewModel.ThreadViewModel
import kotlinx.coroutines.launch
import kotlin.concurrent.thread


@Composable
fun MainScreen(navController: NavController,
               homeViewModel: HomeViewModel,
               threadViewModel: ThreadViewModel
){
    val pagerState : PagerState = rememberPagerState(initialPage = 0,pageCount = {2})
    val coroutineScope = rememberCoroutineScope()

    Scaffold (bottomBar = { myBottomBar(navController)}) {
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(it)
            .background(MaterialTheme.colorScheme.background)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.threads_logo),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground)
                )
            }
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                indicator = { tabPositions: List<TabPosition> ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(
                            currentTabPosition = tabPositions[pagerState.currentPage]
                        ),
                        color = Color.White // Set the indicator color to white
                    )

                }
            ) {
                Tab(
                    text = {
                        Text(
                            "for you",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    },
                    selected = pagerState.currentPage == 0,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(0) } },
                    modifier = Modifier.padding(10.dp)
                )
                Tab(
                    text = {
                        Text(
                            "Following",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    },
                    selected = pagerState.currentPage == 1,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(1) } },
                    modifier = Modifier.padding(10.dp)

                )
            }

            HorizontalPager(state = pagerState) { page ->
                when (page) {
                    0 -> Home(navController, homeViewModel,threadViewModel)
                    1 -> followingThreadsScreen(navController, homeViewModel,threadViewModel)
                }
            }
        }
    }

    }