package my.edu.tarc.bait2073mobileassignment.ui.home

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
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
import my.edu.tarc.bait2073mobileassignment.databinding.FragmentHomeBinding


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private var currentImageID: Int = R.drawable.hamster_without_clothes
    private lateinit var sharedPreferences: SharedPreferences
    private val databaseRef = Firebase.database.reference

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        loadData()

        initCoinShop()

        sharedPreferences = requireActivity().getSharedPreferences("prof_pref", Context.MODE_PRIVATE)
        binding.textViewHelloUser.text = getString(R.string.hello_user) + " " + sharedPreferences.getString("name", null)

        var userPhone = sharedPreferences.getString("phone", null)
        if (userPhone != null) {
            GlobalScope.launch(Dispatchers.Main) {
                var userData = fetchData(userPhone)
                binding.buttonFood.text = "x" + userData.petFoodQty
                binding.buttonToy.text = "x" + userData.petToyQty
                binding.buttonClothes.text = "x" + userData.petClothesQty
                binding.buttonDog.text = "x" + userData.dogQty
                binding.buttonCat.text = "x" + userData.catQty

                binding.buttonFood.setOnClickListener {
                    if(userData.petFoodQty > 0) {
                        val newFood = userData.petFoodQty - 1
                        val updateFood = hashMapOf<String, Any>()
                        updateFood["PetFoodQty"] = newFood
                        binding.buttonFood.text = "x$newFood"
                        try {
                            databaseRef.child("user/$userPhone/coinShop")
                                .updateChildren(updateFood)
                            createAnimations(R.drawable.pet_food, binding.buttonFood)
                            userData.petFoodQty = newFood
                        } catch (e: Exception) {
                            Log.e("TAG", "Failed to update coins: ${e.message}")
                        }
                    } else {
                        Toast.makeText(context, "You do not have any food!", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                binding.buttonToy.setOnClickListener {
                    if(userData.petToyQty > 0) {
                        val newToy = userData.petToyQty - 1
                        val updateToy = hashMapOf<String, Any>()
                        updateToy["PetToyQty"] = newToy
                        binding.buttonToy.text = "x$newToy"
                        try {
                            databaseRef.child("user/$userPhone/coinShop")
                                .updateChildren(updateToy)
                            userData.petToyQty = newToy
                        } catch (e: Exception) {
                            Log.e("TAG", "Failed to update coins: ${e.message}")
                        }

                        val num = (1..5).random()
                        if (num == 1) {
                            createAnimations(R.drawable.toy1, binding.buttonToy)
                        } else if (num == 2) {
                            createAnimations(R.drawable.toy2, binding.buttonToy)
                        } else if (num == 3) {
                            createAnimations(R.drawable.toy3, binding.buttonToy)
                        } else if (num == 4) {
                            createAnimations(R.drawable.toy4, binding.buttonToy)
                        } else if (num == 5) {
                            createAnimations(R.drawable.toy5, binding.buttonToy)
                        }
                    } else {
                        Toast.makeText(context, "You do not have any toys!", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                binding.buttonClothes.setOnClickListener {
                    if(userData.petClothesQty > 0) {
                        if(currentImageID == R.drawable.dog_without_clothes) {
                            binding.imageViewMain.setImageResource(R.drawable.dog_with_clothes)
                            currentImageID = R.drawable.dog_with_clothes
                            saveData()
                        }
                        else if(currentImageID == R.drawable.cat_without_clothes) {
                            binding.imageViewMain.setImageResource(R.drawable.cat_with_clothes)
                            currentImageID = R.drawable.cat_with_clothes
                            saveData()
                        }
                        else if(currentImageID == R.drawable.hamster_without_clothes) {
                            binding.imageViewMain.setImageResource(R.drawable.hamster_with_clothes)
                            currentImageID = R.drawable.hamster_with_clothes
                            saveData()
                        }
                    } else {
                        Toast.makeText(context, "You do not have clothes yet!", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                binding.buttonDog.setOnClickListener {
                    if(userData.dogQty > 0) {
                        currentImageID = R.drawable.dog_without_clothes
                        binding.imageViewMain.setImageResource(currentImageID)
                        saveData()
                    }
                    else {
                        Toast.makeText(context, "You do not have dog yet!", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                binding.buttonCat.setOnClickListener {
                    if(userData.catQty > 0) {
                        currentImageID = R.drawable.cat_without_clothes
                        binding.imageViewMain.setImageResource(currentImageID)
                        saveData()
                    }
                    else {
                        Toast.makeText(context, "You do not have cat yet!", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                binding.buttonHamster.setOnClickListener {
                    currentImageID = R.drawable.hamster_without_clothes
                    binding.imageViewMain.setImageResource(currentImageID)
                    saveData()
                }
            }
        }

        binding.buttonCheckStat.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_stepCounterFragment)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun createAnimations(image: Int, button: Button) {
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

        button.setEnabled(false)
        Handler().postDelayed(Runnable { // This method will be executed once the timer is over
            button.setEnabled(true)
        }, 1000)
    }

    private fun saveData() {
        val sharedPreferences = requireContext().getSharedPreferences("homePrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt("imageID", currentImageID)
        editor.apply()
    }

    private fun loadData() {
        val sharedPreferences = requireContext().getSharedPreferences("homePrefs", Context.MODE_PRIVATE)
        val savedID = sharedPreferences.getInt("imageID", R.drawable.hamster_without_clothes)
        currentImageID = savedID
        binding.imageViewMain.setImageResource(savedID)
    }

    data class UserData(
        var coins: Int,
        var petFoodQty: Int,
        var petToyQty: Int,
        var petClothesQty: Int,
        var dogQty: Int,
        var catQty: Int
    )

    private suspend fun fetchData(userPhone: String): UserData {
        val database = Firebase.database.reference
        val coinShopRef = database.child("user").child(userPhone).child("coinShop")
        return try {
            val snapshot = coinShopRef.get().await()
            UserData(
                coins = snapshot.child("Coins").getValue(Int::class.java) ?: 0,
                petFoodQty = snapshot.child("PetFoodQty").getValue(Int::class.java) ?: 0,
                petToyQty = snapshot.child("PetToyQty").getValue(Int::class.java) ?: 0,
                petClothesQty = snapshot.child("PetClothesQty").getValue(Int::class.java) ?: 0,
                dogQty = snapshot.child("DogQty").getValue(Int::class.java) ?: 0,
                catQty = snapshot.child("CatQty").getValue(Int::class.java) ?: 0
            )
        } catch (e: Exception) {
            Log.e("FirebaseData", "Error fetching data from Firebase: ${e.message}")
            UserData(0, 0, 0, 0, 0, 0)
        }
    }

    private fun initCoinShop() {
        val profSharedPreferences =
            requireActivity().getSharedPreferences("prof_pref", Context.MODE_PRIVATE)
        val profPhone = profSharedPreferences.getString("phone", "")
        val databaseRef = Firebase.database.reference

        // Check if variable exists in Firebase
        if (!profPhone.isNullOrEmpty()) {
            databaseRef.child("user/$profPhone/coinShop").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Variable exists, retrieve the values
                        val value = dataSnapshot.value // This will contain the data from "user/profPhone/coinShop"
                        // Use the retrieved value as needed
                    } else {
                        // Variable does not exist, add the new variable
                        val coinShopDataMap = HashMap<String, Any>()
                        coinShopDataMap["Coins"] = 0
                        coinShopDataMap["PetFoodQty"] = 0
                        coinShopDataMap["PetToyQty"] = 0
                        coinShopDataMap["PetClothesQty"] = 0
                        coinShopDataMap["DogQty"] = 0
                        coinShopDataMap["CatQty"] = 0


                        databaseRef.child("user/$profPhone/coinShop").setValue(coinShopDataMap)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle database error
                    Log.e("TAG", "Database error: ${databaseError.message}")
                }
            })
        }
        else {
            Log.e("TAG", "profPhone is null")
        }
    }
}