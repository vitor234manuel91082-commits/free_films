package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.MediaItem
import com.example.data.model.MediaType
import com.example.data.model.UserProfile
import com.example.ui.CineViewModel
import com.example.ui.theme.*

@Composable
fun CineHome(
    viewModel: CineViewModel,
    modifier: Modifier = Modifier
) {
    val activeProfile by viewModel.activeProfile.collectAsState()
    val selectedMedia by viewModel.selectedMedia.collectAsState()
    
    val profile = activeProfile ?: return

    var currentTab by remember { mutableStateOf("inicio") } // "inicio", "pesquisa", "lista", "perfil"

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(BlackBackground),
        bottomBar = {
            CineBottomNavigation(
                activeTab = currentTab,
                onTabSelected = { currentTab = it }
            )
        },
        containerColor = BlackBackground
    ) { innerPadding ->
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Screen switching animations
            Crossfade(
                targetState = currentTab,
                label = "tab_switch"
            ) { tab ->
                when (tab) {
                    "inicio" -> InicioTab(viewModel)
                    "pesquisa" -> PesquisaTab(viewModel)
                    "lista" -> ListaTab(viewModel)
                    "perfil" -> PerfilTab(viewModel, profile)
                }
            }

            // Shared popup details sheet overlay
            selectedMedia?.let { media ->
                MediaDetailsDialog(
                    media = media,
                    viewModel = viewModel,
                    onDismiss = { viewModel.removeMediaDetails() }
                )
            }
        }
    }
}

