package com.cineverse.android.features.home

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cineverse.core.designsystem.components.MovieCard
import com.cineverse.domain.model.Movie
import com.cineverse.presentation.home.HomeEffect
import com.cineverse.presentation.home.HomeIntent
import com.cineverse.presentation.home.HomeTab
import com.cineverse.presentation.home.HomeViewModel
import com.seiko.imageloader.rememberImagePainter
import kotlinx.coroutines.delay
import org.koin.compose.koinInject
import androidx.compose.foundation.Image

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onNavigateToDetails: (Int) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is HomeEffect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        
        TabRow(
            selectedTabIndex = state.currentTab.ordinal,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[state.currentTab.ordinal]),
                    color = MaterialTheme.colorScheme.primary
                )
            },
            divider = {}
        ) {
            HomeTab.values().forEach { tab ->
                Tab(
                    selected = state.currentTab == tab,
                    onClick = { viewModel.sendIntent(HomeIntent.ChangeTab(tab)) },
                    text = {
                        Text(
                            text = if (tab == HomeTab.Trending) "Trending" else "Now Playing",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (state.currentTab == tab) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        if (state.isLoading && state.movies.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                
                if (state.nowPlaying.isNotEmpty()) {
                    item(span = { GridItemSpan(2) }) {
                        val pagerState = rememberPagerState(pageCount = { state.nowPlaying.size })

                        LaunchedEffect(Unit) {
                            while (true) {
                                delay(4000)
                                if (pagerState.pageCount > 0) {
                                    val next = (pagerState.currentPage + 1) % pagerState.pageCount
                                    pagerState.animateScrollToPage(next, animationSpec = tween(800))
                                }
                            }
                        }

                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .padding(16.dp)
                                .clip(RoundedCornerShape(16.dp))
                        ) { page ->
                            val movie = state.nowPlaying[page]
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable { onNavigateToDetails(movie.id) }
                            ) {
                                
                                val painter = rememberImagePainter(movie.posterPath ?: "")
                                Image(
                                    painter = painter,
                                    contentDescription = movie.title,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.Transparent,
                                                    Color.Black.copy(alpha = 0.8f)
                                                )
                                            )
                                        )
                                )

                                Column(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = movie.title,
                                        style = MaterialTheme.typography.headlineLarge.copy(
                                            color = Color.White,
                                            fontSize = 20.sp
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Rating: ★ ${movie.voteAverage}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "•  ${movie.releaseDate}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color.LightGray
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item(span = { GridItemSpan(2) }) {
                    Text(
                        text = if (state.currentTab == HomeTab.Trending) "Trending This Week" else "Now Playing in Cinemas",
                        style = MaterialTheme.typography.headlineLarge,
                        modifier = Modifier.padding(start = 16.dp, top = 8.dp)
                    )
                }

                val listToDisplay = if (state.currentTab == HomeTab.Trending) state.movies else state.nowPlaying
                items(listToDisplay) { movie ->
                    Column(
                        modifier = Modifier.padding(
                            start = if (listToDisplay.indexOf(movie) % 2 == 0) 16.dp else 0.dp,
                            end = if (listToDisplay.indexOf(movie) % 2 != 0) 16.dp else 0.dp
                        )
                    ) {
                        MovieCard(
                            imageUrl = movie.posterPath,
                            onClick = { onNavigateToDetails(movie.id) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = movie.title,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "★ ${movie.voteAverage}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
