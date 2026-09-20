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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.game.state.GameViewModel
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DustyRose
import com.example.ui.theme.DustyRoseLight
import com.example.ui.theme.HoneyYellow
import com.example.ui.theme.HoneyYellowCream
import com.example.ui.theme.HoneyYellowLight
import com.example.ui.theme.TealBrand

data class AssetItem(
    val id: String,
    val name: String,
    val subtitle: String,
    val category: String,
    val drawableRes: Int,
    val description: String,
    val modelType: String,
    val stats: List<Pair<String, String>>
)

@Composable
fun AssetLibraryDialog(
    viewModel: GameViewModel,
    onDismiss: () -> Unit
) {
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }
    val categories = listOf("All Assets", "Characters", "3D Deliveries", "World & Branding")

    val allAssets = remember {
        listOf(
            AssetItem(
                id = "char_nami",
                name = "Nami the Courier",
                subtitle = "Playable Lead Character",
                category = "Characters",
                drawableRes = R.drawable.char_nami,
                description = "Energetic ginger tabby courier with aerodynamic ears, yellow safety helmet with tinted speed goggles, and official Lalameow delivery uniform.",
                modelType = "3D Hero Character & Scooter Rig",
                stats = listOf("Agility" to "★★★★★", "Speed" to "45 km/h", "Special" to "Paws Drift")
            ),
            AssetItem(
                id = "char_lily",
                name = "Lily the Baker",
                subtitle = "Artisan Pastry Chef",
                category = "Characters",
                drawableRes = R.drawable.char_lily,
                description = "Master cat pastry chef operating Lily's Artisan Bakery. Regularly orders deliveries of fresh warm strawberry croissants and morning bread.",
                modelType = "3D NPC Customer & Shopkeeper",
                stats = listOf("Venue" to "Lily's Bakery", "District" to "Downpaws Market", "Favorite" to "Croissants")
            ),
            AssetItem(
                id = "char_benny",
                name = "Benny the Mechanic",
                subtitle = "Shell-Paws Station Operator",
                category = "Characters",
                drawableRes = R.drawable.char_benny,
                description = "Friendly fox mechanic stationed at the Shell-Paws gas depot. Refuels scooters, tunes carburetors, and shares shortcut tips.",
                modelType = "3D NPC Fuel Attendant",
                stats = listOf("Venue" to "Shell-Paws East", "District" to "Downpaws Market", "Service" to "Petro & Tune-Up")
            ),
            AssetItem(
                id = "char_momo",
                name = "Momo Esq.",
                subtitle = "Corporate Legal Consultant",
                category = "Characters",
                drawableRes = R.drawable.char_momo,
                description = "Dapper Siamese legal cat in sharp collar and tie. Handles VIP courier contracts and urgent document transfers.",
                modelType = "3D NPC Customer",
                stats = listOf("Venue" to "Momo Consulting", "District" to "Downpaws Market", "Specialty" to "Express Contracts")
            ),
            AssetItem(
                id = "char_grandma",
                name = "Grandma Whiskers",
                subtitle = "Cottage Matriarch",
                category = "Characters",
                drawableRes = R.drawable.char_grandma,
                description = "Beloved sweet grandmother cat residing in the sunny market cottage. Loves morning parcel deliveries of rainbow yarn and tea.",
                modelType = "3D NPC Customer",
                stats = listOf("Venue" to "Whiskers Cottage", "District" to "Downpaws Market", "Reward" to "Generous Tips")
            ),
            AssetItem(
                id = "char_pigeon",
                name = "Mr. Pickles",
                subtitle = "City Traffic Scout",
                category = "Characters",
                drawableRes = R.drawable.char_pigeon,
                description = "Urban carrier pigeon with tiny pilot goggles. Perches on streetlamps to monitor traffic jams, road obstacles, and weather changes.",
                modelType = "3D Ambient NPC",
                stats = listOf("Patrol" to "Skyline & Lamps", "Speed" to "Flapping Fast", "Trait" to "Sees All")
            ),
            AssetItem(
                id = "item_parcel",
                name = "Lalameow Delivery Parcel",
                subtitle = "3D Standard Package",
                category = "3D Deliveries",
                drawableRes = R.drawable.item_parcel,
                description = "Official craft cardboard parcel box sealed with Lalameow yellow paw-print tape, fragile postage stamp, and barcode label. Strapped securely to your scooter rack.",
                modelType = "3D Procedural Mesh & Holographic Beacon",
                stats = listOf("Weight" to "1.5 kg", "Durability" to "High", "Tape" to "Paw-Tested")
            ),
            AssetItem(
                id = "item_newspaper",
                name = "The Daily Purr",
                subtitle = "3D Morning Newspaper Roll",
                category = "3D Deliveries",
                drawableRes = R.drawable.item_newspaper,
                description = "Crisp rolled daily morning edition newspaper with printed headlines and royal blue twine ribbon. Delivered straight to neighborhood doorsteps.",
                modelType = "3D Cylindrical Mesh & Newsstand Rack",
                stats = listOf("Edition" to "Morning Press", "Headline" to "Nap Time Law", "Pages" to "16 Meows")
            ),
            AssetItem(
                id = "item_croissant",
                name = "Artisan Pastry Box",
                subtitle = "3D Bakery Delivery Package",
                category = "3D Deliveries",
                drawableRes = R.drawable.item_croissant,
                description = "Pastel insulated pastry box containing freshly baked butter croissants and sweet tarts from Lily's oven. Keep warm during transit!",
                modelType = "3D Insulated Bakery Model",
                stats = listOf("Temp" to "Warm & Flaky", "Bakery" to "Lily's Artisan", "Aroma" to "Butter 100%")
            ),
            AssetItem(
                id = "game_app_icon",
                name = "Lalameow Rider Crest",
                subtitle = "Official App Icon & Emblem",
                category = "World & Branding",
                drawableRes = R.drawable.game_app_icon,
                description = "Iconic golden brand seal of Lalameow Rider featuring the cheerful courier cat face, courier goggles, winged helmet crest, and checkered racing laurels.",
                modelType = "Adaptive Vector & Raster Icon",
                stats = listOf("Resolution" to "Ultra HD", "Colors" to "Honey Gold / Slate", "Version" to "2.0")
            )
        )
    }

    val filteredAssets = remember(selectedCategoryIndex) {
        if (selectedCategoryIndex == 0) allAssets
        else allAssets.filter { it.category == categories[selectedCategoryIndex] }
    }

    var selectedAsset by remember { androidx.compose.runtime.mutableStateOf(filteredAssets.first()) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .width(720.dp)
                    .fillMaxHeight(0.92f)
                    .shadow(24.dp, RoundedCornerShape(28.dp))
                    .border(3.dp, HoneyYellow, RoundedCornerShape(28.dp)),
                shape = RoundedCornerShape(28.dp),
                color = HoneyYellowCream
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkSurface)
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🎨", fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    "Lalameow Asset & 3D Model Library",
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    "Official 3D characters, delivery props & world models",
                                    color = HoneyYellowLight,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Card(
                            onClick = onDismiss,
                            colors = CardDefaults.cardColors(containerColor = Color(0x33FFFFFF)),
                            shape = CircleShape,
                            modifier = Modifier.testTag("btn_close_asset_library")
                        ) {
                            Text(
                                "✕",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    // Category Tabs
                    TabRow(
                        selectedTabIndex = selectedCategoryIndex,
                        containerColor = Color.White,
                        contentColor = DustyRose,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedCategoryIndex]),
                                color = DustyRose
                            )
                        }
                    ) {
                        categories.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedCategoryIndex == index,
                                onClick = {
                                    selectedCategoryIndex = index
                                    val first = allAssets.firstOrNull { index == 0 || it.category == categories[index] }
                                    if (first != null) selectedAsset = first
                                },
                                text = {
                                    Text(
                                        title,
                                        fontWeight = if (selectedCategoryIndex == index) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.sp
                                    )
                                }
                            )
                        }
                    }

                    // Two-Pane Content: Asset Grid/List on Left, Detailed Inspector on Right
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Left List
                        LazyColumn(
                            modifier = Modifier
                                .weight(0.48f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredAssets) { item ->
                                val isSelected = item.id == selectedAsset.id
                                Card(
                                    onClick = { selectedAsset = item },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) Color.White else Color(0xEEFFFFFF)
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, HoneyYellow) else null,
                                    elevation = CardDefaults.cardElevation(if (isSelected) 4.dp else 1.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("asset_item_${item.id}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Image(
                                            painter = painterResource(id = item.drawableRes),
                                            contentDescription = item.name,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(52.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .border(1.5.dp, HoneyYellowLight, RoundedCornerShape(12.dp))
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                item.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = DarkSurface
                                            )
                                            Text(
                                                item.subtitle,
                                                fontSize = 11.sp,
                                                color = Color(0xFF64748B)
                                            )
                                            Text(
                                                item.modelType,
                                                fontSize = 10.sp,
                                                color = TealBrand,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Right Inspector Card
                        Card(
                            modifier = Modifier
                                .weight(0.52f)
                                .fillMaxHeight(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Large Hero Asset Artwork
                                Box(
                                    modifier = Modifier
                                        .size(130.dp)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(Color(0xFFF1F5F9))
                                        .border(2.dp, HoneyYellow, RoundedCornerShape(20.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = selectedAsset.drawableRes),
                                        contentDescription = selectedAsset.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    selectedAsset.name,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp,
                                    color = DarkSurface
                                )
                                Text(
                                    selectedAsset.subtitle,
                                    fontSize = 12.sp,
                                    color = DustyRose,
                                    fontWeight = FontWeight.SemiBold
                                )

                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                                Text(
                                    selectedAsset.description,
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF475569),
                                    lineHeight = 16.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp)
                                )

                                Spacer(modifier = Modifier.weight(1f))

                                // Stats pills
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    selectedAsset.stats.forEach { (label, value) ->
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(label, fontSize = 10.sp, color = Color.Gray)
                                            Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DarkSurface)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
