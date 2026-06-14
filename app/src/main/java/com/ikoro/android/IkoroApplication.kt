package com.ikoro.android

import android.content.Context
import android.app.Application
import androidx.room.Room
import com.ikoro.android.identity.IdentityManager
import com.ikoro.android.persistence.IkoroDatabase

/**
 * Main application class for Ikoro Android.
 */
class IkoroApplication : Application() {
    
    lateinit var database: IkoroDatabase
    lateinit var identityManager: IdentityManager
    
    override fun onCreate() {
        super.onCreate()

        database = try {
            Room.databaseBuilder(
                applicationContext,
                IkoroDatabase::class.java,
                "ikoro_database"
            )
                .fallbackToDestructiveMigration()
                .build()
        } catch (e: Exception) {
            // If Room fails (corrupted DB, migration issue), delete and recreate.
            applicationContext.deleteDatabase("ikoro_database")
            Room.databaseBuilder(
                applicationContext,
                IkoroDatabase::class.java,
                "ikoro_database"
            )
                .fallbackToDestructiveMigration()
                .build()
        }

        identityManager = try {
            IdentityManager(applicationContext)
        } catch (e: Exception) {
            // IdentityManager may fail if stored keys are corrupt. Reset storage.
            applicationContext.getSharedPreferences("identity", Context.MODE_PRIVATE).edit().clear().apply()
            IdentityManager(applicationContext)
        }
    }
}
