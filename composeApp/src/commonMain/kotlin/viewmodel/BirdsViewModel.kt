package viewmodel

import dev.icerock.moko.mvvm.viewmodel.ViewModel
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import model.BirdImage

data class BirdsUIState(val images: List<BirdImage>, val selectedCategory: String? = null) {
    val categories: Set<String> = images.map { it.category }.toSet()
    val selectedImages: List<BirdImage> = images.filter { it.category == selectedCategory }
}

class BirdsViewModel : ViewModel() {
    private val _uiState: MutableStateFlow<BirdsUIState> =
        MutableStateFlow(BirdsUIState(emptyList()))
    val uiState: StateFlow<BirdsUIState> = _uiState.asStateFlow()

    private val httpClient: HttpClient = HttpClient() {
        install(ContentNegotiation) {
            json()
        }
    }

    fun selectCategory(category: String) {
        _uiState.update { state ->
            if (state.selectedCategory == category) {
                state.copy(selectedCategory = null)
            } else {
                state.copy(selectedCategory = category)
            }
        }
    }

    fun updateImages() {
        viewModelScope.launch {
            val images: List<BirdImage> = getImages()
            _uiState.update {
                it.copy(images = images)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        httpClient.close()
    }

    private suspend fun getImages(): List<BirdImage> =
        httpClient
            .get("https://sebi.io/demo-image-api/pictures.json")
            .body<List<BirdImage>>()
}