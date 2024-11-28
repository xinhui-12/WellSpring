package my.edu.tarc.bait2073mobileassignment.ui.bmi

import java.util.Date
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Locale

@Entity(tableName = "bmi")
data class BMI(
    var height: Float,
    var weight: Float,
    var result: Float,
    @PrimaryKey var dateTimeInMillis: Long
    ){
    fun getFormattedDateTime(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm:ss", Locale.getDefault())
        val dateTime = Date(dateTimeInMillis)
        return sdf.format(dateTime)
    }

    fun setFormattedDateTime(dateTimeString: String) {
        val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm:ss", Locale.getDefault())
        val dateTime = sdf.parse(dateTimeString)
        dateTimeInMillis = dateTime?.time ?: 0
    }

    fun getFormattedDateOnly(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val dateTime = Date(dateTimeInMillis)
        return sdf.format(dateTime)
    }
}
