package com.movieapp.features.movielist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.movieapp.theme.CartoonFontFamily
import com.movieapp.theme.NeoButton
import com.movieapp.theme.NeubrutalismIcons
import com.movieapp.theme.TypewriterFontFamily
import com.movieapp.theme.YoeshinFontFamily
import com.movieapp.theme.neoBorder
import com.movieapp.theme.neoColors
import com.movieapp.theme.neoShadow
import com.movieapp.util.t

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieListScreen(
    viewModel: MovieListViewModel,
    onTitleClick: (slug: String, isTvShow: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val gridState = rememberLazyGridState()
    val neoColors = MaterialTheme.neoColors

    // Pull-to-refresh state
    val pullRefreshState = rememberPullToRefreshState()
    if (pullRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            viewModel.refresh()
        }
    }

    LaunchedEffect(uiState.isRefreshing) {
        if (!uiState.isRefreshing) {
            pullRefreshState.endRefresh()
        }
    }

    // Detect scrolling near bottom to trigger pagination (US-03)
    val shouldPaginate by remember {
        derivedStateOf {
            val totalItems = gridState.layoutInfo.totalItemsCount
            val lastVisibleIndex = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 0 && lastVisibleIndex >= totalItems - 4
        }
    }

    LaunchedEffect(shouldPaginate) {
        if (shouldPaginate && !uiState.isPaginating && uiState.currentHasMore) {
            viewModel.loadNextPage()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(pullRefreshState.nestedScrollConnection)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Segmented Tabs: Movies vs TV Shows (US-01)
            CategorySegmentedTabs(
                selectedCategory = uiState.activeCategory,
                onCategorySelected = { viewModel.selectCategory(it) }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Initial Loading State with Skeleton Cards
            if (uiState.isInitialLoading && uiState.currentDisplayList.isEmpty()) {
                com.movieapp.theme.MovieListFeedSkeleton(modifier = Modifier.weight(1f))
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    state = gridState,
                    contentPadding = PaddingValues(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(
                        items = uiState.currentDisplayList,
                        key = { item -> "${item.id}_${item.slug}_${item.displayTitle}" }
                    ) { item ->
                        MovieGridCard(
                            item = item,
                            onClick = {
                                val slug = item.slug ?: item.id.toString()
                                val isTv = uiState.activeCategory == MediaCategory.TV_SHOWS
                                onTitleClick(slug, isTv)
                            }
                        )
                    }

                    // Inline Pagination Progress Indicator
                    if (uiState.isPaginating) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = neoColors.primary, strokeWidth = 3.dp)
                            }
                        }
                    }
                }
            }

            // Inline Error Notice & Retry Button
            uiState.errorMessage?.let { errorText ->
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .neoShadow(offsetX = 3.dp, offsetY = 3.dp, color = neoColors.shadow)
                        .background(neoColors.errorBackground, RoundedCornerShape(12.dp))
                        .neoBorder(width = 2.dp, color = neoColors.border, shape = RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = errorText,
                            fontFamily = YoeshinFontFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal,
                            color = neoColors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        NeoButton(
                            onClick = { viewModel.retry() },
                            text = t("try_again"),
                            backgroundColor = neoColors.primary,
                            contentColor = neoColors.textPrimary
                        )
                    }
                }
            }
        }

        // Pull to refresh indicator
        PullToRefreshContainer(
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            containerColor = neoColors.primary,
            contentColor = neoColors.textPrimary
        )
    }
}

/**
 * Accessible Neobrutalist segmented category tab bar.
 */
@Composable
private fun CategorySegmentedTabs(
    selectedCategory: MediaCategory,
    onCategorySelected: (MediaCategory) -> Unit
) {
    val neoColors = MaterialTheme.neoColors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .neoBorder(width = 2.dp, color = neoColors.border, shape = RoundedCornerShape(14.dp))
            .background(neoColors.surfaceMuted, RoundedCornerShape(14.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        val categories = listOf(
            MediaCategory.MOVIES to t("category_movies"),
            MediaCategory.TV_SHOWS to t("category_tv_shows")
        )

        categories.forEach { (category, label) ->
            val isSelected = selectedCategory == category
            val bg = if (isSelected) neoColors.primary else neoColors.surfaceMuted
            val shadowOffset = if (isSelected) 3.dp else 0.dp

            Box(
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 48.dp)
                    .then(
                        if (isSelected) {
                            Modifier
                                .neoShadow(offsetX = shadowOffset, offsetY = shadowOffset, color = neoColors.shadow, shape = RoundedCornerShape(10.dp))
                                .background(bg, RoundedCornerShape(10.dp))
                                .neoBorder(width = 2.dp, color = neoColors.border, shape = RoundedCornerShape(10.dp))
                        } else {
                            Modifier
                        }
                    )
                    .clickable { onCategorySelected(category) }
                    .semantics {
                        role = Role.Tab
                        selected = isSelected
                    }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    fontFamily = CartoonFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = neoColors.textPrimary
                )
            }
        }
    }
}

/**
 * Individual Movie or TV Show poster card with Neobrutalism border and hard shadow.
 */
@Composable
private fun MovieGridCard(
    item: MovieDTO,
    onClick: () -> Unit
) {
    val neoColors = MaterialTheme.neoColors
    val a11yLabel = "${item.displayTitle}, released in ${item.displayYear}, rating ${item.formattedRating} out of 10"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .neoShadow(offsetX = 3.dp, offsetY = 3.dp, color = neoColors.shadow, shape = RoundedCornerShape(12.dp))
            .background(neoColors.surface, RoundedCornerShape(12.dp))
            .neoBorder(width = 2.dp, color = neoColors.border, shape = RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .semantics {
                role = Role.Button
                contentDescription = a11yLabel
            }
    ) {
        // Poster Image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .background(neoColors.surfaceMuted)
        ) {
            AsyncImage(
                model = item.poster,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Rating Badge with Star Icon
            val itemRating = item.rating
            if (itemRating != null && itemRating > 0.0) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .neoBorder(width = 1.5.dp, color = neoColors.border, shape = RoundedCornerShape(6.dp))
                        .background(neoColors.primary, RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(
                        imageVector = NeubrutalismIcons.Star,
                        contentDescription = null,
                        tint = neoColors.textPrimary,
                        modifier = Modifier.size(11.dp)
                    )
                    Text(
                        text = item.formattedRating,
                        fontFamily = TypewriterFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = neoColors.textPrimary
                    )
                }
            }
        }

        // Details Row
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Text(
                text = item.displayTitle,
                fontFamily = CartoonFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = neoColors.textPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.displayYear,
                    fontFamily = TypewriterFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = neoColors.textSecondary
                )
                val isTv = item.mediaType?.contains("tv", ignoreCase = true) == true
                val typeLabel = if (isTv) t("badge_tv_show") else t("badge_movie")
                val typeBg = if (isTv) neoColors.secondary else neoColors.tertiary
                Box(
                    modifier = Modifier
                        .neoBorder(width = 1.5.dp, color = neoColors.border, shape = RoundedCornerShape(4.dp))
                        .background(typeBg, RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = typeLabel,
                        fontFamily = TypewriterFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        color = neoColors.textPrimary
                    )
                }
            }
        }
    }
}
