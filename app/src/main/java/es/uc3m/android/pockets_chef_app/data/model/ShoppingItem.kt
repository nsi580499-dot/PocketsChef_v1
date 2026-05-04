package es.uc3m.android.pockets_chef_app.data.model
import com.google.firebase.firestore.PropertyName
data class ShoppingItem(
    val id: String = "",
    val name: String = "",
    val amount: String = "",
    @get:PropertyName("isChecked")
    @set:PropertyName("isChecked")
    var isChecked: Boolean = false,
    val userId: String = ""
)