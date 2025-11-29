package ca.uqac.inf865.truestay.di

import android.util.Log
import ca.uqac.inf865.truestay.BuildConfig
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    private const val TAG = "FirebaseModule"

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance().apply {
            if (BuildConfig.USE_EMULATORS) {
                Log.d(TAG, "🔧 Using Auth Emulator at ${BuildConfig.EMULATOR_HOST}:9099")
                useEmulator(BuildConfig.EMULATOR_HOST, 9099)
            } else {
                Log.d(TAG, "☁️ Using Production Firebase Auth")
            }
        }
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance().apply {
            if (BuildConfig.USE_EMULATORS) {
                Log.d(TAG, "🔧 Using Firestore Emulator at ${BuildConfig.EMULATOR_HOST}:8080")
                useEmulator(BuildConfig.EMULATOR_HOST, 8080)
            } else {
                Log.d(TAG, "☁️ Using Production Firestore")
            }
        }
    }

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance().apply {
            if (BuildConfig.USE_EMULATORS) {
                Log.d(TAG, "🔧 Using Storage Emulator at ${BuildConfig.EMULATOR_HOST}:9199")
                useEmulator(BuildConfig.EMULATOR_HOST, 9199)
            } else {
                Log.d(TAG, "☁️ Using Production Firebase Storage")
            }
        }
    }

    @Provides
    @Singleton
    fun provideFirebaseFunctions(): FirebaseFunctions {
        return FirebaseFunctions.getInstance().apply {
            if (BuildConfig.USE_EMULATORS) {
                Log.d(TAG, "🔧 Using Functions Emulator at ${BuildConfig.EMULATOR_HOST}:5001")
                useEmulator(BuildConfig.EMULATOR_HOST, 5001)
            } else {
                Log.d(TAG, "☁️ Using Production Firebase Functions")
            }
        }
    }
}