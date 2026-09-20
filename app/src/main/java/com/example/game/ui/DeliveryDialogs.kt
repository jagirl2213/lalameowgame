package com.example.game.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.game.model.DeliveryCategory
import com.example.game.model.DeliveryJob
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DustyRose
import com.example.ui.theme.HoneyYellow
import com.example.ui.theme.HoneyYellowCream
import com.example.ui.theme.TealBrand

fun getCustomerAsset(name: String): Int {
    return when {
        name.contains("Lily", ignoreCase = true) -> R.drawable.char_lily
        name.contains("Benny", ignoreCase = true) -> R.drawable.char_benny
        name.contains("Momo", ignoreCase = true) -> R.drawable.char_momo
        name.contains("Grandma", ignoreCase = true) || name.contains("Tabitha", ignoreCase = true) -> R.drawable.char_grandma
        name.contains("Pigeon", ignoreCase = true) || name.contains("Pickles", ignoreCase = true) -> R.drawable.char_pigeon
        else -> R.drawable.char_nami
    }
}

fun getItemAsset(category: DeliveryCategory): Int {
    return when (category) {
        DeliveryCategory.NEWSPAPER -> R.drawable.item_newspaper
        DeliveryCategory.FOOD -> R.drawable.item_croissant
        else -> R.drawable.item_parcel
    }
}

@Composable
fun DeliveryRequestDialog(
    job: DeliveryJob,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Dialog(onDismissRequest = onDecline) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = HoneyYellowCream),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .shadow(16.dp, RoundedCornerShape(28.dp))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Header badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(job.category.badgeColor, RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "${job.category.icon} ${job.category.label}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    if (job.isUrgent) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFEF4444), RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "⚡ URGENT",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Item 3D Asset Art & Description
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = getItemAsset(job.category)),
                        contentDescription = job.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(68.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(2.dp, HoneyYellow, RoundedCornerShape(16.dp))
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            job.title,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            color = DarkSurface
                        )
                        Text(
                            job.description,
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Route summary box
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🏪", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Pickup Location", fontSize = 10.sp, color = Color.Gray)
                                Text(
                                    "${job.pickupVenue} • ${job.pickupDistrict}",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = getCustomerAsset(job.dropoffCustomer)),
                                contentDescription = job.dropoffCustomer,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .border(1.5.dp, HoneyYellow, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Deliver To", fontSize = 10.sp, color = Color.Gray)
                                Text(
                                    "${job.dropoffCustomer} • ${job.dropoffDistrict}",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Rewards bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFFEF3C7), RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text("🪙 +${job.rewardCoins} Coins", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFB45309))
                        }
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFCCFBF1), RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text("⭐ +${job.rewardRep} Rep", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF0F766E))
                        }
                    }

                    Text("~${job.estimatedDistanceMeters}m", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Accept & Decline Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDecline,
                        modifier = Modifier.weight(1f).testTag("btn_decline_job"),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Decline", color = Color(0xFF64748B))
                    }

                    Button(
                        onClick = onAccept,
                        modifier = Modifier.weight(1f).testTag("btn_accept_job"),
                        colors = ButtonDefaults.buttonColors(containerColor = HoneyYellow),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("ACCEPT 🐾", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerInteractionDialog(
    dialogueText: String,
    onContinue: () -> Unit
) {
    Dialog(onDismissRequest = onContinue) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .shadow(16.dp, RoundedCornerShape(28.dp))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Real Customer Portrait
                Image(
                    painter = painterResource(id = getCustomerAsset(dialogueText)),
                    contentDescription = "Customer",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .border(3.dp, HoneyYellow, CircleShape)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    "Delivery Successful!",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = DarkSurface
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    dialogueText,
                    fontSize = 14.sp,
                    color = Color(0xFF334155),
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onContinue,
                    colors = ButtonDefaults.buttonColors(containerColor = DustyRose),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().testTag("btn_customer_dialog_ok")
                ) {
                    Text("Purr-fect! Continue Riding 🛵", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
