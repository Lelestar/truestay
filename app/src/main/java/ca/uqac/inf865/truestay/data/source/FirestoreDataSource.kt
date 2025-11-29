package ca.uqac.inf865.truestay.data.source

import androidx.compose.animation.core.Spring
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    // Collections
    fun usersCollection() = firestore.collection("users")
    fun propertiesCollection() = firestore.collection("properties")
    fun rentalsCollection() = firestore.collection("rentals")
    fun inventoriesCollection() = firestore.collection("inventories")
    fun reviewsCollection() = firestore.collection("reviews")
    fun favoritesCollection() = firestore.collection("favorites")

    // Generic operations
    suspend fun <T> getDocument(collection: String, documentId: String, clazz: Class<T>): T? {
        return try {
            firestore.collection(collection)
                .document(documentId)
                .get()
                .await()
                .toObject(clazz)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun <T> getDocuments(collection: String, clazz: Class<T>): List<T> {
        return try {
            firestore.collection(collection)
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(clazz) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun <T> addDocument(collection: String, data: T): String {
        val docRef = firestore.collection(collection).document()
        docRef.set(data as Any).await()
        return docRef.id
    }

    suspend fun <T> updateDocument(collection: String, documentId: String, data: T) {
        firestore.collection(collection)
            .document(documentId)
            .set(data as Any)
            .await()
    }

    suspend fun deleteDocument(collection: String, documentId: String) {
        firestore.collection(collection)
            .document(documentId)
            .delete()
            .await()
    }

    suspend fun updateDocumentFields(
        collection: String,
        documentId: String,
        fields: Map<String, Any?>
    ) {
        firestore.collection(collection)
            .document(documentId)
            .update(fields)
            .await()
    }


    suspend fun <T> queryDocuments(
        collection: String,
        field: String,
        value: Any,
        clazz: Class<T>
    ): List<T> {
        return try {
            firestore.collection(collection)
                .whereEqualTo(field, value)
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(clazz) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Variant that includes document IDs - returns pairs of (documentId, object)
    suspend fun <T> queryDocumentsWithIds(
        collection: String,
        field: String,
        value: Any,
        clazz: Class<T>
    ): List<Pair<String, T>> {
        return try {
            firestore.collection(collection)
                .whereEqualTo(field, value)
                .get()
                .await()
                .documents
                .mapNotNull { document ->
                    document.toObject(clazz)?.let { obj ->
                        document.id to obj
                    }
                }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun updateField(
        collection: String,
        documentId : String,
        field : String,
        value: Any
    ) {
        firestore.collection(collection)
            .document(documentId)
            .update(field, value)
            .await()
    }
}