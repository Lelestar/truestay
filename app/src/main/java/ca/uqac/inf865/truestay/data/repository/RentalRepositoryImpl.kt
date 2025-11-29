package ca.uqac.inf865.truestay.data.repository

import ca.uqac.inf865.truestay.data.model.RentalDto
import ca.uqac.inf865.truestay.data.model.toDto
import ca.uqac.inf865.truestay.data.model.toDomain
import ca.uqac.inf865.truestay.data.source.FirestoreDataSource
import ca.uqac.inf865.truestay.domain.model.Rental
import ca.uqac.inf865.truestay.domain.model.RentalStatus
import ca.uqac.inf865.truestay.domain.repository.RentalRepository
import javax.inject.Inject

class RentalRepositoryImpl @Inject constructor(
    private val firestoreDataSource: FirestoreDataSource
) : RentalRepository {

    override suspend fun createRental(rental: Rental): Result<String> {
        return try {
            val rentalDto = rental.toDto().copy(createdAt = System.currentTimeMillis())
            val id = firestoreDataSource.addDocument("rentals", rentalDto)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRentalById(id: String): Result<Rental> {
        return try {
            val rental = firestoreDataSource.getDocument("rentals", id, RentalDto::class.java)
                ?: return Result.failure(Exception("Rental not found"))
            Result.success(rental.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRentalsByTenant(tenantId: String): Result<List<Rental>> {
        return try {
            val rentals = firestoreDataSource.queryDocuments(
                "rentals",
                "tenantId",
                tenantId,
                RentalDto::class.java
            ).map { it.toDomain() }
            Result.success(rentals)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRentalsByLandlord(landlordId: String): Result<List<Rental>> {
        return try {
            val rentals = firestoreDataSource.queryDocuments(
                "rentals",
                "landlordId",
                landlordId,
                RentalDto::class.java
            ).map { it.toDomain() }
            Result.success(rentals)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateRental(rental: Rental): Result<Unit> {
        return try {
            firestoreDataSource.updateDocument("rentals", rental.id, rental.toDto())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateRentalStatus(rentalId: String, newStatus: RentalStatus): Result<Unit> {
        return try {
            firestoreDataSource.updateField(
                collection = "rentals",
                documentId = rentalId,
                field = "status",
                value = newStatus.name
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateRentalId(rentalId: String): Result<Unit> {
        return try {
            firestoreDataSource.updateField(
                collection = "rentals",
                documentId = rentalId,
                field = "id",
                value = rentalId
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


}