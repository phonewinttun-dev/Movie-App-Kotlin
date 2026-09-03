package com.movieapp.features.search

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.movieapp.features.movielist.MovieDTO
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
fun SearchScreen(
    viewModel: SearchViewModel,
    onTitleClick: (slug: String, isTvShow: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Look for a movie or show",
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            color = NeoBlack
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Accessible Search Bar with Clear Button (US-04)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .neoShadow(offsetX = 4.dp, offsetY = 4.dp, shape = RoundedCornerShape(12.dp))
                .background(NeoWhite, RoundedCornerShape(12.dp))
                .neoBorder(shape = RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = uiState.query,
                onValueChange = { viewModel.onQueryChange(it) },
                textStyle = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeoBlack
                ),
                cursorBrush = SolidColor(NeoBlack),
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .semantics { contentDescription = "Search text input field" },
                decorationBox = { innerTextField ->
                    if (uiState.query.isEmpty()) {
                        Text(
                            text = "Type a title or topic...",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF888888)
                        )
                    }
                    innerTextField()
                }
            )

            if (uiState.query.isNotEmpty()) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                        .background(NeoBlack, RoundedCornerShape(8.dp))
                        .clickable { viewModel.clearQuery() }
                        .semantics {
                            role = Role.Button
                            contentDescription = "Clear search text"
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Clear",
                        color = NeoWhite,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Results update automatically as you type.",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF666666)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Content Area: Loading, Initial Prompt, Empty State, Error, or Results
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
                        .neoShadow(offsetX = 3.dp, offsetY = 3.dp)
                        .background(NeoErrorBackground, RoundedCornerShape(12.dp))
                        .neoBorder(shape = RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = uiState.errorMessage ?: "",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeoBlack
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        NeoButton(
                            onClick = { viewModel.retry() },
                            text = "Try Again",
                            backgroundColor = NeoYellow
                        )
                    }
                }
            }
            uiState.isEmptyResult -> {
                // Empty State with polite guidance (US-05)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .neoShadow(offsetX = 4.dp, offsetY = 4.dp, shape = RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFF0F2), RoundedCornerShape(12.dp))
                        .neoBorder(shape = RoundedCornerShape(12.dp))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No Matches Found",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = NeoBlack
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "We could not find any titles matching your search. Check your spelling or search for another title.",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF444444)
                        )
                    }
                }
            }
            uiState.isInitial -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .neoShadow(offsetX = 3.dp, offsetY = 3.dp, shape = RoundedCornerShape(12.dp))
                        .background(Color(0xFFF2EFE9), RoundedCornerShape(12.dp))
                        .neoBorder(shape = RoundedCornerShape(12.dp))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Search by Title",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = NeoBlack
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Type in the box above to find movies or television series.",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF666666)
                        )
                    }
                }
            }
            else -> {
                // Search Results List
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(
                        items = uiState.results,
                        key = { "${it.id}_${it.slug}_${it.displayTitle}" }
                    ) { item ->
                        SearchResultCard(
                            item = item,
                            onClick = {
                                val isTv = item.mediaType?.contains("tv", ignoreCase = true) == true
                                val slug = item.slug ?: item.id.toString()
                                onTitleClick(slug, isTv)
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Result row card featuring movie poster, metadata, and category tag.
 */
@Composable
private fun SearchResultCard(
    item: MovieDTO,
    onClick: () -> Unit
) {
    val a11yLabel = "${item.displayTitle}, released in ${item.displayYear}, rating ${item.formattedRating}"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .neoShadow(offsetX = 3.dp, offsetY = 3.dp, shape = RoundedCornerShape(12.dp))
            .background(NeoWhite, RoundedCornerShape(12.dp))
            .neoBorder(shape = RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(10.dp)
            .semantics {
                role = Role.Button
                contentDescription = a11yLabel
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail
        Box(
            modifier = Modifier
                .size(width = 54.dp, height = 76.dp)
                .neoBorder(width = 1.5.dp, shape = RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFE0E0E0))
        ) {
            AsyncImage(
                model = item.poster,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Metadata
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.displayTitle,
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = NeoBlack
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${item.displayYear} • Rating ${item.formattedRating}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF666666)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Badge [Movie] or [TV Show]
        val isTv = item.mediaType?.contains("tv", ignoreCase = true) == true
        val badgeText = if (isTv) "TV Show" else "Movie"
        val badgeColor = if (isTv) NeoPink else NeoCyan

        Box(
            modifier = Modifier
                .neoBorder(width = 1.5.dp, shape = RoundedCornerShape(6.dp))
                .background(badgeColor, RoundedCornerShape(6.dp))
                .padding(horizontal = 6.dp, vertical = 3.dp)
        ) {
            Text(
                text = badgeText,
                fontWeight = FontWeight.Black,
                fontSize = 10.sp,
                color = NeoBlack
            )
        }
    }
}
