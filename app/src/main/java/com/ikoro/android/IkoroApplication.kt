package com.ikoro.android

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
        
        database = Room.databaseBuilder(
            applicationContext,
            IkoroDatabase::class.java,
            "ikoro_database"
        ).build()
        
        identityManager = IdentityManager(applicationContext)
    }
}
