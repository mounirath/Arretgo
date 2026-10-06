package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Subway
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FavoritePlace
import com.example.model.AppLanguage
import com.example.ui.theme.ArrivaCyan
import com.example.ui.theme.ArrivaIndigo

@Composable
fun FavoritesDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    favorites: List<FavoritePlace>,
    onSelectFavorite: (FavoritePlace) -> Unit,
    onDeleteFavorite: (FavoritePlace) -> Unit,
    currentLanguage: AppLanguage
) {
    if (!isOpen) return

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF13182C).copy(alpha = 0.94f),
        modifier = Modifier.border(
            androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.GlassTokens.GlassBorderBrush),
            RoundedCornerShape(28.dp)
        ),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = when (currentLanguage) {
                        AppLanguage.AR -> "⭐ الأماكن والمحطات المفضلة"
                        AppLanguage.EN -> "⭐ Favorite Places & Stations"
                        AppLanguage.FR -> "⭐ Lieux et Arrêts Favoris"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (favorites.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.AR -> "لا توجد أماكن محفوظة حتى الآن"
                                AppLanguage.EN -> "No favorite places saved yet"
                                AppLanguage.FR -> "Aucun favori enregistré pour le moment"
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 320.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(favorites) { place ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSelectFavorite(place)
                                        onDismiss()
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val icon = when (place.tag.lowercase()) {
                                        "gare" -> Icons.Default.Train
                                        "métro", "metro" -> Icons.Default.Subway
                                        "maison", "domicile" -> Icons.Default.Home
                                        "travail", "bureau" -> Icons.Default.Business
                                        else -> Icons.Default.LocationOn
                                    }
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = ArrivaIndigo,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = place.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (place.address.isNotEmpty()) {
                                            Text(
                                                text = place.address,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                    IconButton(
                                        onClick = { onDeleteFavorite(place) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Supprimer",
                                            tint = Color(0xFFF43F5E),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = when (currentLanguage) {
                        AppLanguage.AR -> "إغلاق"
                        AppLanguage.EN -> "Close"
                        AppLanguage.FR -> "Fermer"
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}

@Composable
fun AddFavoriteDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    initialName: String,
    initialAddress: String,
    onConfirm: (name: String, tag: String) -> Unit,
    currentLanguage: AppLanguage
) {
    if (!isOpen) return

    var customName by remember { mutableStateOf(initialName) }
    var selectedTag by remember { mutableStateOf("Gare") }
    val tags = listOf("Gare", "Métro", "Domicile", "Travail", "Autre")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF13182C).copy(alpha = 0.94f),
        modifier = Modifier.border(
            androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.GlassTokens.GlassBorderBrush),
            RoundedCornerShape(28.dp)
        ),
        title = {
            Text(
                text = when (currentLanguage) {
                    AppLanguage.AR -> "حفظ في المفضلة ⭐"
                    AppLanguage.EN -> "Save to Favorites ⭐"
                    AppLanguage.FR -> "Enregistrer dans les Favoris ⭐"
                },
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = customName,
                    onValueChange = { customName = it },
                    label = {
                        Text(
                            when (currentLanguage) {
                                AppLanguage.AR -> "اسم المكان"
                                AppLanguage.EN -> "Place Name"
                                AppLanguage.FR -> "Nom du lieu"
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = when (currentLanguage) {
                        AppLanguage.AR -> "الفئة :"
                        AppLanguage.EN -> "Category:"
                        AppLanguage.FR -> "Catégorie :"
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    tags.forEach { tag ->
                        val isSelected = selectedTag == tag
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) ArrivaIndigo else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .clickable { selectedTag = tag }
                        ) {
                            Text(
                                text = tag,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(customName.ifBlank { initialName }, selectedTag)
                    onDismiss()
                }
            ) {
                Text(
                    text = when (currentLanguage) {
                        AppLanguage.AR -> "حفظ"
                        AppLanguage.EN -> "Save"
                        AppLanguage.FR -> "Enregistrer"
                    }
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = when (currentLanguage) {
                        AppLanguage.AR -> "إلغاء"
                        AppLanguage.EN -> "Cancel"
                        AppLanguage.FR -> "Annuler"
                    }
                )
            }
        }
    )
}
