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
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import es.uc3m.android.pockets_chef_app.R
import es.uc3m.android.pockets_chef_app.data.model.PantryItem
import es.uc3m.android.pockets_chef_app.data.model.ShoppingItem
import es.uc3m.android.pockets_chef_app.ui.theme.ErrorRed
import es.uc3m.android.pockets_chef_app.ui.theme.PocketsChefTheme
import es.uc3m.android.pockets_chef_app.ui.theme.WarningAmber
import es.uc3m.android.pockets_chef_app.ui.viewmodel.PantryViewModel
import es.uc3m.android.pockets_chef_app.ui.viewmodel.ShoppingListViewModel

private data class CategoryInfo(val name: String, val resId: Int)

private val categories = listOf(
    CategoryInfo("All", R.string.category_all),
    CategoryInfo("Dairy", R.string.category_dairy),
    CategoryInfo("Meat", R.string.category_meat),
    CategoryInfo("Vegetables", R.string.category_vegetables),
    CategoryInfo("Grains", R.string.category_grains),
    CategoryInfo("Condiments", R.string.category_condiments)
)

@Composable
fun PantryScreen(
    navController: NavController,
    viewModel: PantryViewModel = viewModel(),
    shoppingListViewModel: ShoppingListViewModel = viewModel()
) {
    val pantryItems by viewModel.itemsState.collectAsState()
    val shoppingItems by shoppingListViewModel.items.collectAsState()
    val context = LocalContext.current

    PantryScreenContent(
        pantryItems = pantryItems,
        shoppingItems = shoppingItems,
        onAddItem = { item -> viewModel.addItem(item, context) },
        onDeleteItem = { item -> viewModel.deleteItem(item) },
        onToggleShoppingItem = { item -> shoppingListViewModel.toggleItem(item) },
        onDeleteShoppingItem = { item -> shoppingListViewModel.deleteItem(item) },
        onBackClick = { navController.popBackStack() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantryScreenContent(
    pantryItems: List<PantryItem>,
    shoppingItems: List<ShoppingItem> = emptyList(),
    onAddItem: (PantryItem) -> Unit,
    onDeleteItem: (PantryItem) -> Unit,
    onToggleShoppingItem: (ShoppingItem) -> Unit = {},
    onDeleteShoppingItem: (ShoppingItem) -> Unit = {},
    onBackClick: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf("All") }
    var showAddDialog by remember { mutableStateOf(false) }
    var showShoppingList by remember { mutableStateOf(false) }

    val displayedItems = if (selectedCategory == "All") pantryItems
    else pantryItems.filter { it.category == selectedCategory }

    Scaffold(
        topBar = {
            ElegantHeader(
                title = stringResource(R.string.my_pantry),
                subtitle = stringResource(R.string.items_in_your_pantry, pantryItems.size)
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                // Shopping list button
                SmallFloatingActionButton(
                    onClick = { showShoppingList = true },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    BadgedBox(
                        badge = {
                            if (shoppingItems.isNotEmpty()) {
                                Badge { Text(shoppingItems.size.toString()) }
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = "Shopping List",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                // Add pantry item button
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(R.string.add_item),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategory == category.name,
                        onClick = { selectedCategory = category.name },
                        label = { Text(stringResource(category.resId)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (displayedItems.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.no_items),
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
                        PantryItemCard(
                            item = item,
                            onDelete = { onDeleteItem(item) }
                        )
                    }
                }
            }
        }
    }

    // Add pantry item dialog
    if (showAddDialog) {
        AddPantryItemDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { item ->
                onAddItem(item)
                showAddDialog = false
            }
        )
    }

    // Shopping list bottom sheet
    if (showShoppingList) {
        ModalBottomSheet(
            onDismissRequest = { showShoppingList = false }
        ) {
            ShoppingListContent(
                items = shoppingItems,
                onToggle = onToggleShoppingItem,
                onDelete = onDeleteShoppingItem
            )
        }
    }
}

@Composable
fun ShoppingListContent(
    items: List<ShoppingItem>,
    onToggle: (ShoppingItem) -> Unit,
    onDelete: (ShoppingItem) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = "Shopping List",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No items yet.\nAdd ingredients from a recipe!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items, key = { it.id }) { item ->
                    ShoppingListItem(
                        item = item,
                        onToggle = { onToggle(item) },
                        onDelete = { onDelete(item) }
                    )
                }
            }
        }
    }
}

@Composable
fun ShoppingListItem(
    item: ShoppingItem,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isChecked)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.isChecked,
                onCheckedChange = { onToggle() }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    textDecoration = if (item.isChecked) TextDecoration.LineThrough else null,
                    color = if (item.isChecked)
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    else
                        MaterialTheme.colorScheme.onSurface
                )
                if (item.amount.isNotBlank()) {
                    Text(
                        text = item.amount,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary.copy(
                            alpha = if (item.isChecked) 0.4f else 1f
                        )
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = ErrorRed.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
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
                val categoryResId = categories.find { it.name == item.category }?.resId
                    ?: R.string.category_all
                SuggestionChip(
                    onClick = {},
                    label = {
                        Text(
                            stringResource(categoryResId),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete),
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
    var unit by remember { mutableStateOf("") }
    if (unit.isEmpty()) unit = stringResource(R.string.unit_units)
    var selectedCategoryName by remember { mutableStateOf("Vegetables") }
    var daysUntilExpiry by remember { mutableStateOf("7") }
    val itemCategories = categories.filter { it.name != "All" }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.add_pantry_item_title), fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.item_name_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text(stringResource(R.string.qty_label)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text(stringResource(R.string.unit_label)) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = daysUntilExpiry,
                    onValueChange = { daysUntilExpiry = it },
                    label = { Text(stringResource(R.string.expires_in_days_label)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    stringResource(R.string.category_label),
                    style = MaterialTheme.typography.labelMedium
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(itemCategories) { cat ->
                        FilterChip(
                            selected = selectedCategoryName == cat.name,
                            onClick = { selectedCategoryName = cat.name },
                            label = {
                                Text(
                                    stringResource(cat.resId),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
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
                            id = java.util.UUID.randomUUID().toString(),
                            name = name.trim(),
                            quantity = quantity.ifBlank { "1" },
                            unit = unit.trim(),
                            category = selectedCategoryName,
                            expiryDate = expiryMs,
                            isExpiringSoon = days <= 3
                        )
                    )
                },
                enabled = name.isNotBlank()
            ) { Text(stringResource(R.string.add_button)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel_button)) }
        }
    )
}

@Composable
fun formatExpiry(expiryMs: Long): String {
    val daysLeft = ((expiryMs - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)).toInt()
    return when {
        daysLeft < 0  -> stringResource(R.string.expired)
        daysLeft == 0 -> stringResource(R.string.expires_today)
        daysLeft == 1 -> stringResource(R.string.expires_tomorrow)
        else          -> stringResource(R.string.expires_in_days, daysLeft)
    }
}

@Preview(showBackground = true)
@Composable
fun PantryScreenPreview() {
    PocketsChefTheme {
        PantryScreenContent(
            pantryItems = listOf(
                PantryItem(
                    id = "1", name = "Fresh Milk", quantity = "1", unit = "L",
                    category = "Dairy",
                    expiryDate = System.currentTimeMillis() + 86400000L,
                    isExpiringSoon = true
                )
            ),
            shoppingItems = listOf(
                ShoppingItem(id = "1", name = "Eggs", amount = "6", isChecked = false),
                ShoppingItem(id = "2", name = "Butter", amount = "100g", isChecked = true)
            ),
            onAddItem = {},
            onDeleteItem = {},
            onBackClick = {}
        )
    }
}