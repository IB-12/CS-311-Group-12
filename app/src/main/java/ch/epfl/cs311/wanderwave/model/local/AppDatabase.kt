package ch.epfl.cs311.wanderwave.model.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [TrackEntity::class , BeaconEntity ::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
  abstract fun trackDao(): TrackDao
  abstract fun beaconDao(): BeaconDao
}
