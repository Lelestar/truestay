package ca.uqac.inf865.truestay.domain.repository

import ca.uqac.inf865.truestay.domain.model.Property
import ca.uqac.inf865.truestay.domain.model.PropertyFilters

interface PropertyRepository {
    suspend fun getProperties(): Result<List<Property>>
    suspend fun getPropertyById(id: String): Result<Property>
    suspend fun getPropertiesByLandlord(landlordId: String): Result<List<Property>>
    suspend fun addProperty(property: Property): Result<String>
    suspend fun updateProperty(property: Property): Result<Unit>
    suspend fun deleteProperty(propertyId: String): Result<Unit>
    suspend fun searchProperties(filters: PropertyFilters): Result<List<Property>>
}