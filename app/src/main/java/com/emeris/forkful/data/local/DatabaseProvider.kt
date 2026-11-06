package com.emeris.forkful.data.local

/**
 * Forkful - Room Database Singleton Factory
 * Module: PROG7314 Portfolio of Evidence (POE)
 * Group Members:
 * - Jamie Abrahams (ST10434047)
 * - Isabelle Devlin (ST10445500)
 * - Justin Fussell (ST10280758)
 * - Matteo Nusca (ST10440432)
 *
 * Thread-safe provider initializing and supplying the Room AppDatabase instance.
 */

import android.content.Context
import androidx.room.Room

object DatabaseProvider {

    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "forkful_database"
            ).build()
            INSTANCE = instance
            instance
        }
    }
}

//------------------------------End-Of-File------------------------------//
