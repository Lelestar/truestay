package ca.uqac.inf865.truestay.data.repository

import android.net.Uri
import ca.uqac.inf865.truestay.data.source.StorageDataSource
import ca.uqac.inf865.truestay.domain.repository.StorageRepository
import javax.inject.Inject

class StorageRepositoryImpl @Inject constructor(
    private val storageDataSource: StorageDataSource
) : StorageRepository {

    override suspend fun uploadImage(uri: Uri, path: String): Result<String> {
        return try {
            val url = storageDataSource.uploadImage(uri, path)
            Result.success(url)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun uploadImages(uris: List<Uri>, path: String): Result<List<String>> {
        return try {
            val urls = storageDataSource.uploadImages(uris, path)
            Result.success(urls)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteImage(url: String): Result<Unit> {
        return try {
            storageDataSource.deleteImage(url)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteImages(urls: List<String>): Result<Unit> {
        return try {
            urls.forEach { storageDataSource.deleteImage(it) }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}