package my.edu.tarc.bait2073mobileassignment.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import my.edu.tarc.bait2073mobileassignment.ui.bmi.BMI

@Dao
interface BMIDao {
    @Query("SELECT * FROM bmi ORDER BY dateTimeInMillis DESC")
    fun getAllBMI(): LiveData<List<BMI>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(bmi: BMI)

    @Delete
    suspend fun delete(bmi: BMI)

    @Update
    suspend fun update(bmi: BMI)
}