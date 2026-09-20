package com.example.game.ui

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.game.model.BackpackOption
import com.example.game.model.CatCustomization
import com.example.game.model.FurColorOption
import com.example.game.model.HelmetOption
import com.example.game.model.JacketOption
import com.example.game.model.ScooterColorOption
import com.example.game.model.ScooterModelOption
import com.example.game.state.GameViewModel
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DustyRose
import com.example.ui.theme.DustyRoseLight
import com.example.ui.theme.HoneyYellow
import com.example.ui.theme.HoneyYellowCream
import com.example.ui.theme.HoneyYellowLight
import com.example.ui.theme.TealBrand

@Composable
fun GarageCustomizationDialog(
    viewModel: GameViewModel,
    onDismiss: () -> Unit
) {
    val customization by viewModel.customization.collectAsState()
    val coins by viewModel.coins.collectAsState()
    val speedLevel by viewModel.speedUpgradeLevel.collectAsState()
    val accelLevel by viewModel.accelUpgradeLevel.collectAsState()
    val tankLevel by viewModel.tankUpgradeLevel.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Cat Rider", "Wardrobe", "Scooter", "Tuning")

    var editName by remember { mutableStateOf(customization.name) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.9f)
                .shadow(24.dp, RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            color = HoneyYellowCream
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurface)
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Meow Garage & Boutique",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        )
                        Text(
                            "🪙 $coins CatCoins Available",
                            color = HoneyYellowLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = HoneyYellow),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.testTag("btn_garage_close")
                    ) {
                        Text("Done", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                // Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.White,
                    contentColor = DustyRose
                ) {
                    tabs.forEachIndexed { idx, title ->
                        Tab(
                            selected = selectedTab == idx,
                            onClick = { selectedTab = idx },
                            text = { Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                        )
                    }
                }

                // Body
                LazyColumn(
                    modifier = Modifier.weight(1f).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    when (selectedTab) {
                        0 -> { // Cat Rider Basics
                            item {
                                Text("Rider Name", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = editName,
                                    onValueChange = {
                                        editName = it
                                        viewModel.updateCustomization(customization.copy(name = it))
                                    },
                                    modifier = Modifier.fillMaxWidth().testTag("input_cat_name"),
                                    singleLine = true,
                                    label = { Text("Name your cat courier") }
                                )
                            }

                            item {
                                Text("Fur Coat & Pattern", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                FurColorOption.values().forEach { fur ->
                                    val isSelected = customization.furColor == fur
                                    Card(
                                        onClick = { viewModel.updateCustomization(customization.copy(furColor = fur)) },
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) DustyRose.copy(alpha = 0.15f) else Color.White
                                        ),
                                        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, DustyRose) else null,
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .background(fur.primaryColor, CircleShape)
                                                    .border(2.dp, fur.secondaryColor, CircleShape)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(fur.label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                        }
                                    }
                                }
                            }
                        }

                        1 -> { // Wardrobe (Helmets, Jackets, Bags)
                            item {
                                Text("Helmet / Headwear", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                HelmetOption.values().forEach { helm ->
                                    val isSelected = customization.helmet == helm
                                    Card(
                                        onClick = { viewModel.updateCustomization(customization.copy(helmet = helm)) },
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) TealBrand.copy(alpha = 0.15f) else Color.White
                                        ),
                                        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, TealBrand) else null,
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                                    ) {
                                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Box(modifier = Modifier.size(20.dp).background(helm.color, CircleShape))
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(helm.label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }

                            item {
                                Text("Courier Jacket / Vest", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                JacketOption.values().forEach { j ->
                                    val isSelected = customization.jacket == j
                                    Card(
                                        onClick = { viewModel.updateCustomization(customization.copy(jacket = j)) },
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) HoneyYellow.copy(alpha = 0.15f) else Color.White
                                        ),
                                        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, HoneyYellow) else null,
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                                    ) {
                                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Box(modifier = Modifier.size(20.dp).background(j.color, CircleShape))
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(j.label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }

                            item {
                                Text("Delivery Backpack", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                BackpackOption.values().forEach { bag ->
                                    val isSelected = customization.backpack == bag
                                    Card(
                                        onClick = { viewModel.updateCustomization(customization.copy(backpack = bag)) },
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) DustyRose.copy(alpha = 0.15f) else Color.White
                                        ),
                                        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, DustyRose) else null,
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                                    ) {
                                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Text(bag.iconSymbol, fontSize = 16.sp)
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(bag.label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }
                        }

                        2 -> { // Scooter Model & Paint
                            item {
                                Text("Scooter Model", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                ScooterModelOption.values().forEach { m ->
                                    val isSelected = customization.scooterModel == m
                                    Card(
                                        onClick = { viewModel.updateCustomization(customization.copy(scooterModel = m)) },
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) TealBrand.copy(alpha = 0.15f) else Color.White
                                        ),
                                        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, TealBrand) else null,
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(m.label, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text(m.description, fontSize = 11.sp, color = Color.Gray)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                "Top Speed: ${m.baseSpeed.toInt()} km/h • Accel: ${m.baseAccel.toInt()}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = TealBrand
                                            )
                                        }
                                    }
                                }
                            }

                            item {
                                Text("Pastel Paint Job", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                ScooterColorOption.values().forEach { sc ->
                                    val isSelected = customization.scooterColor == sc
                                    Card(
                                        onClick = { viewModel.updateCustomization(customization.copy(scooterColor = sc)) },
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) HoneyYellow.copy(alpha = 0.15f) else Color.White
                                        ),
                                        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, HoneyYellow) else null,
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                                    ) {
                                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Box(modifier = Modifier.size(22.dp).background(sc.bodyColor, CircleShape))
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(sc.label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }
                        }

                        3 -> { // Scooter Tuning Upgrades
                            item {
                                UpgradeRow(
                                    title = "Engine Top Speed",
                                    description = "Increases maximum driving velocity",
                                    level = speedLevel,
                                    cost = speedLevel * 50,
                                    coins = coins,
                                    onUpgrade = { viewModel.upgradeSpeed() }
                                )
                            }
                            item {
                                UpgradeRow(
                                    title = "Quick-Torque Acceleration",
                                    description = "Faster pedal pickup and hill climbing",
                                    level = accelLevel,
                                    cost = accelLevel * 45,
                                    coins = coins,
                                    onUpgrade = { viewModel.upgradeAccel() }
                                )
                            }
                            item {
                                UpgradeRow(
                                    title = "Extended Fuel Tank",
                                    description = "Reduces fuel depletion per kilometer",
                                    level = tankLevel,
                                    cost = tankLevel * 40,
                                    coins = coins,
                                    onUpgrade = { viewModel.upgradeTank() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UpgradeRow(
    title: String,
    description: String,
    level: Int,
    cost: Int,
    coins: Int,
    onUpgrade: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(description, fontSize = 11.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Tier $level / 5", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TealBrand)
            }

            if (level < 5) {
                Button(
                    onClick = onUpgrade,
                    enabled = coins >= cost,
                    colors = ButtonDefaults.buttonColors(containerColor = HoneyYellow),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("🪙 $cost", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            } else {
                Text("MAX ✨", fontWeight = FontWeight.ExtraBold, color = DustyRose, fontSize = 13.sp)
            }
        }
    }
}
