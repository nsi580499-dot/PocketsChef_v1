package es.uc3m.android.pockets_chef_app.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.uc3m.android.pockets_chef_app.data.model.Recipe
import es.uc3m.android.pockets_chef_app.data.repository.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class RecipeViewModel(private val repository: RecipeRepository = RecipeRepository()) : ViewModel() {

    var searchQuery by mutableStateOf("")
    var showFavoritesOnly by mutableStateOf(false)

    private val _recipes = MutableStateFlow<List<Recipe>>(emptyList())
    val recipesState: StateFlow<List<Recipe>> = _recipes.asStateFlow()

    init {
        loadRecipes()
    }

    private fun loadRecipes() {
        viewModelScope.launch {
            repository.getLatestPublicRecipes().collectLatest { list ->
                // Ordenamos en memoria para asegurar que la carga sea rápida 
                // y no dependa de índices compuestos en Firebase
                _recipes.value = list.sortedByDescending { it.createdAt }
            }
        }
    }

    fun getRecipeById(id: String): Recipe? {
        return _recipes.value.find { it.id == id }
    }

    fun toggleFavorite(recipe: Recipe) {
        _recipes.value = _recipes.value.map {
            if (it.id == recipe.id) it.copy(isFavorite = !it.isFavorite) else it
        }
    }
}
