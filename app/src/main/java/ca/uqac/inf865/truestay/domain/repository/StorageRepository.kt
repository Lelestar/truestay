package ca.uqac.inf865.truestay.domain.repository

import android.net.Uri

interface StorageRepository {
    suspend fun uploadImage(uri: Uri, path: String): Result<String>
    suspend fun uploadImages(uris: List<Uri>, path: String): Result<List<String>>
    suspend fun deleteImage(url: String): Result<Unit>
    suspend fun deleteImages(urls: List<String>): Result<Unit>
}