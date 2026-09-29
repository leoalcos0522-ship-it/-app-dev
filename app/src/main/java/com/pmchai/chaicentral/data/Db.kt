package com.pmchai.chaicentral.data

import android.content.Context
import androidx.room.*

@Database(entities = [Product::class, Sale::class], version = 1, exportSchema = false)
abstract class ChaiDb : RoomDatabase() {
    abstract fun dao(): ChaiDao

    companion object {
        @Volatile private var inst: ChaiDb? = null
        fun get(ctx: Context): ChaiDb = inst ?: synchronized(this) {
            inst ?: Room.databaseBuilder(ctx.applicationContext, ChaiDb::class.java, "chai.db")
                .build().also { inst = it }
        }
    }
}
