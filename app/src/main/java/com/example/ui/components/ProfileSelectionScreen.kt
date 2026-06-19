package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.ui.CineViewModel
import com.example.ui.theme.*

// Colors for Netflix-style colorful squares
val ProfileColors = listOf(
    Color(0xFF651FFF), // Purple
    Color(0xFFFF3D00), // Vibrant Orange
    Color(0xFF00E5FF), // Teal
    Color(0xFF00E676), // Lime Green
    Color(0xFFF50057)  // Ruby pink
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProfileSelectionScreen(
    viewModel: CineViewModel,
    modifier: Modifier = Modifier
) {
    val profiles by viewModel.profilesList.collectAsState()
    var isCreateMode by remember { mutableStateOf(false) }
    var newProfileName by remember { mutableStateOf("") }
    var selectedAvatarIndex by remember { mutableStateOf(0) }
    var isDeleteMode by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        BlackBackground,
                        Color(0xFF140D05), // Amber undertone
                        BlackBackground
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        if (!isCreateMode) {
            // --- Select Profile View ---
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header Brand Logo
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(bottom = 32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Movie,
                        contentDescription = "Free Films Logo",
                        tint = OrangePrimary,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "FREE FILMS",
                        color = OrangePrimary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                }

                Text(
                    text = "Quem está assistindo?",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Profiles Grid
                val displayProfiles = profiles.take(5) // Limit to 5
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    modifier = Modifier
                        .widthIn(max = 320.dp)
                        .weight(1f, fill = false)
                ) {
                    items(displayProfiles) { profile ->
                        val avatarColor = ProfileColors.getOrElse(profile.avatarIndex) { OrangePrimary }
                        
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .testTag("profile_item_${profile.name}")
                                .combinedClickable(
                                    onClick = { 
                                        if (isDeleteMode) {
                                            viewModel.deleteProfile(profile)
                                        } else {
                                            viewModel.selectProfile(profile)
                                        }
                                    },
                                    onLongClick = {
                                        isDeleteMode = !isDeleteMode
                                    }
                                )
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(avatarColor)
                                    .border(
                                        width = if (isDeleteMode) 3.dp else 0.dp,
                                        color = if (isDeleteMode) Color.Red else Color.Transparent,
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                // Draw the stylized initials inside avatar
                                Text(
                                    text = profile.name.take(1).uppercase(),
                                    color = Color.White,
                                    fontSize = 42.sp,
                                    fontWeight = FontWeight.Black
                                )
                                
                                if (isDeleteMode) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.5f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Excluir Perfil",
                                            tint = Color.Red,
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = profile.name,
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Add profile option if less than 5 profiles
                    if (displayProfiles.size < 5) {
                        item {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .testTag("add_profile_card")
                                    .clickable {
                                        newProfileName = ""
                                        selectedAvatarIndex = displayProfiles.size % ProfileColors.size
                                        isCreateMode = true
                                    }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(2.dp, TextSecondary, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Adicionar Perfil",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Adicionar",
                                    color = TextSecondary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

                // Toggle delete mode button
                if (profiles.isNotEmpty()) {
                    OutlinedButton(
                        onClick = { isDeleteMode = !isDeleteMode },
                        border = BorderStroke(1.dp, if (isDeleteMode) OrangePrimary else TextSecondary),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.testTag("manage_profiles_button")
                    ) {
                        Text(
                            text = if (isDeleteMode) "Concluído" else "Gerenciar Perfis",
                            fontSize = 14.sp,
                            color = if (isDeleteMode) OrangePrimary else Color.White
                        )
                    }
                }
            }
        } else {
            // --- Create Profile View ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .shadow(12.dp, RoundedCornerShape(16.dp))
                    .testTag("create_profile_dialog"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BlackSurface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Criar Perfil",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Profile Avatar Selection Previews
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(bottom = 20.dp)
                    ) {
                        ProfileColors.forEachIndexed { idx, color ->
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(color)
                                    .border(
                                        width = if (selectedAvatarIndex == idx) 3.dp else 0.dp,
                                        color = if (selectedAvatarIndex == idx) Color.White else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedAvatarIndex = idx },
                                contentAlignment = Alignment.Center
                            ) {
                                if (selectedAvatarIndex == idx) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selecionado",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                } else if (newProfileName.isNotBlank()) {
                                    Text(
                                        text = newProfileName.take(1).uppercase(),
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                }
                            }
                        }
                    }

                    // Input Text
                    OutlinedTextField(
                        value = newProfileName,
                        onValueChange = { if (it.length <= 12) newProfileName = it },
                        label = { Text("Nome do perfil", color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = OrangePrimary,
                            unfocusedBorderColor = TextSecondary,
                            focusedLabelColor = OrangePrimary,
                            cursorColor = OrangePrimary
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp)
                            .testTag("new_profile_name_input")
                    )

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TextButton(
                            onClick = { isCreateMode = false },
                            colors = ButtonDefaults.textButtonColors(contentColor = TextSecondary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancelar", fontSize = 16.sp)
                        }

                        Button(
                            onClick = {
                                if (newProfileName.isNotBlank()) {
                                    viewModel.createProfile(newProfileName.trim(), selectedAvatarIndex)
                                    isCreateMode = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                            enabled = newProfileName.isNotBlank(),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("save_profile_button")
                        ) {
                            Text("Salvar", fontSize = 16.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
