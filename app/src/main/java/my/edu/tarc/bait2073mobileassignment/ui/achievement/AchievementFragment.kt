package my.edu.tarc.bait2073mobileassignment.ui.achievement

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import my.edu.tarc.bait2073mobileassignment.R
import my.edu.tarc.bait2073mobileassignment.databinding.FragmentAchievementBinding

class AchievementFragment : Fragment() {

    private var _binding: FragmentAchievementBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout using view binding
        _binding = FragmentAchievementBinding.inflate(inflater, container, false)
        return binding.root
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        displayAchievementDetails()

        binding.btnShare.setOnClickListener{
            shareAchievement()
        }

    }

    private fun displayAchievementDetails() {
        val stepsSharedPreferences = requireContext().getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val profSharedPreferences = requireContext().getSharedPreferences("prof_pref", Context.MODE_PRIVATE)
        // Update achievement details TextView with dynamic data (replace with actual data)
        val stepsAchieved = stepsSharedPreferences.getInt("currentSteps", 0)
        val stepsGoal = profSharedPreferences.getInt("steps", 100)
        val distanceAchieved = stepsSharedPreferences.getInt("currentDistances", 0)
        val distanceGoal = profSharedPreferences.getInt("distances", 0)

        if(stepsAchieved >= stepsGoal || distanceAchieved >= distanceGoal){
            binding.textViewCongrates.text = "Great Work!"
            binding.textViewAnouncement.text = "You have just completed"
        } else {
            binding.textViewCongrates.text = "Don't Give Up!"
            binding.textViewAnouncement.text = "You can do better next time."
        }

        // Update the image based on achievement status
        if (stepsAchieved >= stepsGoal || distanceAchieved >= distanceGoal) {
            binding.imageViewAchievement.setImageResource(R.drawable.medal) // Show medal image
        } else {
            binding.imageViewAchievement.setImageResource(R.drawable.comfort_image) // Show comfort image
        }

        binding.textViewStepsAchieve.text = "$stepsAchieved /"
        binding.textViewDistancesAchieve.text = "$distanceAchieved km /"
        binding.textViewGoalSteps.text = "$stepsGoal "
        binding.textViewGoalDistances.text = "$distanceGoal km"
    }

    private fun shareAchievement(){
        val achievementText = binding.textViewCongrates.text.toString() + "\n" +
                binding.textViewAnouncement.text.toString() + "\n" +
                "Steps Achieved: " + binding.textViewStepsAchieve.text.toString() + "\n" +
                "Distance Achieved: " + binding.textViewDistancesAchieve.text.toString() + "\n" +
                "Goal Steps: " + binding.textViewGoalSteps.text.toString() + "\n" +
                "Goal Distances: " + binding.textViewGoalDistances.text.toString()

        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_TEXT, achievementText)

        startActivity(Intent.createChooser(shareIntent, "Share via"))
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}