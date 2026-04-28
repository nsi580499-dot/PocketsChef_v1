package es.uc3m.android.pockets_chef_app.ui.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import java.net.URL

data class Supermarket(
    val name: String,
    val latLng: LatLng,
    val address: String = ""
)

class MapViewModel(application: Application) : AndroidViewModel(application) {

    var userLocation by mutableStateOf<LatLng?>(null)
        private set

    var supermarkets by mutableStateOf<List<Supermarket>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    private val fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(application)

    @SuppressLint("MissingPermission")
    fun loadLocationAndSupermarkets(apiKey: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val location: Location? = fusedLocationClient.lastLocation.await()
                val latLng = if (location != null) {
                    LatLng(location.latitude, location.longitude)
                } else {
                    // Fallback to UC3M Leganés campus
                    LatLng(40.3310, -3.7645)
                }
                userLocation = latLng
                fetchNearbySupermarkets(latLng, apiKey)
            } catch (e: Exception) {
                errorMessage = "Could not get location"
                isLoading = false
            }
        }
    }

    private suspend fun fetchNearbySupermarkets(location: LatLng, apiKey: String) {
        try {
            val url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                    "?location=${location.latitude},${location.longitude}" +
                    "&radius=1500" +
                    "&type=supermarket" +
                    "&key=$apiKey"

            val response = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                URL(url).readText()
            }
            val json = JSONObject(response)
            val results = json.getJSONArray("results")

            val list = mutableListOf<Supermarket>()
            for (i in 0 until results.length()) {
                val place = results.getJSONObject(i)
                val name = place.getString("name")
                val geometry = place.getJSONObject("geometry").getJSONObject("location")
                val lat = geometry.getDouble("lat")
                val lng = geometry.getDouble("lng")
                val address = place.optString("vicinity", "")
                list.add(Supermarket(name = name, latLng = LatLng(lat, lng), address = address))
            }
            supermarkets = list
        } catch (e: Exception) {
            errorMessage = "Could not load supermarkets"
        } finally {
            isLoading = false
        }
    }
}