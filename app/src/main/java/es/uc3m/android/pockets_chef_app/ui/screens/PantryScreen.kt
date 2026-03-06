package es.uc3m.android.pockets_chef_app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import es.uc3m.android.pockets_chef_app.data.model.PantryItem
import es.uc3m.android.pockets_chef_app.ui.theme.ErrorRed
import es.uc3m.android.pockets_chef_app.ui.theme.PocketsChefTheme
import es.uc3m.android.pockets_chef_app.ui.theme.WarningAmber
import es.uc3m.android.pockets_chef_app.ui.viewmodel.PantryViewModel

private val categories = listOf("All", "Dairy", "Meat", "Vegetables", "Grains", "Condiments")

@Composable
fun PantryScreen(
    navController: NavController,
    viewModel: PantryViewModel = viewModel()
) {
    var selectedCategory by remember { mutableStateOf("All") }
    var showAddDialog by remember { mutableStateOf(false) }

    val displayedItems = if (selectedCategory == "All") viewModel.items
                         else viewModel.items.filter { it.category == selectedCategory }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add item",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {

            Surface(color = MaterialTheme.colorScheme.primary) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(
                        text = "My Pantry",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text = "${viewModel.items.size} items in your pantry",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (displayedItems.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No items yet. Tap + to add!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(displayedItems, key = { it.id }) { item ->
                        PantryItemCard(item = item, onDelete = { viewModel.deleteItem(item) })
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddPantryItemDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { item ->
                viewModel.addItem(item)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun PantryItemCard(item: PantryItem, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${item.quantity} ${item.unit}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formatExpiry(item.expiryDate),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (item.isExpiringSoon) WarningAmber
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                SuggestionChip(
                    onClick = {},
                    label = { Text(item.category, style = MaterialTheme.typography.labelSmall) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = ErrorRed.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun AddPantryItemDialog(onDismiss: () -> Unit, onConfirm: (PantryItem) -> Unit) {
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var unit by remember { mutableStateOf("units") }
    var selectedCategory by remember { mutableStateOf("Vegetables") }
    var daysUntilExpiry by remember { mutableStateOf("7") }
    val itemCategories = listOf("Dairy", "Meat", "Vegetables", "Grains", "Condiments")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Pantry Item", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Item name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("Qty") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unit") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = daysUntilExpiry,
                    onValueChange = { daysUntilExpiry = it },
                    label = { Text("Expires in (days)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Category", style = MaterialTheme.typography.labelMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(itemCategories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val days = daysUntilExpiry.toLongOrNull() ?: 7L
                    val expiryMs = System.currentTimeMillis() + days * 24 * 60 * 60 * 1000
                    onConfirm(
                        PantryItem(
                            name = name.trim(),
                            quantity = quantity.ifBlank { "1" },
                            unit = unit.trim(),
                            category = selectedCategory,
                            expiryDate = expiryMs,
                            isExpiringSoon = days <= 3
                        )
                    )
                },
                enabled = name.isNotBlank()
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

fun formatExpiry(expiryMs: Long): String {
    val daysLeft = ((expiryMs - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)).toInt()
    return when {
        daysLeft < 0  -> "Expired"
        daysLeft == 0 -> "Expires today!"
        daysLeft == 1 -> "Expires tomorrow"
        else          -> "Expires in $daysLeft days"
    }
}

@Preview(showBackground = true)
@Composable
fun PantryScreenPreview() {
    PocketsChefTheme { PantryScreen(navController = rememberNavController()) }
}
