package com.movieapp.features.moviedetail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.movieapp.theme.NeoBlack
import com.movieapp.theme.NeoButton
import com.movieapp.theme.NeoCyan
import com.movieapp.theme.NeoErrorBackground
import com.movieapp.theme.NeoPink
import com.movieapp.theme.NeoWhite
import com.movieapp.theme.NeoYellow
import com.movieapp.theme.neoBorder
import com.movieapp.theme.neoShadow

@Composable
fun MovieDetailScreen(
    viewModel: MovieDetailViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top Navigation Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                    .neoShadow(offsetX = 3.dp, offsetY = 3.dp, shape = RoundedCornerShape(10.dp))
                    .background(NeoWhite, RoundedCornerShape(10.dp))
                    .neoBorder(shape = RoundedCornerShape(10.dp))
                    .clickable(onClick = onBackClick)
                    .semantics { role = Role.Button }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Back to List",
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    color = NeoBlack
                )
            }

            val badgeText = if (uiState.isTvShow) "TV Show" else "Movie"
            val badgeColor = if (uiState.isTvShow) NeoPink else NeoCyan

            Box(
                modifier = Modifier
                    .neoBorder(width = 2.dp, shape = RoundedCornerShape(8.dp))
                    .background(badgeColor, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = badgeText,
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp,
                    color = NeoBlack
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = NeoBlack, strokeWidth = 3.dp)
                }
            }
            uiState.errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .neoShadow(offsetX = 4.dp, offsetY = 4.dp, shape = RoundedCornerShape(12.dp))
                        .background(NeoErrorBackground, RoundedCornerShape(12.dp))
                        .neoBorder(shape = RoundedCornerShape(12.dp))
                        .padding(20.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = uiState.errorMessage ?: "",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeoBlack
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        NeoButton(
                            onClick = { viewModel.retry() },
                            text = "Try Again",
                            backgroundColor = NeoYellow
                        )
                    }
                }
            }
            uiState.detail != null -> {
                val detail = uiState.detail!!

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState)
                ) {
                    // Poster Banner Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .neoShadow(offsetX = 4.dp, offsetY = 4.dp, shape = RoundedCornerShape(14.dp))
                            .background(Color(0xFFE0E0E0), RoundedCornerShape(14.dp))
                            .neoBorder(shape = RoundedCornerShape(14.dp))
                            .clip(RoundedCornerShape(14.dp))
                    ) {
                        AsyncImage(
                            model = detail.backdrop ?: detail.poster,
                            contentDescription = "Banner image for ${detail.displayTitle}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Title and Rating Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = detail.displayTitle,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = NeoBlack,
                                modifier = Modifier.semantics { heading() }
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = detail.displayYear,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF555555)
                                )
                                detail.runtime?.let {
                                    Text(text = "•", color = Color(0xFF888888))
                                    Text(
                                        text = it,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF555555)
                                    )
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .neoShadow(offsetX = 3.dp, offsetY = 3.dp, shape = RoundedCornerShape(10.dp))
                                .background(NeoYellow, RoundedCornerShape(10.dp))
                                .neoBorder(shape = RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = detail.formattedRating,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                color = NeoBlack
                            )
                        }
                    }

                    // Genre Badges
                    if (detail.safeGenres.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            detail.safeGenres.forEach { genre ->
                                Box(
                                    modifier = Modifier
                                        .neoShadow(offsetX = 2.dp, offsetY = 2.dp, shape = RoundedCornerShape(8.dp))
                                        .background(Color(0xFFFFFBE6), RoundedCornerShape(8.dp))
                                        .neoBorder(width = 1.5.dp, shape = RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = genre,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = NeoBlack
                                    )
                                }
                            }
                        }
                    }

                    // Story Summary Card
                    Spacer(modifier = Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .neoShadow(offsetX = 4.dp, offsetY = 4.dp, shape = RoundedCornerShape(12.dp))
                            .background(NeoWhite, RoundedCornerShape(12.dp))
                            .neoBorder(shape = RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Text(
                                text = "Story Summary",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = NeoBlack,
                                modifier = Modifier.semantics { heading() }
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = detail.plot ?: "No summary is currently available for this title.",
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color(0xFF333333)
                            )
                        }
                    }

                    // TV Shows: Seasons and Episodes (US-07)
                    if (uiState.isTvShow && detail.safeSeasons.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Choose Season and Episode",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = NeoBlack,
                            modifier = Modifier.semantics { heading() }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Season Selector Tabs
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(detail.safeSeasons) { season ->
                                val isSelected = season.seasonNumber == uiState.selectedSeasonNumber
                                val bg = if (isSelected) NeoBlack else NeoWhite
                                val textCol = if (isSelected) NeoWhite else NeoBlack

                                Box(
                                    modifier = Modifier
                                        .defaultMinSize(minWidth = 48.dp, minHeight = 44.dp)
                                        .neoShadow(offsetX = 2.dp, offsetY = 2.dp, shape = RoundedCornerShape(8.dp))
                                        .background(bg, RoundedCornerShape(8.dp))
                                        .neoBorder(width = 2.dp, shape = RoundedCornerShape(8.dp))
                                        .clickable { viewModel.selectSeason(season.seasonNumber) }
                                        .semantics {
                                            role = Role.Tab
                                            selected = isSelected
                                        }
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = season.displayName,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = textCol
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Episode Cards List
                        uiState.activeSeason?.safeEpisodes?.forEach { episode ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .neoShadow(offsetX = 2.dp, offsetY = 2.dp, shape = RoundedCornerShape(10.dp))
                                    .background(NeoWhite, RoundedCornerShape(10.dp))
                                    .neoBorder(width = 2.dp, shape = RoundedCornerShape(10.dp))
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .neoBorder(width = 1.5.dp, shape = RoundedCornerShape(6.dp))
                                                .background(NeoYellow, RoundedCornerShape(6.dp))
                                                .padding(horizontal = 6.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = "${episode.episodeNumber}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Black,
                                                color = NeoBlack
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = episode.displayTitle,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = NeoBlack
                                        )
                                    }
                                    episode.runtime?.let {
                                        Text(
                                            text = it,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF666666)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
