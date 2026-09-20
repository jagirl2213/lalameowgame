package com.example.game.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.game.model.DeliveryState
import com.example.game.model.MessageType
import com.example.game.model.PhoneMessage
import com.example.game.state.GameViewModel
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DustyRose
import com.example.ui.theme.DustyRoseLight
import com.example.ui.theme.HoneyYellow
import com.example.ui.theme.HoneyYellowCream
import com.example.ui.theme.HoneyYellowLight
import com.example.ui.theme.TealBrand
import com.example.ui.theme.TealBrandLight

@Composable
fun MeowPhoneDialog(
    viewModel: GameViewModel,
    onDismiss: () -> Unit
) {
    val messages by viewModel.phoneMessages.collectAsState()
    val activeJob by viewModel.activeJob.collectAsState()
    val coins by viewModel.coins.collectAsState()
    val reputation by viewModel.reputation.collectAsState()
    val challenges by viewModel.dailyChallenges.collectAsState()
    val fuel by viewModel.fuelPercent.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Chats", "Delivery", "Assets", "PawPay", "Quests")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        // Cute Phone Bezel
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .width(360.dp)
                    .fillMaxHeight(0.88f)
                    .shadow(24.dp, RoundedCornerShape(36.dp))
                    .border(4.dp, Color(0xFF1E293B), RoundedCornerShape(36.dp)),
                shape = RoundedCornerShape(36.dp),
                color = HoneyYellowCream
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // 1. Phone Top Island / Status Bar
                    PhoneStatusBar(onDismiss = onDismiss)

                    // 2. Navigation Tabs
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.White,
                        contentColor = DustyRose,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = DustyRose
                            )
                        }
                    ) {
                        tabTitles.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = {
                                    Text(
                                        title,
                                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 11.sp
                                    )
                                }
                            )
                        }
                    }

                    // 3. Tab Content
                    Box(modifier = Modifier.weight(1f).padding(12.dp)) {
                        when (selectedTab) {
                            0 -> PhoneMessagesTab(messages)
                            1 -> PhoneDeliveryTab(activeJob, viewModel)
                            2 -> PhoneAssetsTab(onOpenFullLibrary = { viewModel.showAssetLibraryDialog.value = true })
                            3 -> PhoneWalletTab(coins, reputation, fuel)
                            4 -> PhoneQuestsTab(challenges)
                        }
                    }

                    // 4. Phone Home Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 110.dp, height = 5.dp)
                                .background(Color(0xFF94A3B8), RoundedCornerShape(3.dp))
                                .clickable { onDismiss() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PhoneStatusBar(onDismiss: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("14:20 ☀️", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)

        // Dynamic Island Notch
        Box(
            modifier = Modifier
                .size(width = 80.dp, height = 18.dp)
                .background(Color.Black, RoundedCornerShape(9.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("MeowOS", color = HoneyYellowLight, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("📶 🔋94%", color = Color.White, fontSize = 11.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .background(Color(0xFF334155), CircleShape)
                    .clickable { onDismiss() },
                contentAlignment = Alignment.Center
            ) {
                Text("✕", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun PhoneMessagesTab(messages: List<PhoneMessage>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(messages) { msg ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Image(
                        painter = painterResource(id = getCustomerAsset(msg.sender)),
                        contentDescription = msg.sender,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .border(2.dp, HoneyYellow, CircleShape)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                msg.sender,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = DarkSurface
                            )
                            Text(
                                msg.timeLabel,
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        }

                        Text(
                            msg.title,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = DustyRose
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            msg.body,
                            fontSize = 11.sp,
                            color = Color(0xFF475569),
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PhoneDeliveryTab(activeJob: com.example.game.model.DeliveryJob?, viewModel: GameViewModel) {
    if (activeJob == null) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("📦", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text("No Active Delivery", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(
                "Cruising for jobs! Watch your phone for upcoming pickup requests.",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(modifier = Modifier.height(14.dp))
            Card(
                onClick = { viewModel.generateRandomDeliveryJob() },
                colors = CardDefaults.cardColors(containerColor = HoneyYellow),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    "Search For Nearby Orders",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(3.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            activeJob.category.label,
                            color = activeJob.category.badgeColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Box(
                            modifier = Modifier
                                .background(HoneyYellow, RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                "🪙 ${activeJob.rewardCoins} + ⭐ ${activeJob.rewardRep}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        activeJob.title,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = DarkSurface
                    )
                    Text(
                        activeJob.description,
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📍", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text("Pickup", fontSize = 10.sp, color = Color.Gray)
                            Text(
                                "${activeJob.pickupVenue} (${activeJob.pickupDistrict})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🏁", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text("Dropoff Customer", fontSize = 10.sp, color = Color.Gray)
                            Text(
                                "${activeJob.dropoffCustomerAvatar} ${activeJob.dropoffCustomer} (${activeJob.dropoffDistrict})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    if (activeJob.newspaperHeadline != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Text(
                                "📰 HEADLINE: \"${activeJob.newspaperHeadline}\"",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val statusText = if (activeJob.state == DeliveryState.ACCEPTED) {
                        "Status: On way to pickup point"
                    } else {
                        "Status: Package on board! Delivering to customer"
                    }
                    Text(
                        statusText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TealBrand
                    )
                }
            }
        }
    }
}

@Composable
fun PhoneWalletTab(coins: Int, reputation: Int, fuel: Float) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Balance Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = HoneyYellow),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("PawPay Wallet", color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("🪙 $coins CatCoins", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Courier Rank: ⭐ $reputation Pts", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text("Fuel: ${fuel.toInt()}%", color = Color.White, fontSize = 12.sp)
                }
            }
        }

        // Reputation Rank Progress
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Delivery Rating & Perks", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("⭐ 5.0 Star Rider Rating", color = DustyRose, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Text("• Unlocked: Fragile Curios & Newspaper Runs", fontSize = 11.sp, color = Color.Gray)
                Text("• Bonus Tips Active for quick deliveries", fontSize = 11.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun PhoneQuestsTab(challenges: List<com.example.game.state.DailyChallenge>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(challenges) { c ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (c.isCompleted) Color(0xFFECFDF5) else Color.White
                ),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            c.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (c.isCompleted) Color(0xFF059669) else DarkSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Progress: ${c.currentProgress} / ${c.targetGoal}",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(
                                if (c.isCompleted) Color(0xFF10B981) else HoneyYellow,
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            if (c.isCompleted) "CLAIMED ✨" else "🪙 +${c.rewardCoins}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PhoneAssetsTab(onOpenFullLibrary: () -> Unit) {
    val quickAssets = remember {
        listOf(
            Triple("Nami Rider", "3D Hero Courier", R.drawable.char_nami),
            Triple("Lily Pastry", "Bakery Chef", R.drawable.char_lily),
            Triple("Benny Fox", "Station Mechanic", R.drawable.char_benny),
            Triple("Momo Legal", "Corporate Cat", R.drawable.char_momo),
            Triple("Grandma Whiskers", "Cottage Matriarch", R.drawable.char_grandma),
            Triple("3D Parcel", "Lalameow Delivery Box", R.drawable.item_parcel),
            Triple("The Daily Purr", "3D Rolled Newspaper", R.drawable.item_newspaper),
            Triple("Bakery Box", "3D Warm Croissants", R.drawable.item_croissant),
            Triple("App Crest", "Official Game Logo", R.drawable.game_app_icon)
        )
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Card(
                onClick = onOpenFullLibrary,
                colors = CardDefaults.cardColors(containerColor = HoneyYellow),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().testTag("btn_open_full_asset_library")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("🎨 3D Asset & Character Library", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Tap to inspect all 3D models & characters", color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp)
                    }
                    Text("OPEN ➔", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                }
            }
        }

        items(quickAssets) { (name, subtitle, res) ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = res),
                        contentDescription = name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.5.dp, HoneyYellow, RoundedCornerShape(12.dp))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DarkSurface)
                        Text(subtitle, fontSize = 11.sp, color = Color(0xFF64748B))
                    }
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("3D Asset", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = TealBrand)
                    }
                }
            }
        }
    }
}

