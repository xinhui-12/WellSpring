package my.edu.tarc.bait2073mobileassignment.database

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import my.edu.tarc.bait2073mobileassignment.ui.bmi.BMI

class BMIRepository(private val bmiDao: BMIDao) {
    val allBMI: LiveData<List<BMI>> = bmiDao.getAllBMI()

    @WorkerThread
    suspend fun add(bmi: BMI){
        bmiDao.insert(bmi)
    }

    @WorkerThread
    suspend fun delete(bmi: BMI){
        bmiDao.delete(bmi)
    }

    @WorkerThread
    suspend fun update(bmi: BMI){
        bmiDao.update(bmi)
    }
}