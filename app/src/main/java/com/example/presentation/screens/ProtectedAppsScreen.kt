package com.example.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AppCategory
import com.example.domain.model.ProtectedApp
import com.example.presentation.components.NovaCard
import com.example.presentation.components.NovaStatusBadge
import com.example.presentation.components.NovaTopBar
import com.example.presentation.viewmodel.ProtectionFilter
import com.example.ui.theme.NovaCyanNeon
import com.example.ui.theme.NovaDarkCard
import com.example.ui.theme.NovaDarkCardBorder
import com.example.ui.theme.NovaElectricViolet
import com.example.ui.theme.NovaSuccessGreen
import com.example.ui.theme.NovaTextMuted
import com.example.ui.theme.NovaTextPrimary
import com.example.ui.theme.NovaTextSecondary
import com.example.ui.theme.NovaVoidBlack

@Composable
fun ProtectedAppsScreen(
    apps: List<ProtectedApp>,
    searchQuery: String,
    selectedCategory: AppCategory?,
    protectionFilter: ProtectionFilter,
    onSearchChange: (String) -> Unit,
    onCategorySelect: (AppCategory?) -> Unit,
    onFilterSelect: (ProtectionFilter) -> Unit,
    onToggleProtection: (String, Boolean) -> Unit,
    onSelectAll: () -> Unit,
    onClearAll: () -> Unit,
    onResetDefaults: () -> Unit,
    onAppClick: (ProtectedApp) -> Unit,
    onBack: () -> Unit
) {
    val totalProtected = apps.count { it.isProtected }
    val categoryScroll = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NovaVoidBlack)
            .testTag("protected_apps_screen")
    ) {
        NovaTopBar(
            title = "Protected Apps",
            subtitle = "$totalProtected active of ${apps.size} shown",
            onBack = onBack,
            actions = {
                Row {
                    IconButton(
                        onClick = onResetDefaults,
                        modifier = Modifier.testTag("apps_reset_defaults")
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Reset Defaults",
                            tint = NovaCyanNeon
                        )
                    }
                }
            }
        )

        // Search Field
        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("apps_search_input"),
                placeholder = { Text("Search by application or package...", color = NovaTextMuted) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = NovaCyanNeon
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = NovaTextSecondary)
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NovaCyanNeon,
                    unfocusedBorderColor = NovaDarkCardBorder,
                    focusedTextColor = NovaTextPrimary,
                    unfocusedTextColor = NovaTextPrimary,
                    cursorColor = NovaCyanNeon
                ),
                singleLine = true
            )
        }

        // Quick Batch Action Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    onClick = onSelectAll,
                    modifier = Modifier.testTag("btn_select_all")
                ) {
                    Text("Select All", color = NovaCyanNeon, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                TextButton(
                    onClick = onClearAll,
                    modifier = Modifier.testTag("btn_clear_all")
                ) {
                    Text("Clear All", color = NovaTextSecondary, fontSize = 12.sp)
                }
            }

            // Filter status chips
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                ProtectionFilter.entries.forEach { filter ->
                    val isSelected = (protectionFilter == filter)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) NovaElectricViolet else NovaDarkCard)
                            .clickable { onFilterSelect(filter) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("filter_${filter.name}")
                    ) {
                        Text(
                            text = filter.label,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.Black else NovaTextSecondary
                        )
                    }
                }
            }
        }

        // Category Horizontal Scroll Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(categoryScroll)
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onCategorySelect(null) },
                label = { Text("All Categories", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = NovaCyanNeon,
                    selectedLabelColor = Color.Black,
                    containerColor = NovaDarkCard,
                    labelColor = NovaTextSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedCategory == null,
                    borderColor = NovaDarkCardBorder,
                    selectedBorderColor = NovaCyanNeon
                )
            )

            AppCategory.entries.forEach { cat ->
                val isSelected = (selectedCategory == cat)
                FilterChip(
                    selected = isSelected,
                    onClick = { onCategorySelect(if (isSelected) null else cat) },
                    label = { Text(cat.displayName, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NovaCyanNeon,
                        selectedLabelColor = Color.Black,
                        containerColor = NovaDarkCard,
                        labelColor = NovaTextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = NovaDarkCardBorder,
                        selectedBorderColor = NovaCyanNeon
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Apps List
        if (apps.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No applications match your filter",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NovaTextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Try clearing the search query or selecting 'All Apps'",
                        fontSize = 12.sp,
                        color = NovaTextSecondary
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(apps, key = { it.packageName }) { app ->
                    AppItemRow(
                        app = app,
                        onToggle = { isChecked -> onToggleProtection(app.packageName, isChecked) },
                        onClick = { onAppClick(app) }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun AppItemRow(
    app: ProtectedApp,
    onToggle: (Boolean) -> Unit,
    onClick: () -> Unit
) {
    NovaCard(
        borderColor = if (app.isProtected) NovaCyanNeon.copy(alpha = 0.35f) else NovaDarkCardBorder,
        onClick = onClick,
        modifier = Modifier.testTag("app_item_${app.packageName}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Avatar Placeholder / Initials
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (app.isProtected) NovaElectricViolet.copy(alpha = 0.25f) else NovaDarkCardBorder
                    ),
                contentAlignment = Alignment.Center
            ) {
                val initial = app.appName.firstOrNull()?.uppercase() ?: "A"
                Text(
                    text = initial,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (app.isProtected) NovaCyanNeon else NovaTextSecondary
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.appName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = NovaTextPrimary
                )
                Text(
                    text = app.category.displayName,
                    fontSize = 11.sp,
                    color = NovaElectricViolet,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = app.packageName,
                    fontSize = 10.sp,
                    color = NovaTextMuted,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Switch(
                checked = app.isProtected,
                onCheckedChange = onToggle,
                modifier = Modifier.testTag("toggle_${app.packageName}"),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = NovaVoidBlack,
                    checkedTrackColor = NovaCyanNeon,
                    uncheckedThumbColor = NovaTextSecondary,
                    uncheckedTrackColor = NovaDarkCardBorder
                )
            )
        }
    }
}
