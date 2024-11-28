package my.edu.tarc.bait2073mobileassignment.ui.bmi

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.Firebase
import com.google.firebase.database.database
import my.edu.tarc.bait2073mobileassignment.R
import my.edu.tarc.bait2073mobileassignment.databinding.FragmentBmiResultBinding

class BMIResultFragment : Fragment() {

    private var _binding: FragmentBmiResultBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var profSharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBmiResultBinding.inflate(inflater,container,false)
        return binding.root
    } // end of onCreateView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        readPreferences()

        binding.linkNutrition.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_BMI_result_to_nutritionAdviceFragment)
        }

        binding.btnReCalculateBMI.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_BMI_result_to_navigation_BMI)
        }

        binding.btnBMIHistory.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_BMI_result_to_navigation_BMI_history)
        }

        binding.saveBtn.setOnClickListener {
            saveResult()
        }
    } // end of onViewCreated

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    } // end of onDestroyView

    private fun readPreferences(){
        sharedPreferences = requireActivity().getSharedPreferences("bmi_pref", Context.MODE_PRIVATE)
        val result = sharedPreferences.getFloat("result", 0.0f)

        binding.txtBMIResultNumber.text = "%.2f".format(result)
        bmiResult(result)
    }

    private fun bmiResult(result: Float){
        val color: Int
        val text: String

        if(result < 18.5){
            color = requireContext().getColor(R.color.bmi_blue_underweight)
            text = getString(R.string.underweight)
        }else if(result < 25){
            color = requireContext().getColor(R.color.bmi_blue_normal)
            text = getString(R.string.normal)
        }else if(result < 30){
            color = requireContext().getColor(R.color.bmi_blue_overweight)
            text = getString(R.string.overweight)
        }else if(result < 35){
            color = requireContext().getColor(R.color.bmi_blue_obese)
            text = getString(R.string.obese)
        }else{
            color = requireContext().getColor(R.color.bmi_blue_extremely_obese)
            text = getString(R.string.extremely_obese)
        }

        binding.txtBMIResultWord.setTextColor(color)
        binding.txtBMIResultWord.text = text
    }

    private fun saveResult(){
        profSharedPreferences = requireActivity().getSharedPreferences("prof_pref", Context.MODE_PRIVATE)
        val profPhone = profSharedPreferences.getString("phone", "")

        val bmiValue = BMI(
            sharedPreferences.getFloat("height", 0.0f),
            sharedPreferences.getFloat("weight", 0.0f),
            sharedPreferences.getFloat("result", 0.0f),
            0
        )
        sharedPreferences.getString("datetime", "01/01/2000 00:00:00")
            ?.let { bmiValue.setFormattedDateTime(it) }

        // update the profile preference
        with(profSharedPreferences.edit()) {
            putString("gender", sharedPreferences.getString("gender", null))
            putInt("age", sharedPreferences.getInt("age", 0))
            putFloat("height", bmiValue.height)
            putFloat("weight", bmiValue.weight)
            apply()
        }
        saveResultToDatabase(profPhone!!, bmiValue)

    }

    private fun saveResultToDatabase(profPhone: String, bmiValue: BMI){
        if (profPhone.isNotEmpty()) {
            val database = Firebase.database.reference
            database.child("user").child(profPhone).child("bmiHistory")
                .child(bmiValue.getFormattedDateTime().replace("/", "")).setValue(bmiValue)
            val profRef = database.child("user").child(profPhone)
            val newData = HashMap<String,Any>()
            newData["gender"] = profSharedPreferences.getString("gender", null)!!
            newData["age"] = profSharedPreferences.getInt("age", 0)
            newData["height"] = profSharedPreferences.getFloat("height", 0.0f)
            newData["weight"] = profSharedPreferences.getFloat("weight", 0.0f)
            profRef.updateChildren(newData)
                .addOnSuccessListener {
                    Toast.makeText(context, "BMI result saved", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Log.e("Failed Saved", "Error updating data: ${it.message}")
                }
        }
    }
}