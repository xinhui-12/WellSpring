package my.edu.tarc.bait2073mobileassignment.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import my.edu.tarc.bait2073mobileassignment.ui.bmi.BMI

@Database(entities = [BMI::class], version = 1, exportSchema = false)
abstract class BMIDatabase: RoomDatabase() {
    abstract fun bmiDao():BMIDao

    companion object{
        @Volatile
        private var INSTANCE: BMIDatabase? = null

        fun getDatabase(context: Context):BMIDatabase{
            val tempInstance = INSTANCE
            if(tempInstance != null){
                return tempInstance
            }
            synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BMIDatabase::class.java,
                    "bmi_db"
                ).build()

                INSTANCE = instance
                return instance
            }
        }
    }

}