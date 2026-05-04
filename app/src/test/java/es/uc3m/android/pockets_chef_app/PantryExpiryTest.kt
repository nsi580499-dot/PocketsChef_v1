package es.uc3m.android.pockets_chef_app

import es.uc3m.android.pockets_chef_app.data.model.PantryItem
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PantryExpiryTest {

    // Test 1: item expiring in 2 days is flagged as expiring soon
    @Test
    fun pantryItem_isExpiringSoon_whenExpiresInTwoDays() {
        val twoDaysMs = System.currentTimeMillis() + 2 * 24 * 60 * 60 * 1000L
        val item = PantryItem(
            id = "1",
            name = "Milk",
            quantity = "1",
            unit = "L",
            category = "Dairy",
            expiryDate = twoDaysMs,
            isExpiringSoon = true
        )

        assertTrue(item.isExpiringSoon)
    }

    // Test 2: item expiring in 10 days is NOT flagged as expiring soon
    @Test
    fun pantryItem_isNotExpiringSoon_whenExpiresInTenDays() {
        val tenDaysMs = System.currentTimeMillis() + 10 * 24 * 60 * 60 * 1000L
        val item = PantryItem(
            id = "2",
            name = "Cheese",
            quantity = "200",
            unit = "g",
            category = "Dairy",
            expiryDate = tenDaysMs,
            isExpiringSoon = false
        )

        assertFalse(item.isExpiringSoon)
    }

    // Test 3: item in the past is expired
    @Test
    fun pantryItem_isExpired_whenExpiryDateIsInThePast() {
        val yesterday = System.currentTimeMillis() - 24 * 60 * 60 * 1000L
        val item = PantryItem(
            id = "3",
            name = "Yogurt",
            quantity = "1",
            unit = "unit",
            category = "Dairy",
            expiryDate = yesterday,
            isExpiringSoon = true
        )

        val isExpired = item.expiryDate < System.currentTimeMillis()
        assertTrue(isExpired)
    }
}
