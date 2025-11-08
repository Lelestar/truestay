package ca.uqac.inf865.truestay.di

import ca.uqac.inf865.truestay.data.repository.AuthRepositoryImpl
import ca.uqac.inf865.truestay.data.repository.FavoriteRepositoryImpl
import ca.uqac.inf865.truestay.data.repository.InventoryRepositoryImpl
import ca.uqac.inf865.truestay.data.repository.GeocodingRepositoryImpl
import ca.uqac.inf865.truestay.data.repository.PropertyRepositoryImpl
import ca.uqac.inf865.truestay.data.repository.RentalRepositoryImpl
import ca.uqac.inf865.truestay.data.repository.ReviewRepositoryImpl
import ca.uqac.inf865.truestay.data.repository.StorageRepositoryImpl
import ca.uqac.inf865.truestay.data.repository.ThemePreferencesRepositoryImpl
import ca.uqac.inf865.truestay.data.repository.UserRepositoryImpl
import ca.uqac.inf865.truestay.domain.repository.AuthRepository
import ca.uqac.inf865.truestay.domain.repository.GeocodingRepository
import ca.uqac.inf865.truestay.domain.repository.FavoriteRepository
import ca.uqac.inf865.truestay.domain.repository.InventoryRepository
import ca.uqac.inf865.truestay.domain.repository.PropertyRepository
import ca.uqac.inf865.truestay.domain.repository.RentalRepository
import ca.uqac.inf865.truestay.domain.repository.ReviewRepository
import ca.uqac.inf865.truestay.domain.repository.StorageRepository
import ca.uqac.inf865.truestay.domain.repository.ThemePreferencesRepository
import ca.uqac.inf865.truestay.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindPropertyRepository(
        propertyRepositoryImpl: PropertyRepositoryImpl
    ): PropertyRepository

    @Binds
    @Singleton
    abstract fun bindRentalRepository(
        rentalRepositoryImpl: RentalRepositoryImpl
    ): RentalRepository

    @Binds
    @Singleton
    abstract fun bindInventoryRepository(
        inventoryRepositoryImpl: InventoryRepositoryImpl
    ): InventoryRepository

    @Binds
    @Singleton
    abstract fun bindReviewRepository(
        reviewRepositoryImpl: ReviewRepositoryImpl
    ): ReviewRepository

    @Binds
    @Singleton
    abstract fun bindFavoriteRepository(
        favoriteRepositoryImpl: FavoriteRepositoryImpl
    ): FavoriteRepository

    @Binds
    @Singleton
    abstract fun bindStorageRepository(
        storageRepositoryImpl: StorageRepositoryImpl
    ): StorageRepository

    @Binds
    @Singleton
    abstract fun bindThemePreferencesRepository(
        themePreferencesRepositoryImpl: ThemePreferencesRepositoryImpl
    ): ThemePreferencesRepository

    @Binds
    @Singleton
    abstract fun bindGeocodingRepository(
        geocodingRepositoryImpl: GeocodingRepositoryImpl
    ): GeocodingRepository
}
