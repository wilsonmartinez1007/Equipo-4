package com.univalle.inventory.di

import android.content.Context
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.univalle.inventory.data.InventoryDB
import com.univalle.inventory.data.InventoryDao
import com.univalle.inventory.data.repository.WidgetRepository  // ← CAMBIO AQUÍ
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideRoomDatabase(
        @ApplicationContext context: Context
    ): InventoryDB {
        return Room.databaseBuilder(
            context,
            InventoryDB::class.java,
            "inventory_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideInventoryDao(db: InventoryDB): InventoryDao {
        return db.inventoryDao()
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideWidgetRepository(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore,
        dao: InventoryDao
    ): WidgetRepository {
        return WidgetRepository(
            firebaseAuth = firebaseAuth,
            firestore = firestore,
            localDao = dao
        )
    }


    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context = context
}