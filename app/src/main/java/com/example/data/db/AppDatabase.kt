package com.example.data.db

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Room Database structure supporting local persistence of thesis slides, notes,
 * and presentation outlines. Fully compliant with modern Android development guidelines.
 */

@Entity(tableName = "thesis_slides")
data class ThesisSlide(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val category: String, // "Sendai", "Law 36.15", "Law 12.90", "FLCN", "General"
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface ThesisSlideDao {
    @Query("SELECT * FROM thesis_slides ORDER BY timestamp DESC")
    fun getAllSlides(): Flow<List<ThesisSlide>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSlide(slide: ThesisSlide)

    @Update
    suspend fun updateSlide(slide: ThesisSlide)

    @Query("DELETE FROM thesis_slides WHERE id = :id")
    suspend fun deleteSlideById(id: Int)

    @Query("DELETE FROM thesis_slides")
    suspend fun deleteAllSlides()
}

@Database(entities = [ThesisSlide::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun thesisSlideDao(): ThesisSlideDao
}

class ThesisRepository(private val dao: ThesisSlideDao) {
    val allSlides: Flow<List<ThesisSlide>> = dao.getAllSlides()

    suspend fun insert(slide: ThesisSlide) {
        dao.insertSlide(slide)
    }

    suspend fun update(slide: ThesisSlide) {
        dao.updateSlide(slide)
    }

    suspend fun deleteById(id: Int) {
        dao.deleteSlideById(id)
    }

    suspend fun clear() {
        dao.deleteAllSlides()
    }
}
