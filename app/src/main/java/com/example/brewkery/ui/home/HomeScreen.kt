package com.example.brewkery.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeliveryDining
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.brewkery.data.local.OrderEntity
import com.example.brewkery.data.model.CategoryDto
import com.example.brewkery.data.model.CoffeeItemDto
import com.example.brewkery.ui.components.BrewkeryLoadingIndicator
import com.example.brewkery.ui.components.BrewkeryTopBar
import com.example.brewkery.ui.components.NoInternetView
import com.example.brewkery.ui.components.SomethingWentWrongView
import com.example.brewkery.util.bounceClick
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.delay
import java.util.Locale
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.wrapContentHeight
import com.example.brewkery.ui.components.ProfileSideSheet

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onItemClick: (Int) -> Unit,
    onCartClick: () -> Unit,
    onOrderStatusClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var isProfileSheetOpen by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            BrewkeryTopBar(onProfileClick = { isProfileSheetOpen = true })
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCartClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.bounceClick(onClick = onCartClick)
            ) {
                BadgedBox(
                    badge = {
                        if (uiState.totalCartCount > 0) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.secondary,
                                contentColor = MaterialTheme.colorScheme.onSecondary
                            ) {
                                Text(
                                    text = uiState.totalCartCount.toString(),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ShoppingBag,
                        contentDescription = "Cart",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (uiState.screenState) {
                ScreenState.LOADING -> {
                    BrewkeryLoadingIndicator()
                }

                ScreenState.NO_INTERNET -> {
                    NoInternetView(onRetry = { viewModel.loadCatalog() })
                }

                ScreenState.FAIL -> {
                    SomethingWentWrongView(onRetry = { viewModel.loadCatalog() })
                }

                ScreenState.SUCCESS -> {
                    HomeContent(
                        uiState = uiState,
                        onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                        onCategorySelect = { viewModel.selectCategory(it) },
                        onItemClick = onItemClick,
                        onActiveOrderClick = onOrderStatusClick
                    )
                }
            }

            ProfileSideSheet(
                isOpen = isProfileSheetOpen,
                orders = uiState.allOrders,
                onDismiss = { isProfileSheetOpen = false },
                onOrderClick = { ticketId ->
                    isProfileSheetOpen = false
                    onOrderStatusClick(ticketId)
                }
            )
        }
    }
}

@Composable
fun HomeContent(
    uiState: HomeUiState,
    onSearchQueryChange: (String) -> Unit,
    onCategorySelect: (String) -> Unit,
    onItemClick: (Int) -> Unit,
    onActiveOrderClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        if (uiState.activeOrders.isNotEmpty()) {
            ActiveOrdersBannerPager(
                orders = uiState.activeOrders,
                onOrderClick = onActiveOrderClick
            )
        } else {
            StoreAnnouncementBanner(
                estimatedTime = uiState.meta?.estimatedDeliveryTime ?: "20 - 30 mins",
                deliveryFee = String.format(Locale.US, "$%.2f", uiState.meta?.deliveryFee ?: 2.50)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp)),
            placeholder = { Text("Search coffee, drinks, bakery...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        CategoryChipsRow(
            categories = uiState.categories,
            selectedCategoryId = uiState.selectedCategoryId,
            onCategorySelect = onCategorySelect
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.filteredItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No coffee or items found",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(bottom = 80.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(uiState.filteredItems, key = { it.id }) { item ->
                    CoffeeItemCard(
                        item = item,
                        onClick = { onItemClick(item.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun StoreAnnouncementBanner(
    estimatedTime: String,
    deliveryFee: String
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Welcome to Brewkery ☕",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Handcrafted coffee & artisan oven bakes delivered to your door.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Timer,
                        contentDescription = "Time",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = estimatedTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(
                        imageVector = Icons.Rounded.DeliveryDining,
                        contentDescription = "Fee",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$deliveryFee fee",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ActiveOrdersBannerPager(
    orders: List<OrderEntity>,
    onOrderClick: (String) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { orders.size })

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Active Orders (${orders.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            if (orders.size > 1) {
                Text(
                    text = "${pagerState.currentPage + 1} of ${orders.size}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            pageSpacing = 12.dp,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            val order = orders[page]
            ActiveOrderBannerCard(
                order = order,
                onClick = { onOrderClick(order.ticketId) }
            )
        }

        if (orders.size > 1) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(orders.size) { iteration ->
                    val isSelected = pagerState.currentPage == iteration
                    val color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    val width = if (isSelected) 18.dp else 8.dp
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .height(8.dp)
                            .width(width)
                            .clip(CircleShape)
                            .background(color)
                    )
                }
            }
        }
    }
}

@Composable
fun ActiveOrderBannerCard(
    order: OrderEntity,
    onClick: () -> Unit
) {
    var remainingMinutes by remember { mutableStateOf(0) }
    var currentStatus by remember { mutableStateOf(order.status) }

    LaunchedEffect(order.timestamp, order.deliveryTimeMinutes, order.status) {
        val deliveryTime = order.timestamp + (order.deliveryTimeMinutes * 60000L)
        val now = System.currentTimeMillis()

        if (now >= deliveryTime || order.status == "DELIVERED") {
            remainingMinutes = 0
            currentStatus = "DELIVERED"
        } else {
            val remainingMillis = deliveryTime - now
            val remMins = ((remainingMillis / 60000L) + 1).toInt().coerceAtLeast(1)
            remainingMinutes = remMins
            currentStatus = if (remMins > 5) "PREPARING" else "OUT FOR DELIVERY"

            while (System.currentTimeMillis() < deliveryTime) {
                delay(5000L)
                val currNow = System.currentTimeMillis()
                val rem = (((deliveryTime - currNow) / 60000L) + 1).toInt().coerceAtLeast(0)
                remainingMinutes = rem
                if (rem <= 0) {
                    currentStatus = "DELIVERED"
                    break
                } else if (rem <= 5) {
                    currentStatus = "OUT FOR DELIVERY"
                }
            }
        }
    }

    val itemsSummaryText = remember(order.itemsSummaryJson) {
        try {
            val type = object : TypeToken<List<Map<String, Any>>>() {}.type
            val rawList: List<Map<String, Any>> = Gson().fromJson(order.itemsSummaryJson, type)
            rawList.joinToString(", ") { map ->
                val name = map["name"] as? String ?: ""
                val qty = (map["quantity"] as? Number)?.toInt() ?: 1
                "${qty}x $name"
            }
        } catch (e: Exception) {
            ""
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.LocalShipping,
                contentDescription = "Tracking",
                tint = MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Ticket ${order.ticketId}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = currentStatus,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (currentStatus == "DELIVERED")
                        "Order Delivered • Enjoy your coffee! (hides in 2 min)"
                    else
                        "Arriving in ~$remainingMinutes mins • ${order.itemCount} items",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.9f)
                )

                if (itemsSummaryText.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Items: $itemsSummaryText",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Normal,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.85f)
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryChipsRow(
    categories: List<CategoryDto>,
    selectedCategoryId: String,
    onCategorySelect: (String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 0.dp)
    ) {
        item {
            FilterChip(
                selected = selectedCategoryId == "ALL",
                onClick = { onCategorySelect("ALL") },
                label = { Text("All Items") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.bounceClick(onClick = { onCategorySelect("ALL") })
            )
        }

        items(categories, key = { it.id }) { cat ->
            val isSelected = selectedCategoryId == cat.id
            FilterChip(
                selected = isSelected,
                onClick = { onCategorySelect(cat.id) },
                label = { Text("${cat.icon} ${cat.name}") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.bounceClick(onClick = { onCategorySelect(cat.id) })
            )
        }
    }
}

@Composable
fun CoffeeItemCard(
    item: CoffeeItemDto,
    onClick: () -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                if (!item.badge.isNullOrBlank()) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondary,
                        shape = RoundedCornerShape(bottomEnd = 8.dp),
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Text(
                            text = item.badge,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = "Rating",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = item.rating.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = String.format(Locale.US, "$%.2f", item.basePrice),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
