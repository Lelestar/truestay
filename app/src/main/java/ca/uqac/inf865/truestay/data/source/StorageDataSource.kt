package ca.uqac.inf865.truestay.data.source

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageDataSource @Inject constructor(
    private val storage: FirebaseStorage
) {
    suspend fun uploadImage(uri: Uri, path: String): String {
        val fileName = "${UUID.randomUUID()}.jpg"
        val ref = storage.reference.child("$path/$fileName")
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }

    suspend fun uploadImages(uris: List<Uri>, path: String): List<String> {
        return uris.map { uploadImage(it, path) }
    }

    suspend fun deleteImage(url: String) {
        try {
            val ref = storage.getReferenceFromUrl(url)
            ref.delete().await()
        } catch (e: Exception) {
            // Image might not exist
        }
    }
}