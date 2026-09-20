package com.example.game.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.engine.Camera3D
import com.example.game.engine.CatRiderRenderer3D
import com.example.game.model.BackpackOption
import com.example.game.model.CatCustomization
import com.example.game.model.FurColorOption
import com.example.game.model.HelmetOption
import com.example.game.model.JacketOption
import com.example.game.model.ScooterColorOption
import com.example.game.model.ScooterModelOption
import com.example.game.state.GameViewModel
import com.example.ui.theme.DustyRose
import com.example.ui.theme.HoneyYellow
import com.example.ui.theme.TealBrand

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CharacterCreatorScreen(viewModel: GameViewModel) {
    val customization by viewModel.customization.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var riderName by remember { mutableStateOf(customization.name) }
    var turntableYaw by remember { mutableFloatStateOf(15f) }

    val tabs = listOf("🐱 Breed & Fur", "⛑️ Helmet", "🧥 Jacket", "🎒 Backpack", "🛵 Scooter")

    // Turntable 3D preview camera & renderer
    val previewCamera = remember {
        Camera3D(
            posX = 0f,
            posY = 2.2f,
            posZ = -4.8f,
            yawDeg = 0f,
            pitchDeg = 15f,
            fov = 55f
        )
    }
    val riderRenderer = remember { CatRiderRenderer3D() }

    // Auto-spin turntable slightly
    var frameTime by remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) {
        var lastNanos = 0L
        while (true) {
            withFrameNanos { nanos ->
                if (lastNanos != 0L) {
                    val dt = (nanos - lastNanos) / 1_000_000_000f
                    turntableYaw = (turntableYaw + dt * 15f) % 360f
                    riderRenderer.updateAnimations(dt)
                    frameTime = nanos
                }
                lastNanos = nanos
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0F172A),
                        Color(0xFF1E293B),
                        Color(0xFF0F172A)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("character_creator_screen")
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🐾", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "LALAMEOW RIDER",
                            color = HoneyYellow,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                    }
                    Text(
                        text = "Customize your courier & scooter",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                }

                // Randomize Button
                Button(
                    onClick = {
                        val randomFur = FurColorOption.values().random()
                        val randomHelmet = HelmetOption.values().random()
                        val randomJacket = JacketOption.values().random()
                        val randomBackpack = BackpackOption.values().random()
                        val randomModel = ScooterModelOption.values().random()
                        val randomScooter = ScooterColorOption.values().random()
                        val randomNames = listOf("Mochi", "Boba", "Nori", "Wasabi", "Paws", "Biscuit", "Chai", "Truffle")
                        val newName = randomNames.random()
                        riderName = newName
                        viewModel.updateCustomization(
                            customization.copy(
                                name = newName,
                                furColor = randomFur,
                                helmet = randomHelmet,
                                jacket = randomJacket,
                                backpack = randomBackpack,
                                scooterModel = randomModel,
                                scooterColor = randomScooter
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("randomize_button")
                ) {
                    Text("🎲 Random", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // 3D Turntable Preview Stage
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFF334155), Color(0xFF1E293B)),
                            radius = 450f
                        )
                    )
                    .border(1.5.dp, Color(0x33FFFFFF), RoundedCornerShape(20.dp))
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            turntableYaw = (turntableYaw - dragAmount.x * 0.5f + 360f) % 360f
                        }
                    }
                    .testTag("3d_turntable_preview"),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    @Suppress("UNUSED_VARIABLE")
                    val dummy = frameTime
                    val w = size.width
                    val h = size.height

                    // Draw pedestal disk
                    val diskY = h * 0.72f
                    drawOval(
                        color = Color(0x33000000),
                        topLeft = Offset(w * 0.15f, diskY - 10f),
                        size = Size(w * 0.7f, 40f)
                    )
                    drawOval(
                        color = Color(0xFF475569),
                        topLeft = Offset(w * 0.2f, diskY - 20f),
                        size = Size(w * 0.6f, 32f)
                    )
                    drawOval(
                        color = HoneyYellow.copy(alpha = 0.6f),
                        topLeft = Offset(w * 0.22f, diskY - 18f),
                        size = Size(w * 0.56f, 26f)
                    )

                    // Render 3D Rider on turntable
                    previewCamera.yawDeg = turntableYaw
                    riderRenderer.renderCatRider(
                        drawScope = this,
                        camera = previewCamera,
                        worldX = 0f,
                        worldY = 0.4f,
                        worldZ = 0f,
                        headingDeg = 0f,
                        steeringAngleDeg = 0f,
                        speed = 0f,
                        leanAngleDeg = 0f,
                        customization = customization,
                        isCelebrating = false,
                        isHeadlightOn = true
                    )
                }

                Text(
                    text = "↔ Drag to rotate",
                    color = Color(0x88CBD5E1),
                    fontSize = 11.sp,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Navigation Tabs for Categories
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = HoneyYellow,
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 13.sp,
                                fontWeight = if (selectedTab == index) FontWeight.ExtraBold else FontWeight.Normal,
                                color = if (selectedTab == index) HoneyYellow else Color(0xFF94A3B8)
                            )
                        }
                    )
                }
            }

            // Options Content Area
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // TAB 0: Identity & Breed
                if (selectedTab == 0) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Rider Name",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = riderName,
                                    onValueChange = {
                                        riderName = it
                                        viewModel.updateCustomization(customization.copy(name = it))
                                    },
                                    placeholder = { Text("Enter cat name...", color = Color.Gray) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = HoneyYellow,
                                        unfocusedBorderColor = Color(0xFF475569),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("cat_name_input")
                                )
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Select Fur Breed",
                            color = Color(0xFFE2E8F0),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    items(FurColorOption.values().toList()) { fur ->
                        val isSelected = customization.furColor == fur
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFF334155) else Color(0xFF1E293B)
                            ),
                            shape = RoundedCornerShape(14.dp),
                            border = if (isSelected) CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(HoneyYellow, TealBrand))) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateCustomization(customization.copy(furColor = fur))
                                }
                                .testTag("fur_option_${fur.name}")
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(fur.primaryColor)
                                        .border(2.dp, fur.secondaryColor, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = fur.label,
                                        color = if (isSelected) HoneyYellow else Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Soft patterned plush coat",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // TAB 1: Helmets & Bows
                if (selectedTab == 1) {
                    items(HelmetOption.values().toList()) { helmet ->
                        val isSelected = customization.helmet == helmet
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFF334155) else Color(0xFF1E293B)
                            ),
                            shape = RoundedCornerShape(14.dp),
                            border = if (isSelected) CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(HoneyYellow, TealBrand))) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateCustomization(customization.copy(helmet = helmet))
                                }
                                .testTag("helmet_option_${helmet.name}")
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(helmet.color)
                                        .border(2.dp, helmet.accentColor, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = helmet.label,
                                        color = if (isSelected) HoneyYellow else Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Courier certified protection",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // TAB 2: Jackets
                if (selectedTab == 2) {
                    items(JacketOption.values().toList()) { jacket ->
                        val isSelected = customization.jacket == jacket
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFF334155) else Color(0xFF1E293B)
                            ),
                            shape = RoundedCornerShape(14.dp),
                            border = if (isSelected) CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(HoneyYellow, TealBrand))) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateCustomization(customization.copy(jacket = jacket))
                                }
                                .testTag("jacket_option_${jacket.name}")
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(jacket.color)
                                        .border(2.dp, jacket.trimColor, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = jacket.label,
                                        color = if (isSelected) HoneyYellow else Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Thermal weather resistant",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // TAB 3: Backpacks
                if (selectedTab == 3) {
                    items(BackpackOption.values().toList()) { bag ->
                        val isSelected = customization.backpack == bag
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFF334155) else Color(0xFF1E293B)
                            ),
                            shape = RoundedCornerShape(14.dp),
                            border = if (isSelected) CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(HoneyYellow, TealBrand))) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateCustomization(customization.copy(backpack = bag))
                                }
                                .testTag("backpack_option_${bag.name}")
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(bag.color)
                                        .border(2.dp, Color.White, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = bag.iconSymbol, fontSize = 16.sp)
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = bag.label,
                                        color = if (isSelected) HoneyYellow else Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "High-capacity insulated delivery bag",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // TAB 4: Scooter Model & Paint
                if (selectedTab == 4) {
                    item {
                        Text(
                            text = "Scooter Model",
                            color = Color(0xFFE2E8F0),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    items(ScooterModelOption.values().toList()) { model ->
                        val isSelected = customization.scooterModel == model
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFF334155) else Color(0xFF1E293B)
                            ),
                            shape = RoundedCornerShape(14.dp),
                            border = if (isSelected) CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(HoneyYellow, TealBrand))) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateCustomization(customization.copy(scooterModel = model))
                                }
                                .testTag("scooter_model_${model.name}")
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "🛵", fontSize = 28.sp)
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = model.label,
                                        color = if (isSelected) HoneyYellow else Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Top Speed: ${model.baseSpeed.toInt()} km/h • ${model.description}",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Scooter Color Finish",
                            color = Color(0xFFE2E8F0),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 10.dp)
                        )
                    }

                    items(ScooterColorOption.values().toList()) { colorOpt ->
                        val isSelected = customization.scooterColor == colorOpt
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFF334155) else Color(0xFF1E293B)
                            ),
                            shape = RoundedCornerShape(14.dp),
                            border = if (isSelected) CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(HoneyYellow, TealBrand))) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateCustomization(customization.copy(scooterColor = colorOpt))
                                }
                                .testTag("scooter_color_${colorOpt.name}")
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(colorOpt.bodyColor)
                                        .border(2.dp, colorOpt.seatColor, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = colorOpt.label,
                                        color = if (isSelected) HoneyYellow else Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "High-gloss protective lacquer",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Bottom Primary Start Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.closeCharacterCreator()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HoneyYellow),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .shadow(8.dp, RoundedCornerShape(16.dp))
                        .testTag("adopt_and_start_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "ADOPT & HIT THE ROAD! 🚀🐾",
                            color = Color(0xFF0F172A),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    }
}
