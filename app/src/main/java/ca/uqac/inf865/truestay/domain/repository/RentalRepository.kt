package ca.uqac.inf865.truestay.domain.repository

import ca.uqac.inf865.truestay.domain.model.Rental
import ca.uqac.inf865.truestay.domain.model.RentalStatus

interface RentalRepository {
    suspend fun createRental(rental: Rental): Result<String>
    suspend fun getRentalById(id: String): Result<Rental>
    suspend fun getRentalsByTenant(tenantId: String): Result<List<Rental>>
    suspend fun getRentalsByLandlord(landlordId: String): Result<List<Rental>>
    suspend fun getRentalsByProperty(propertyId: String): Result<List<Rental>>
    suspend fun updateRental(rental: Rental): Result<Unit>
    suspend fun deleteRental(rentalId: String): Result<Unit>
}