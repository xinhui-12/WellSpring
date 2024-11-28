package my.edu.tarc.bait2073mobileassignment.ui.bmi

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import my.edu.tarc.bait2073mobileassignment.R
import my.edu.tarc.bait2073mobileassignment.databinding.FragmentNutritionAdviceBinding

class NutritionAdviceFragment : Fragment() {

    private var _binding: FragmentNutritionAdviceBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNutritionAdviceBinding.inflate(inflater,container,false)
        return binding.root;
    } // end of onCreateView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.closeBtn.setOnClickListener {
            findNavController().popBackStack()
        }
    } // end of onViewCreated
}