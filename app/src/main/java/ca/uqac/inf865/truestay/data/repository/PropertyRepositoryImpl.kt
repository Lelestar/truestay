package ca.uqac.inf865.truestay.data.repository

import ca.uqac.inf865.truestay.data.model.PropertyDto
import ca.uqac.inf865.truestay.data.model.toDto
import ca.uqac.inf865.truestay.data.model.toDomain
import ca.uqac.inf865.truestay.data.source.FirestoreDataSource
import ca.uqac.inf865.truestay.domain.model.Property
import ca.uqac.inf865.truestay.domain.model.PropertyFilters
import ca.uqac.inf865.truestay.domain.model.PropertyStatus
import ca.uqac.inf865.truestay.domain.model.RoomType
import ca.uqac.inf865.truestay.domain.repository.PropertyRepository
import ca.uqac.inf865.truestay.domain.repository.StorageRepository
import javax.inject.Inject

class PropertyRepositoryImpl @Inject constructor(
    private val firestoreDataSource: FirestoreDataSource,
    private val storageRepository: StorageRepository
) : PropertyRepository {

    override suspend fun getProperties(): Result<List<Property>> {
        return try {
            val properties = firestoreDataSource.getDocuments("properties", PropertyDto::class.java)
                .map { it.toDomain() }
            Result.success(properties)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPropertyById(id: String): Result<Property> {
        return try {
            android.util.Log.d("PropertyRepo", "getPropertyById called with id=$id")
            val property = firestoreDataSource.getDocument("properties", id, PropertyDto::class.java)
            android.util.Log.d("PropertyRepo", "Document retrieved: ${property?.id}, name=${property?.name}")
            if (property == null) {
                android.util.Log.e("PropertyRepo", "Property not found for id=$id")
                return Result.failure(Exception("Property not found"))
            }
            val domainProperty = property.toDomain()
            android.util.Log.d("PropertyRepo", "Property converted to domain: id=${domainProperty.id}, name=${domainProperty.name}, rooms=${domainProperty.rooms.size}, photos=${domainProperty.photos.size}")
            Result.success(domainProperty)
        } catch (e: Exception) {
            android.util.Log.e("PropertyRepo", "Exception in getPropertyById: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun getPropertiesByLandlord(landlordId: String): Result<List<Property>> {
        return try {
            val properties = firestoreDataSource.queryDocuments(
                "properties",
                "landlordId",
                landlordId,
                PropertyDto::class.java
            ).map { it.toDomain() }
            Result.success(properties)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addProperty(property: Property): Result<String> {
        return try {
            android.util.Log.d("PropertyRepo", "addProperty called with id=${property.id}")
            val propertyDto = property.toDto().copy(
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            // Use updateDocument which actually does a set() to keep the ID
            firestoreDataSource.updateDocument("properties", property.id, propertyDto)
            android.util.Log.d("PropertyRepo", "Property added with id=${property.id}")
            Result.success(property.id)
        } catch (e: Exception) {
            android.util.Log.e("PropertyRepo", "Error addProperty: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun updateProperty(property: Property): Result<Unit> {
        return try {
            val propertyDto = property.toDto().copy(updatedAt = System.currentTimeMillis())
            firestoreDataSource.updateDocument("properties", property.id, propertyDto)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteProperty(propertyId: String): Result<Unit> {
        return try {
            // First, get the property to retrieve photo URLs
            val propertyResult = getPropertyById(propertyId)

            propertyResult.onSuccess { property ->
                // Delete all photos from storage if any exist
                if (property.photos.isNotEmpty()) {
                    storageRepository.deleteImages(property.photos)
                        .onFailure { exception ->
                            android.util.Log.e("PropertyRepo", "Error deleting photos: ${exception.message}")
                            // Continue with property deletion even if photo deletion fails
                        }
                }
            }

            // Delete the property document from Firestore
            firestoreDataSource.deleteDocument("properties", propertyId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchProperties(filters: PropertyFilters): Result<List<Property>> {
        return try {
            // 1. Get all properties
            var properties = firestoreDataSource.getDocuments("properties", PropertyDto::class.java)
                .map { it.toDomain() }
                .filter { it.status == PropertyStatus.PUBLISHED } // Only published properties

            // 2. Apply filters locally
            properties = properties.filter { property ->
                applyFilters(property, filters)
            }

            Result.success(properties)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun applyFilters(property: Property, filters: PropertyFilters): Boolean {
        // Filter: Availability
        if (filters.availableOnly && !property.isAvailable) {
            return false
        }

        // Filter: Minimum Price
        if (filters.minPrice != null && property.monthlyRent < filters.minPrice) {
            return false
        }

        // Filter: Maximum Price
        if (filters.maxPrice != null && property.monthlyRent > filters.maxPrice) {
            return false
        }

        // Filter: Minimum Surface
        if (filters.minSurface != null && property.surface < filters.minSurface) {
            return false
        }

        // Filter: Maximum Surface
        if (filters.maxSurface != null && property.surface > filters.maxSurface) {
            return false
        }

        // Filter: Number of bedrooms
        if (filters.bedroomCounts.isNotEmpty()) {
            val bedroomCount = property.rooms.count { it.type == RoomType.BEDROOM }
            val matchesRoomCount = filters.bedroomCounts.any { it.matches(bedroomCount) }
            if (!matchesRoomCount) {
                return false
            }
        }

        // Filter: Minimum average rating
        if (filters.minRating != null && property.ratings.propertyAverageRating < filters.minRating) {
            return false
        }

        // Filter: Geographical area
        if (filters.geoBounds != null) {
            val lat = property.address.latitude
            val lng = property.address.longitude

            val inBounds = lat <= filters.geoBounds.northEast.latitude &&
                    lat >= filters.geoBounds.southWest.latitude &&
                    lng <= filters.geoBounds.northEast.longitude &&
                    lng >= filters.geoBounds.southWest.longitude

            if (!inBounds) {
                return false
            }
        }

        // Filter: Text search
        if (!filters.searchQuery.isNullOrBlank()) {
            val query = filters.searchQuery.lowercase()
            val matchesSearch = property.address.city.lowercase().contains(query) ||
                    property.address.street.lowercase().contains(query) ||
                    property.name.lowercase().contains(query) ||
                    property.description.lowercase().contains(query)

            if (!matchesSearch) {
                return false
            }
        }

        return true
    }
}
