package my.edu.tarc.bait2073mobileassignment.ui.bmi

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources.getColorStateList
import my.edu.tarc.bait2073mobileassignment.databinding.FragmentBmiBinding
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.findNavController
import my.edu.tarc.bait2073mobileassignment.R
import kotlin.math.pow

class BMIFragment : Fragment(), View.OnClickListener {

    private var _binding: FragmentBmiBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var sharedPreferences: SharedPreferences

    private var maleSelected = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBmiBinding.inflate(inflater, container, false)
        return binding.root
    } // end of onCreateView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.maleBtn.setOnClickListener(this)
        binding.femaleBtn.setOnClickListener(this)

        // the slider change, the height value will change at the same time
        binding.heightSlider.addOnChangeListener { _, value, _ ->
            // Responds to when slider's value is changed
            binding.editTextHeight.setText(value.toInt().toString())
        }

        // the number of the height change by user, the slider will change at the same time
        binding.editTextHeight.addTextChangedListener {
            if(binding.editTextHeight.text.isNotBlank()){
                binding.editTextHeight.setSelection(binding.editTextHeight.text.length)
                if(binding.editTextHeight.getText().toString().toInt() in 1..200 && binding.editTextHeight.text.isNotBlank()){
                    binding.heightSlider.value = binding.editTextHeight.getText().toString().toFloat()
                }
            }
        }

        readPreferences()

        // weight button minus
        binding.minusWeightBtn.setOnClickListener {
            var weight = binding.editTextWeight.text.toString().toInt()
            if(weight > 1){
                weight -= 1
                binding.editTextWeight.setText(weight.toString())
            }else {
                Toast.makeText(activity, "Weight value cannot be less than one", Toast.LENGTH_SHORT).show()
            }
        }

        // weight button plus
        binding.plusWeightBtn.setOnClickListener {
            var weight = binding.editTextWeight.text.toString().toInt()
            weight += 1
            binding.editTextWeight.setText(weight.toString())
        }

        // age button minus
        binding.minusAgeBtn.setOnClickListener {
            var age = binding.editTextAge.text.toString().toInt()
            if(age > 1){
                age -= 1
                binding.editTextAge.setText(age.toString())
            }else {
                Toast.makeText(activity, "Age value cannot be less than one", Toast.LENGTH_SHORT).show()
            }
        }

        // age button plus
        binding.plusAgeBtn.setOnClickListener {
            var age = binding.editTextAge.text.toString().toInt()
            age += 1
            binding.editTextAge.setText(age.toString())
        }

        binding.btnCalculateBMI.setOnClickListener{
            //Input validation
            val weight = binding.editTextWeight.text.toString().toFloatOrNull() ?: 0.0f
            val height = binding.editTextHeight.text.toString().toFloatOrNull() ?: 0.0f
            val age = binding.editTextAge.text.toString().toIntOrNull() ?: 0

            if(weight == 0.0f){
                binding.editTextWeight.error = getString(R.string.error_num_value)
                return@setOnClickListener
            }
            if(height == 0.0f){
                binding.editTextHeight.error = getString(R.string.error_num_value)
                return@setOnClickListener
            }
            if(age == 0){
                binding.editTextAge.error = getString(R.string.error_num_value)
                return@setOnClickListener
            }

            val result = weight / (height / 100).pow(2)
            val bmiData = BMI(height, weight, result, System.currentTimeMillis())

            writePreferences(bmiData, if(maleSelected) "male" else "female", age)

            findNavController().navigate(R.id.action_navigation_BMI_to_navigation_BMI_result)
        }

        binding.btnBMIHistory.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_BMI_to_navigation_BMI_history)
        }
    } // end of onViewCreated

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    } // end of onDestroy

    override fun onClick(v: View){
        if(v.id == binding.maleBtn.id){
            v.setBackgroundTintList(getColorStateList(v.context, R.color.bmi_btn_color_pressed))
            binding.femaleBtn.setBackgroundTintList(getColorStateList(binding.femaleBtn.context, R.color.purple_700))
            maleSelected = true
        }else{
            v.setBackgroundTintList(getColorStateList(v.context, R.color.bmi_btn_color_pressed))
            binding.maleBtn.setBackgroundTintList(getColorStateList(binding.maleBtn.context, R.color.purple_700))
            maleSelected = false
        }
    } // end of onClick

    private fun readPreferences(){
        // get the value from the prof preference
        sharedPreferences = requireActivity().getSharedPreferences("prof_pref", Context.MODE_PRIVATE)
        val height = sharedPreferences.getFloat("height",1.0f)
        val weight = sharedPreferences.getFloat("weight",1.0f)
        val age = sharedPreferences.getInt("age",0)
        val gender = sharedPreferences.getString("gender", "male")?.lowercase()

        // set the value in the UI
        binding.heightSlider.value = height
        binding.editTextHeight.setText(height.toInt().toString())
        binding.editTextWeight.setText(weight.toInt().toString())
        binding.editTextAge.setText(age.toString())
        if(gender == "male")
            binding.maleBtn.performClick()
        else
            binding.femaleBtn.performClick()
    }

    private fun writePreferences(bmiData: BMI, gender: String, age: Int){
        sharedPreferences = requireActivity().getSharedPreferences("bmi_pref",Context.MODE_PRIVATE)
        with(sharedPreferences.edit()){
            putFloat("height", bmiData.height)
            putFloat("weight", bmiData.weight)
            putFloat("result", bmiData.result)
            putString("datetime", bmiData.getFormattedDateTime())
            putString("gender", gender)
            putInt("age", age)
            apply()
        }
    }
}