// --- BOTTOM NAVIGATION COMPONENT ---
@Composable
fun CineBottomNavigation(
    activeTab: String,
    onTabSelected: (String) -> Unit
) {
    NavigationBar(
        containerColor = Color.Black.copy(alpha = 0.95f),
        contentColor = Color.White,
        tonalElevation = 8.dp,
        modifier = Modifier
            .border(width = 0.5.dp, color = Color.White.copy(alpha = 0.1f))
            .navigationBarsPadding()
    ) {
        NavigationBarItem(
            selected = activeTab == "inicio",
            onClick = { onTabSelected("inicio") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = OrangePrimary,
                selectedTextColor = OrangePrimary,
                indicatorColor = Color.White.copy(alpha = 0.1f),
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary
            ),
            icon = { Icon(Icons.Default.Home, contentDescription = "Início") },
            label = { Text("Início", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            modifier = Modifier.testTag("nav_inicio")
        )
        
        NavigationBarItem(
            selected = activeTab == "pesquisa",
            onClick = { onTabSelected("pesquisa") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = OrangePrimary,
                selectedTextColor = OrangePrimary,
                indicatorColor = Color.White.copy(alpha = 0.1f),
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary
            ),
            icon = { Icon(Icons.Default.Search, contentDescription = "Pesquisar") },
            label = { Text("Pesquisar", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            modifier = Modifier.testTag("nav_search")
        )

        NavigationBarItem(
            selected = activeTab == "lista",
            onClick = { onTabSelected("lista") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = OrangePrimary,
                selectedTextColor = OrangePrimary,
                indicatorColor = Color.White.copy(alpha = 0.1f),
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary
            ),
            icon = { Icon(Icons.Default.Bookmark, contentDescription = "Minha Lista") },
            label = { Text("Minha Lista", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            modifier = Modifier.testTag("nav_watchlist")
        )

        NavigationBarItem(
            selected = activeTab == "perfil",
            onClick = { onTabSelected("perfil") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = OrangePrimary,
                selectedTextColor = OrangePrimary,
                indicatorColor = Color.White.copy(alpha = 0.1f),
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary
            ),
            icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
            label = { Text("Perfil", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            modifier = Modifier.testTag("nav_profile")
        )
    }
}

// --- 1. INÍCIO TAB (HOME & GRID CATEGORIES) ---
@Composable
fun InicioTab(viewModel: CineViewModel) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackBackground)
    ) {
        // High-Quality Featured Content Carousel
        item {
            FeaturedCarousel(viewModel = viewModel)
        }

        // Horizontal Category Row: Animes em 4K
        item {
            CineCategoryRow(
                title = "Coleção de Animes 4K",
                items = viewModel.getAnimes(),
                onItemClick = { viewModel.showMediaDetails(it) }
            )
        }

        // Horizontal Category Row: Filmes UHD
        item {
            CineCategoryRow(
                title = "Filmes em Destaque (4K HDR)",
                items = viewModel.getMovies(),
                onItemClick = { viewModel.showMediaDetails(it) }
            )
        }

        // Horizontal Category Row: Séries Completas
        item {
            CineCategoryRow(
                title = "Séries em Alta Qualidade",
                items = viewModel.getSeries(),
                onItemClick = { viewModel.showMediaDetails(it) }
            )
        }
        
        // Spacing bottom padding
        item { Spacer(modifier = Modifier.height(30.dp)) }
    }
}

@Composable
fun FeaturedCarousel(viewModel: CineViewModel) {
    val featuredItems = remember {
        viewModel.getTrending().filter {
            it.type == MediaType.MOVIE || it.type == MediaType.ANIME
        }
    }
    val itemsToShow = remember(featuredItems) {
        featuredItems.ifEmpty { viewModel.getMediaItems().take(5) }
    }

    if (itemsToShow.isEmpty()) return

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { itemsToShow.size }
    )

    // Auto-scroll loop
    val isUserInteracting = remember { mutableStateOf(false) }
    LaunchedEffect(pagerState, itemsToShow.size) {
        if (itemsToShow.isEmpty()) return@LaunchedEffect
        while (true) {
            delay(5000) // Rotate every 5 seconds
            if (!isUserInteracting.value) {
                val nextPage = (pagerState.currentPage + 1) % itemsToShow.size
                pagerState.animateScrollToPage(nextPage)
            }
        }
    }

    // Temporary pause on interaction
    val isScrollInProgress = pagerState.isScrollInProgress
    LaunchedEffect(isScrollInProgress) {
        if (isScrollInProgress) {
            isUserInteracting.value = true
            delay(10000) // Pause auto-scrolling for 10s of inactivity
            isUserInteracting.value = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(460.dp)
            .testTag("featured_carousel")
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val media = itemsToShow[page]
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { viewModel.showMediaDetails(media) }
            ) {
                // Background Poster Image with Full-bleed Layout
                CoilImage(
                    url = media.featuredPosterUrl,
                    contentDescription = "Slide cover for ${media.title}",
                    modifier = Modifier.fillMaxSize()
                )

                // Cinematic Multi-layered Gradients (Black and Orange color accents)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.4f),
                                    Color.Transparent,
                                    BlackBackground.copy(alpha = 0.7f),
                                    BlackBackground
                                )
                            )
                        )
                )

                // Subtle orange bottom/mid shadow backdrop glow
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    OrangePrimary.copy(alpha = 0.12f),
                                    Color.Transparent
                                ),
                                radius = 400f
                            )
                        )
                )

                // Floating Overlay/Content Card inside Carousel Slide
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 24.dp, vertical = 24.dp)
                ) {
                    // Category Badge labels (e.g. EM ALTA, MOVIE/ANIME, UHD 4K)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(bottom = 10.dp)
                    ) {
                        // "EM ALTA" Trending label in black/orange
                        Box(
                            modifier = Modifier
                                .background(OrangePrimary, RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "EM ALTA",
                                fontSize = 10.sp,
                                color = Color.Black,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Media Type Label
                        Box(
                            modifier = Modifier
                                .border(1.dp, OrangeSecondary, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 1.5.dp)
                        ) {
                            Text(
                                text = if (media.type == MediaType.ANIME) "ANIME" else "FILME",
                                fontSize = 10.sp,
                                color = OrangeSecondary,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Quality Spec Label
                        Box(
                            modifier = Modifier
                                .border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 1.5.dp)
                        ) {
                            Text(
                                text = "4K HDR",
                                fontSize = 10.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Bold Responsive Title
                    Text(
                        text = media.title,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        color = Color.White,
                        lineHeight = 34.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Secondary Subtext line
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Text(
                            text = media.releaseYear.toString(),
                            fontSize = 12.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "•", color = TextSecondary.copy(alpha = 0.6f), fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = media.rating,
                            fontSize = 11.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "•", color = TextSecondary.copy(alpha = 0.6f), fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = media.genres.take(2).joinToString(" • "),
                            fontSize = 12.sp,
                            color = OrangeSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Interactive slide actions (Assistir & Minha Lista)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 340.dp)
                    ) {
                        // Play Button
                        Button(
                            onClick = { viewModel.startPlaying(media) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = OrangePrimary,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1.1f)
                                .height(44.dp)
                                .testTag("carousel_play_${media.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Assistir",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.Black
                            )
                        }

                        // My Watchlist Button
                        val isInList = viewModel.isInWatchlist(media.id)
                        OutlinedButton(
                            onClick = { viewModel.toggleWatchlist(media.id) },
                            border = BorderStroke(1.5.dp, if (isInList) OrangePrimary else Color.White),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(0.9f)
                                .height(44.dp)
                                .testTag("carousel_add_${media.id}")
                        ) {
                            Icon(
                                imageVector = if (isInList) Icons.Default.Check else Icons.Default.Add,
                                contentDescription = null,
                                tint = if (isInList) OrangePrimary else Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isInList) "Na Lista" else "Lista",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isInList) OrangePrimary else Color.White
                            )
                        }
                    }
                }
            }
        }

        // Active indicators using Black & Orange
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
        ) {
            itemsToShow.forEachIndexed { index, _ ->
                val isSelected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .height(6.dp)
                        .width(if (isSelected) 18.dp else 6.dp)
                        .background(
                            color = if (isSelected) OrangePrimary else Color.White.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(3.dp)
                        )
                )
            }
        }

        // Modern side controller buttons
        val scope = rememberCoroutineScope()
        IconButton(
            onClick = {
                scope.launch {
                    val prevPage = if (pagerState.currentPage > 0) pagerState.currentPage - 1 else itemsToShow.size - 1
                    pagerState.animateScrollToPage(prevPage)
                }
            },
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 8.dp)
                .size(36.dp)
                .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                .border(0.5.dp, Color.White.copy(alpha = 0.15f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowLeft,
                contentDescription = "Anterior",
                tint = OrangeSecondary,
                modifier = Modifier.size(22.dp)
            )
        }

        IconButton(
            onClick = {
                scope.launch {
                    val nextPage = (pagerState.currentPage + 1) % itemsToShow.size
                    pagerState.animateScrollToPage(nextPage)
                }
            },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 8.dp)
                .size(36.dp)
                .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                .border(0.5.dp, Color.White.copy(alpha = 0.15f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Próximo",
                tint = OrangeSecondary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
fun BillboardHeader(media: MediaItem, viewModel: CineViewModel) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(440.dp)
            .testTag("billboard_header")
    ) {
        // Hero Image Cover
        CoilImage(
            url = media.featuredPosterUrl,
            contentDescription = "Billboard cover",
            modifier = Modifier.fillMaxSize()
        )

        // Dark Gradients overlap for cinematic vibe
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.3f),
                            Color.Transparent,
                            BlackBackground.copy(alpha = 0.8f),
                            BlackBackground
                        )
                    )
                )
        )

        // Floating info labels inside banner
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            
            Text(
                text = media.title,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                color = Color.White,
                lineHeight = 34.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Streaming properties
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(OrangePrimary, RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "4K HDR",
                        fontSize = 10.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = media.rating,
                    fontSize = 12.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = media.genres.take(2).joinToString("  •  "),
                    fontSize = 12.sp,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Quick Play + Watchlist panel
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.widthIn(max = 350.dp)
            ) {
                
                // Play Action
                Button(
                    onClick = { viewModel.startPlaying(media) },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("billboard_play_button")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Assistir", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                // Watchlist action
                val isInList = viewModel.isInWatchlist(media.id)
                OutlinedButton(
                    onClick = { viewModel.toggleWatchlist(media.id) },
                    border = BorderStroke(1.5.dp, if (isInList) OrangePrimary else Color.White),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("billboard_add_list_button")
                ) {
                    Icon(
                        imageVector = if (isInList) Icons.Default.Check else Icons.Default.Add,
                        contentDescription = null,
                        tint = if (isInList) OrangePrimary else Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isInList) "Na Lista" else "Minha Lista",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isInList) OrangePrimary else Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun CineCategoryRow(
    title: String,
    items: List<MediaItem>,
    onItemClick: (MediaItem) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(items) { media ->
                CineCard(media = media, onClick = { onItemClick(media) })
            }
        }
    }
}

@Composable
fun CineCard(
    media: MediaItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(115.dp)
            .clickable { onClick() }
            .testTag("media_card_${media.id}")
    ) {
        // Poster Cover Image
        Box(
            modifier = Modifier
                .width(115.dp)
                .height(170.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(BlackSurface)
                .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
        ) {
            CoilImage(
                url = media.imageUrl,
                contentDescription = media.title,
                modifier = Modifier.fillMaxSize()
            )

            // Age Rating Badge on Poster corner
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
                Text(
                    text = media.rating,
                    fontSize = 9.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(6.dp))
        
        Text(
            text = media.title,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// --- 2. PESQUISA TAB ---
@Composable
fun PesquisaTab(viewModel: CineViewModel) {
    val query by viewModel.searchQuery.collectAsState()
    val results by viewModel.searchResults.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackBackground)
            .padding(16.dp)
    ) {
        // Subtitle header
        Text(
            text = "Pesquisar Filmes & Animes",
            color = OrangePrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Custom Search Input Box
        OutlinedTextField(
            value = query,
            onValueChange = { viewModel.updateQuery(it) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = OrangePrimary) },
            trailingIcon = {
                if (query.isNotBlank()) {
                    IconButton(onClick = { viewModel.updateQuery("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Limpar", tint = Color.White)
                    }
                }
            },
            placeholder = { Text("Procure por título, gênero ou anime...", color = TextSecondary) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = OrangePrimary,
                unfocusedBorderColor = TextSecondary,
                focusedLabelColor = OrangePrimary,
                cursorColor = OrangePrimary,
                focusedContainerColor = BlackSurface,
                unfocusedContainerColor = BlackSurface
            ),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .testTag("search_bar_input")
        )

        if (query.isBlank()) {
            // Suggestion list
            Text(
                text = "Recomendações Populares",
                color = TextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(viewModel.getPopular()) { media ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(BlackSurface)
                            .clickable { viewModel.showMediaDetails(media) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CoilImage(
                            url = media.imageUrl,
                            contentDescription = media.title,
                            modifier = Modifier
                                .width(50.dp)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = media.title,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                    }
                }
            }
        } else {
            // Search Results Grid
            if (results.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Nenhum anime ou filme encontrado.",
                            color = TextSecondary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(results) { media ->
                        CineCard(media = media, onClick = { viewModel.showMediaDetails(media) })
                    }
                }
            }
        }
    }
}

// --- 3. MINHA LISTA TAB ---
@Composable
fun ListaTab(viewModel: CineViewModel) {
    val watchlistEntries by viewModel.watchlist.collectAsState()
    val allMedia = viewModel.getMediaItems()

    // Match database items with memory dataset
    val savedItems = remember(watchlistEntries) {
        watchlistEntries.mapNotNull { entry ->
            allMedia.firstOrNull { it.id == entry.mediaId }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackBackground)
            .padding(16.dp)
    ) {
        Text(
            text = "Minha Lista",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        if (savedItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.BookmarkBorder,
                        contentDescription = "Lista Vazia",
                        tint = OrangePrimary.copy(alpha = 0.6f),
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Sua lista está vazia",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Navegue pelo catálogo e adicione animes e filmes para assistir mais tarde.",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(savedItems) { media ->
                    CineCard(media = media, onClick = { viewModel.showMediaDetails(media) })
                }
            }
        }
    }
}

// --- 4. PERFIL TAB (USER STATS & HISTORIC LOGS) ---
@Composable
fun PerfilTab(viewModel: CineViewModel, profile: UserProfile) {
    val watchHistory by viewModel.watchHistory.collectAsState()
    val allMedia = viewModel.getMediaItems()

    val matchedHistory = remember(watchHistory) {
        watchHistory.mapNotNull { entry ->
            val media = allMedia.firstOrNull { it.id == entry.mediaId }
            if (media != null) {
                Pair(media, entry)
            } else {
                null
            }
        }
    }

    // Genre Distribution tracking simulation
    val genreCounts = remember(matchedHistory) {
        val counts = mutableMapOf<String, Int>()
        matchedHistory.forEach { (media, _) ->
            media.genres.forEach { genre ->
                counts[genre] = (counts[genre] ?: 0) + 1
            }
        }
        counts.toList().sortedByDescending { it.second }.take(3)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackBackground)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile Info Header Card
        item {
            val avatarColor = ProfileColors.getOrElse(profile.avatarIndex) { OrangePrimary }
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(avatarColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = profile.name.take(1).uppercase(),
                        color = Color.White,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = profile.name,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Membro desde 2026",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        // Action Options: Logout/Switch profiles
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                OutlinedButton(
                    onClick = { viewModel.logoutProfile() },
                    border = BorderStroke(1.dp, OrangePrimary),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = OrangePrimary),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.testTag("switch_profile_button")
                ) {
                    Icon(Icons.Default.SwitchAccount, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Trocar de Perfil", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Recent Watch History Logs (Resume Play)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Histórico de Visualização",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (matchedHistory.isNotEmpty()) {
                        TextButton(onClick = { viewModel.clearWatchHistory() }) {
                            Text("Limpar", color = OrangePrimary, fontSize = 12.sp)
                        }
                    }
                }

                if (matchedHistory.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(BlackSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Nenhum histórico registrado ainda",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        items(matchedHistory) { (media, record) ->
                            Column(
                                modifier = Modifier
                                    .width(140.dp)
                                    .clickable { viewModel.startPlaying(media) }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(84.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                ) {
                                    CoilImage(
                                        url = media.featuredPosterUrl,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize()
                                    )

                                    // Resume play overlay button
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.4f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Continuar",
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    // Progress bar
                                    val pct = record.progressSeconds.toFloat() / record.durationSeconds.toFloat()
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .fillMaxWidth()
                                            .height(4.dp)
                                            .background(Color.White.copy(alpha = 0.3f))
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .fillMaxWidth(pct)
                                                .background(OrangePrimary)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = media.title,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "35% assistido",
                                    color = TextSecondary,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Preferences Genres statistics
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Text(
                    text = "Suas Preferências de Áudio/Gênero",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = BlackSurface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Reproduções em 4K HDR e Áudio Espacial Atmos configurados como padrão do perfil.",
                            color = OrangeSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Text(
                            text = "Seus Gêneros Mais Assistidos:",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        if (genreCounts.isEmpty()) {
                            // Default mock stats before user plays anything
                            listOf("Ação" to 0.5f, "Ficção Científica" to 0.3f, "Animação" to 0.2f).forEach { (genre, pct) ->
                                GenreProgressRow(genre = genre, percentage = pct)
                            }
                        } else {
                            val total = genreCounts.sumOf { it.second }.toFloat()
                            genreCounts.forEach { (genre, count) ->
                                GenreProgressRow(genre = genre, percentage = count / total)
                            }
                        }
                    }
                }
            }
        }

        // --- SEÇÃO BANCO DE DADOS DILIGENTE (GITHUB JSON SYNC) ---
        item {
            val dbUrl by viewModel.dbUrl.collectAsState()
            val syncStatus by viewModel.syncStatus.collectAsState()
            var editUrl by remember { mutableStateOf(dbUrl) }
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Banco de Dados Online",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Connection Status Badge
                    val badgeColor = when (syncStatus) {
                        "Sincronizado" -> Color(0xFF4CAF50) // Green
                        "Conectando..." -> Color(0xFFFFC107) // Yellow
                        "Erro" -> Color(0xFFF44336) // Red
                        else -> Color(0xFF9E9E9E) // Grey
                    }
                    
                    Box(
                        modifier = Modifier
                            .background(badgeColor, RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = syncStatus.uppercase(),
                            fontSize = 10.sp,
                            color = if (syncStatus == "Conectando...") Color.Black else Color.White,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = BlackSurface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "O Free Films está conectado a uma base de dados externa no GitHub. É possível hospedar seu próprio arquivo JSON para trocar todos os filmes, animes e séries em tempo real!",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        OutlinedTextField(
                            value = editUrl,
                            onValueChange = { editUrl = it },
                            label = { Text("URL da Base de Dados (JSON)", color = OrangeSecondary) },
                            textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = OrangePrimary,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                                focusedLabelColor = OrangePrimary,
                                unfocusedLabelColor = TextSecondary,
                                cursorColor = OrangePrimary
                            ),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { viewModel.refreshRemoteDatabase(editUrl) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = OrangePrimary,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Sincronizar", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                }
                            }
                            
                            OutlinedButton(
                                onClick = {
                                    editUrl = com.example.data.api.CineApiService.DEFAULT_DATABASE_URL
                                    viewModel.refreshRemoteDatabase(editUrl)
                                },
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .weight(0.8f)
                                    .height(40.dp)
                            ) {
                                Text("Padrão", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
        
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
fun GenreProgressRow(genre: String, percentage: Float) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = genre, color = Color.White, fontSize = 11.sp)
            Text(text = "${(percentage * 100).toInt()}%", color = OrangeSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.1f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(percentage)
                    .background(OrangePrimary)
            )
        }
    }
}


// --- SHARED DETAIL CARD POPUP WINDOW ---
@Composable
fun MediaDetailsDialog(
    media: MediaItem,
    viewModel: CineViewModel,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = false) { /* prevent click propagation */ }
                .border(width = 1.dp, color = OrangePrimary.copy(alpha = 0.3f), shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .testTag("media_detail_sheet"),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            colors = CardDefaults.cardColors(containerColor = BlackSurface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                // Large modal photo header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    CoilImage(
                        url = media.featuredPosterUrl,
                        contentDescription = "Details Banner",
                        modifier = Modifier.fillMaxSize()
                    )

                    // Top Gradients
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Black.copy(alpha = 0.5f), Color.Transparent, BlackSurface)
                                )
                            )
                    )

                    IconButton(
                        onClick = { onDismiss() },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar", tint = Color.White)
                    }
                }

                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text(
                        text = media.title,
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    // Meta Row
                    Row(
                        modifier = Modifier.padding(bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "${media.releaseYear}", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = media.rating,
                            color = OrangeSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .border(1.dp, OrangeSecondary.copy(alpha = 0.6f), RoundedCornerShape(2.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = media.duration, color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(
                            modifier = Modifier
                                .background(OrangePrimary.copy(alpha = 0.2f), RoundedCornerShape(3.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(text = "4K HDR", color = OrangePrimary, fontSize = 10.sp, fontWeight = FontWeight.Black)
                        }
                    }

                    // Watch Actions Panel
                    Row(
                        modifier = Modifier.padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Play direct trigger
                        Button(
                            onClick = { viewModel.startPlaying(media) },
                            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Assistir em 4K", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        // Watchlist toggle
                        val isSaved = viewModel.isInWatchlist(media.id)
                        IconButton(
                            onClick = { viewModel.toggleWatchlist(media.id) },
                            modifier = Modifier
                                .size(42.dp)
                                .border(1.dp, if (isSaved) OrangePrimary else Color.White.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                        ) {
                            Icon(
                                imageVector = if (isSaved) Icons.Default.Check else Icons.Default.Add,
                                contentDescription = "Minha Lista",
                                tint = if (isSaved) OrangePrimary else Color.White
                            )
                        }
                    }

                    // Synopsis Description
                    Text(
                        text = media.description,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Audio and subtitles listing info
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(bottom = 12.dp))
                    
                    Text(
                        text = "Áudio disponível: " + media.audioLanguages.joinToString(", "),
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "Legendas disponíveis: " + media.subtitles.joinToString(", "),
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// --- COIL IMAGE LOADER WITH SHIMMER PLACEHOLDERS ---
@Composable
fun CoilImage(
    url: String,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(url)
            .crossfade(true)
            .build(),
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        placeholder = null, // Using manual gradient container background below
        error = null,
        modifier = modifier.drawBehind {
            // Cool visual loading accent in case image fails or loads slowly
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF1B1B22), Color(0xFF101014))
                )
            )
        }
    )
}
