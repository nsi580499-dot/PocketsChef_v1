package es.uc3m.android.pockets_chef_app.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.util.UUID

object ImageUploadHelper {

    private fun getStorage() = FirebaseStorage.getInstance().reference

    suspend fun uploadBitmap(bitmap: Bitmap, folder: String): Result<String> = try {
        val ref = getStorage().child("$folder/${UUID.randomUUID()}.jpg")
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, baos)
        ref.putBytes(baos.toByteArray()).await()
        val url = ref.downloadUrl.await().toString()
        Result.success(url)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun uploadUri(context: Context, uri: Uri, folder: String): Result<String> = try {
        val ref = getStorage().child("$folder/${UUID.randomUUID()}.jpg")
        ref.putFile(uri).await()
        val url = ref.downloadUrl.await().toString()
        Result.success(url)
    } catch (e: Exception) {
        Result.failure(e)
    }
}