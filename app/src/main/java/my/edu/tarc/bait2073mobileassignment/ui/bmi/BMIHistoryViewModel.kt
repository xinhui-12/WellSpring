package my.edu.tarc.bait2073mobileassignment.ui.bmi

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import my.edu.tarc.bait2073mobileassignment.database.BMIDatabase
import my.edu.tarc.bait2073mobileassignment.database.BMIRepository

class BMIHistoryViewModel(application: Application): AndroidViewModel(application) {
    //LiveData gives updated contacts when changed
    var bmiList : LiveData<List<BMI>>
    private val repository: BMIRepository

    init {
        val bmiDao = BMIDatabase.getDatabase(application).bmiDao()
        repository = BMIRepository(bmiDao)
        bmiList = repository.allBMI
    }

    fun addBMI(bmi: BMI) = viewModelScope.launch { repository.add(bmi) }
    fun updateBMI(bmi: BMI) = viewModelScope.launch { repository.update(bmi) }
    fun deleteBMI(bmi: BMI) = viewModelScope.launch { repository.delete(bmi) }
    fun getBMI(position: Int): BMI?{
        val currentList = bmiList.value
        return if (currentList!=null && position >=0 && position<currentList.size) currentList[position] else null
    }
}