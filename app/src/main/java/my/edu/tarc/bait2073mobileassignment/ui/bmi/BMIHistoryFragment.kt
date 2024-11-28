package my.edu.tarc.bait2073mobileassignment.ui.bmi

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.Firebase
import com.google.firebase.database.database
import com.jjoe64.graphview.DefaultLabelFormatter
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import my.edu.tarc.bait2073mobileassignment.databinding.FragmentBmiHistoryBinding

class BMIHistoryFragment : Fragment(){

    private var _binding: FragmentBmiHistoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPreferences: SharedPreferences
    private val bmiHistoryViewModel: BMIHistoryViewModel by activityViewModels()
    private lateinit var lineGraphView: GraphView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBmiHistoryBinding.inflate(inflater,container,false)
        return binding.root
    } // end of onCreateView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireActivity().getSharedPreferences("prof_pref", Context.MODE_PRIVATE)
        val userPhone = sharedPreferences.getString("phone", null)
        if (userPhone != null) {
            lifecycleScope.launch {
                fetchData(userPhone)
            }
        }
        // Create adapter with LiveData from ViewModel
        val adapter = BMIAdapter(bmiHistoryViewModel.bmiList)
        bmiHistoryViewModel.bmiList.observe(viewLifecycleOwner){
            adapter.setBMI(it)
        }
        binding.recycleBMIListView.adapter = adapter

        lineGraphView = binding.bmiGraphView
        setupLineGraph()

        binding.closeBtn.setOnClickListener {
            findNavController().popBackStack()
        }
    } // end of onViewCreated

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    } // end of onDestroyView

    private suspend fun fetchData(userPhone: String){
        val database = Firebase.database.reference.child("user").child(userPhone).child("bmiHistory")
        try {
            val snapshot = database.get().await()
            for (dataSnapshot in snapshot.children) {
                val height = dataSnapshot.child("height").getValue(Float::class.java) ?: 0f
                val weight = dataSnapshot.child("weight").getValue(Float::class.java) ?: 0f
                val result = dataSnapshot.child("result").getValue(Float::class.java) ?: 0f
                val dateTimeInMillis =
                    dataSnapshot.child("dateTimeInMillis").getValue(Long::class.java) ?: 0L

                val bmi = BMI(height, weight, result, dateTimeInMillis)
                bmiHistoryViewModel.addBMI(bmi)
            }
        } catch (e: Exception) {
            Log.e("FirebaseData", "Error fetching data from Firebase: ${e.message}")
        }
    }

    private fun setupLineGraph(){
        bmiHistoryViewModel.bmiList.observe(viewLifecycleOwner) { bmiList ->
            val sortedList = bmiList.sortedBy{ it.dateTimeInMillis }
            if (sortedList.size >= 2) {
                // Create a LineGraphSeries
                val series = LineGraphSeries<DataPoint>()
                for ((index, bmi) in sortedList.withIndex()) {
                    series.appendData(
                        DataPoint(index.toDouble(), bmi.result.toDouble()), true, sortedList.size
                    )
                    series.color = Color.BLUE
                    series.thickness = 5 // Customize line thickness
                }

                // Add the series to the graph and customize its appearance
                lineGraphView.addSeries(series)

                // Set x-axis labels based on formatted dates from sortedList
                lineGraphView.gridLabelRenderer.labelFormatter = object : DefaultLabelFormatter() {
                    override fun formatLabel(value: Double, isValueX: Boolean): String {
                        return if (isValueX) {
                            val index = value.toInt()
                            val date = sortedList[index].getFormattedDateOnly()
                            date
                        } else {
                            super.formatLabel(value, isValueX)
                        }
                    }
                }
                lineGraphView.gridLabelRenderer.numHorizontalLabels = sortedList.size
                lineGraphView.gridLabelRenderer.setHorizontalLabelsAngle(90)
                lineGraphView.gridLabelRenderer.labelHorizontalHeight = 180
                lineGraphView.gridLabelRenderer.labelsSpace = 20
                lineGraphView.gridLabelRenderer.textSize = 32.0f


                val lowestY = sortedList.minByOrNull { it.result }!!.result
                // Set the minimum Y-axis value to the lowestY
                lineGraphView.viewport.isYAxisBoundsManual = true
                lineGraphView.viewport.setMinY(lowestY.toDouble() - (lowestY % 5)) // Adjust the offset as needed
                lineGraphView.viewport.setMaxY(40.0)
                binding.txtLineGraph.visibility = View.GONE
                binding.bmiGraphView.visibility = View.VISIBLE
            }else{
                binding.txtLineGraph.visibility = View.VISIBLE
                binding.bmiGraphView.visibility = View.INVISIBLE
            }
        }
    }
}