package com.movieapp.features.downloadlinks

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.movieapp.theme.CartoonFontFamily
import com.movieapp.theme.NeubrutalismIcons
import com.movieapp.theme.TypewriterFontFamily
import com.movieapp.theme.YoeshinFontFamily
import com.movieapp.theme.neoBorder
import com.movieapp.theme.neoColors
import com.movieapp.theme.neoShadow
import com.movieapp.util.LocalizationManager
import com.movieapp.util.t
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadLinksBottomSheet(
    title: String,
    downloadLinks: List<DownloadLinkDTO>,
    sheetState: SheetState,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val neoColors = MaterialTheme.neoColors
    val coroutineScope = rememberCoroutineScope()

    // Resolution Filter: Extract distinct resolutions (e.g. 720p, 1080p, 4K)
    val availableResolutions = remember(downloadLinks) {
        listOf("All") + downloadLinks.mapNotNull { it.resolution?.trim() }.filter { it.isNotBlank() }.distinct()
    }
    var selectedResolution by remember { mutableStateOf("All") }

    val filteredLinks = remember(downloadLinks, selectedResolution) {
        if (selectedResolution == "All") {
            downloadLinks
        } else {
            downloadLinks.filter { it.resolution?.trim().equals(selectedResolution, ignoreCase = true) }
        }
    }

    var resolvingLinkId by remember { mutableStateOf<Long?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = neoColors.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = t("download_links"),
                        fontFamily = CartoonFontFamily,
                        fontSize = 18.sp,
                        color = neoColors.textPrimary
                    )
                    Text(
                        text = title,
                        fontFamily = YoeshinFontFamily,
                        fontSize = 13.sp,
                        color = neoColors.textSecondary,
                        modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                    )
                }

                if (downloadLinks.size > 1) {
                    Box(
                        modifier = Modifier
                            .defaultMinSize(minWidth = 48.dp, minHeight = 40.dp)
                            .neoShadow(offsetX = 2.dp, offsetY = 2.dp, color = neoColors.shadow, shape = RoundedCornerShape(8.dp))
                            .background(neoColors.primary, RoundedCornerShape(8.dp))
                            .neoBorder(width = 1.5.dp, color = neoColors.border, shape = RoundedCornerShape(8.dp))
                            .clickable {
                                val allUrls = downloadLinks.mapNotNull { it.url }.filter { it.isNotBlank() }.joinToString("\n")
                                DownloadManagerHelper.copyLinkToClipboard(context, title, allUrls)
                                Toast.makeText(context, LocalizationManager.getString("all_links_copied"), Toast.LENGTH_SHORT).show()
                            }
                            .semantics { role = Role.Button }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = NeubrutalismIcons.Copy,
                                contentDescription = null,
                                tint = neoColors.textPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = t("copy_all_links"),
                                fontFamily = CartoonFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = neoColors.textPrimary
                            )
                        }
                    }
                }
            }

            // Resolution Filter Chips (720p / 1080p / 4K / All)
            if (availableResolutions.size > 2) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(bottom = 14.dp)
                ) {
                    availableResolutions.forEach { res ->
                        val isSelected = res == selectedResolution
                        val label = if (res == "All") t("res_all") else res
                        val bg = if (isSelected) neoColors.primary else neoColors.surfaceMuted

                        Box(
                            modifier = Modifier
                                .then(
                                    if (isSelected) {
                                        Modifier
                                            .neoShadow(offsetX = 2.dp, offsetY = 2.dp, color = neoColors.shadow, shape = RoundedCornerShape(8.dp))
                                            .background(bg, RoundedCornerShape(8.dp))
                                            .neoBorder(width = 2.dp, color = neoColors.border, shape = RoundedCornerShape(8.dp))
                                    } else {
                                        Modifier
                                            .background(bg, RoundedCornerShape(8.dp))
                                            .neoBorder(width = 1.dp, color = neoColors.border, shape = RoundedCornerShape(8.dp))
                                    }
                                )
                                .clickable { selectedResolution = res }
                                .semantics {
                                    role = Role.Tab
                                    selected = isSelected
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontFamily = CartoonFontFamily,
                                fontSize = 12.sp,
                                color = neoColors.textPrimary
                            )
                        }
                    }
                }
            }

            if (filteredLinks.isEmpty()) {
                Text(
                    text = t("no_download_links"),
                    fontFamily = YoeshinFontFamily,
                    fontSize = 13.sp,
                    color = neoColors.textSecondary,
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    items(filteredLinks) { link ->
                        val isResolving = resolvingLinkId == link.id

                        DownloadLinkCard(
                            link = link,
                            isResolving = isResolving,
                            onOpenLink = {
                                if (link.isTelegram) {
                                    // tg:// protocol instant launch
                                    DownloadManagerHelper.openTelegram(context, link.url ?: "")
                                } else {
                                    // In-App Direct Download with Ad/Timer Bypass Resolver
                                    val resolvingMsg = LocalizationManager.getString("resolving_link")
                                    coroutineScope.launch {
                                        resolvingLinkId = link.id
                                        Toast.makeText(context, resolvingMsg, Toast.LENGTH_SHORT).show()
                                        val result = DirectDownloadResolver.resolveDirectUrl(link)
                                        resolvingLinkId = null
                                        result.onSuccess { directUrl ->
                                            DownloadManagerHelper.startNativeDownload(context, title, directUrl)
                                        }.onFailure {
                                            // Fallback to browser if direct media stream cannot be verified
                                            val fallbackMsg = LocalizationManager.getString("opening_browser_for_full_video")
                                            Toast.makeText(context, fallbackMsg, Toast.LENGTH_LONG).show()
                                            DownloadManagerHelper.openExternalLink(context, link)
                                        }
                                    }
                                }
                            },
                            onCopyLink = {
                                coroutineScope.launch {
                                    val rawUrl = link.url?.trim() ?: return@launch
                                    if (rawUrl.contains("megaup.net", ignoreCase = true)) {
                                        Toast.makeText(context, LocalizationManager.getString("resolving_direct_link"), Toast.LENGTH_SHORT).show()
                                        val resolved = DirectDownloadResolver.resolveDirectUrlForCopy(link)
                                        val copyTarget = resolved.getOrDefault(rawUrl)
                                        DownloadManagerHelper.copyLinkToClipboard(context, link.cleanServerName, copyTarget)
                                        Toast.makeText(context, LocalizationManager.getString("direct_link_copied"), Toast.LENGTH_SHORT).show()
                                    } else {
                                        DownloadManagerHelper.copyLinkToClipboard(context, link.cleanServerName, rawUrl)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DownloadLinkCard(
    link: DownloadLinkDTO,
    isResolving: Boolean = false,
    onOpenLink: () -> Unit,
    onCopyLink: () -> Unit
) {
    val neoColors = MaterialTheme.neoColors

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = neoColors.surfaceMuted
        ),
        modifier = Modifier
            .fillMaxWidth()
            .neoBorder(width = 1.5.dp, color = neoColors.border, shape = RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = link.cleanServerName,
                        fontFamily = CartoonFontFamily,
                        fontSize = 15.sp,
                        color = neoColors.textPrimary
                    )
                    link.resolution?.takeIf { it.isNotBlank() }?.let { res ->
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .neoBorder(width = 1.dp, color = neoColors.border, shape = RoundedCornerShape(4.dp))
                                .background(neoColors.primary, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = res,
                                fontFamily = TypewriterFontFamily,
                                fontSize = 10.sp,
                                color = neoColors.textPrimary
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    link.quality?.takeIf { it.isNotBlank() }?.let {
                        Text(
                            text = it,
                            fontFamily = TypewriterFontFamily,
                            fontSize = 12.sp,
                            color = neoColors.textSecondary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    link.size?.takeIf { it.isNotBlank() }?.let {
                        Text(
                            text = "($it)",
                            fontFamily = TypewriterFontFamily,
                            fontSize = 12.sp,
                            color = neoColors.textSecondary
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onCopyLink,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = NeubrutalismIcons.Copy,
                        contentDescription = t("copy_link"),
                        tint = neoColors.textPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Button(
                    onClick = onOpenLink,
                    enabled = !isResolving,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (link.isTelegram) neoColors.tertiary else neoColors.primary,
                        contentColor = neoColors.textPrimary
                    ),
                    modifier = Modifier.neoBorder(width = 1.5.dp, color = neoColors.border, shape = RoundedCornerShape(8.dp))
                ) {
                    if (isResolving) {
                        CircularProgressIndicator(
                            color = neoColors.textPrimary,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Icon(
                            imageVector = if (link.isTelegram) NeubrutalismIcons.Telegram else NeubrutalismIcons.Download,
                            contentDescription = null,
                            tint = neoColors.textPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (link.isTelegram) t("telegram_action") else t("direct_download"),
                            fontFamily = CartoonFontFamily,
                            fontSize = 13.sp,
                            color = neoColors.textPrimary
                        )
                    }
                }
            }
        }
    }
}
