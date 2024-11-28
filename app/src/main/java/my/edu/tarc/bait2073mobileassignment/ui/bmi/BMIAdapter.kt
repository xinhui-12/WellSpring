package my.edu.tarc.bait2073mobileassignment.ui.bmi

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import my.edu.tarc.bait2073mobileassignment.databinding.FragmentBmiHistoryRecordBinding


class BMIAdapter(private val bmiLiveData: LiveData<List<BMI>>): RecyclerView.Adapter<BMIAdapter.ViewHolder>() {
    private var dataSet = emptyList<BMI>()

    class ViewHolder(val binding: FragmentBmiHistoryRecordBinding): RecyclerView.ViewHolder(binding.root)

    internal fun setBMI(bmi: List<BMI>){
        dataSet = bmi
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = FragmentBmiHistoryRecordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return dataSet!!.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bmi = dataSet?.get(position)
        val bmiResult = "%.2f".format(bmi!!.result)
        holder.binding.bmiValue.text = bmiResult
        holder.binding.dateLabel.text = bmi.getFormattedDateOnly()
        holder.binding.heightValue.text = bmi.height.toInt().toString()
        holder.binding.weightValue.text = bmi.weight.toInt().toString()
    }
}