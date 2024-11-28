package my.edu.tarc.bait2073mobileassignment.ui.home

import android.Manifest
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import my.edu.tarc.bait2073mobileassignment.R
import my.edu.tarc.bait2073mobileassignment.databinding.FragmentStepCounterBinding

class StepCounterFragment : Fragment(), SensorEventListener {

    private var _binding: FragmentStepCounterBinding? = null
    private var sensorManager: SensorManager? = null

    private val binding get() = _binding!!
    private var running = false
    private var totalSteps = 0f
    private var previousTotalSteps = 0f
    private var calories = 0.00
    private var time = 0.00
    private var km = 0.00

    private lateinit var sharedPreferences: SharedPreferences
    private val databaseRef = Firebase.database.reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_DENIED){
            //ask for permission
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                123)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStepCounterBinding.inflate(inflater, container, false)

        loadData()
        resetSteps()
        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager

        sharedPreferences = requireActivity().getSharedPreferences("prof_pref", Context.MODE_PRIVATE)
        binding.textViewHello.text = getString(R.string.hello_user) + " " + sharedPreferences.getString("name", null)
        binding.progressCircular.progressMax = sharedPreferences.getInt("steps", 100).toFloat()
        binding.tvTotalMax.text = "/" + sharedPreferences.getInt("steps", 100).toString()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonHome.setOnClickListener {
            findNavController().navigate(R.id.navigation_home)
        }
    }

    override fun onResume() {
        super.onResume()
        running = true
        val stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if(stepSensor == null) {
            Toast.makeText(requireContext(), "No sensor detected on this device", Toast.LENGTH_SHORT).show()
        }
        else {
            sensorManager?.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_FASTEST)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if(running) {
            totalSteps = event!!.values[0]
            val currentSteps = totalSteps.toInt() - previousTotalSteps.toInt()
            binding.tvStepsTaken.text = ("$currentSteps")

            binding.progressCircular.apply {
                setProgressWithAnimation(currentSteps.toFloat())
            }

            val pace = binding.spinnerPace.selectedItemPosition
            if(pace == 0) {
                km = (1.7 * 0.414 * currentSteps.toFloat())
                time = km / 0.9
                calories = (time * 2.8 * 3.5 * 65) / (200 * 60)
            }
            else if(pace == 1) {
                km = (1.7 * 0.414 * currentSteps.toFloat())
                time = km / 1.34
                calories = (time * 3.5 * 3.5 * 65) / (200 * 60)
            }
            else if(pace == 2) {
                km = (1.7 * 0.414 * currentSteps.toFloat())
                time = km / 1.79
                calories = (time * 5 * 3.5 * 65) / (200 * 60)
            }

            binding.textViewKM.text = String.format("%.2f", km / 1000)
            binding.textViewCalories.text = String.format("%.2f", calories)

            val userPhone = sharedPreferences.getString("phone", null)
            if (userPhone != null) {
                GlobalScope.launch(Dispatchers.Main) {
                    var coins = fetchData(userPhone)
                    val coinThreshold = 100
                    if (currentSteps >= coinThreshold && currentSteps % coinThreshold == 0) {
                        val updatedCoins = coins + 1
                        val updateCoin = HashMap<String, Any>()
                        updateCoin["Coins"] = updatedCoins
                        try {
                            databaseRef.child("user/$userPhone/coinShop").updateChildren(updateCoin)
                            createAnimations(R.drawable.gold_coin)
                        } catch (e: Exception) {
                            Log.e("TAG", "Failed to update coins: ${e.message}")
                        }
                    }
                }
            }

            val stepsSharedPreferences = requireContext().getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
            val editor = stepsSharedPreferences.edit()
            editor.putInt("currentSteps", currentSteps)
            editor.putFloat("distance", km.toFloat())
            editor.apply()
        }
    }

    private fun resetSteps() {
        binding.tvStepsTaken.setOnClickListener {
            Toast.makeText(requireContext(), "Long tap to reset steps", Toast.LENGTH_SHORT).show()
        }

        binding.tvStepsTaken.setOnLongClickListener {
            previousTotalSteps = totalSteps
            km = 0.0
            calories = 0.0
            binding.tvStepsTaken.text = 0.toString()
            binding.textViewCalories.text = 0.0.toString()
            binding.textViewKM.text = 0.0.toString()
            saveData()
            binding.progressCircular.apply {
                setProgressWithAnimation(0.toFloat())
            }
            true
        }
    }

    private fun saveData() {
        val sharedPreferences = requireContext().getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putFloat("key1", previousTotalSteps)
        editor.apply()
    }

    private fun loadData() {
        val sharedPreferences = requireContext().getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val savedNumber = sharedPreferences.getFloat("key1", 0f)
        previousTotalSteps = savedNumber
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    private suspend fun fetchData(userPhone: String): Int {
        val database = Firebase.database.reference
        val coinShopRef = database.child("user").child(userPhone).child("coinShop")
        return try {
            coinShopRef.child("Coins").get().await().getValue(Int::class.java) ?: 0
        } catch (e: Exception) {
            Log.e("FirebaseData", "Error fetching data from Firebase: ${e.message}")
        }
    }

    fun createAnimations(image: Int) {
        // Create ImageView
        var imageView = ImageView(requireContext())
        imageView.setImageResource(image)
        var layoutParams = FrameLayout.LayoutParams(
            200, 200
        )
        layoutParams.gravity = android.view.Gravity.CENTER
        imageView.layoutParams = layoutParams

        // Add ImageView to the layout
        binding.root.addView(imageView)

        val parentWidth = resources.displayMetrics.widthPixels // Width of the parent FrameLayout
        val parentHeight = resources.displayMetrics.heightPixels // Height of the parent FrameLayout
        val marginLeft: Float = ((parentWidth - 200) / 2).toFloat() // Adjust 200 based on the width you want
        val marginTop: Float = ((parentHeight - 200) / 2).toFloat() // Adjust 200 based on the height you want

        // Create animations
        val translateX = ObjectAnimator.ofFloat(imageView, "translationX", marginLeft, marginLeft)
        val translateY = ObjectAnimator.ofFloat(imageView, "translationY", marginTop, -200f)
        val fadeOut = ObjectAnimator.ofFloat(imageView, "alpha", 1f, 0f)

        var animatorSet = AnimatorSet()
        animatorSet.playTogether(translateX, translateY, fadeOut)
        animatorSet.duration = 3000
        animatorSet.interpolator = AccelerateDecelerateInterpolator()

        // Start animation
        animatorSet.start()
    }
}