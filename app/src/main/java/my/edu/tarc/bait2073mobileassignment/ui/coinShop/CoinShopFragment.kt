package my.edu.tarc.bait2073mobileassignment.ui.coinShop

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.google.firebase.Firebase
import com.google.firebase.database.database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import my.edu.tarc.bait2073mobileassignment.R
import my.edu.tarc.bait2073mobileassignment.databinding.FragmentCoinshopBinding

class CoinShopFragment : Fragment() {

    private var _binding: FragmentCoinshopBinding? = null
    private lateinit var sharedPreferences: SharedPreferences
    private val databaseRef = Firebase.database.reference
    private lateinit var userData:UserData

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCoinshopBinding.inflate(inflater, container, false)

        sharedPreferences = requireActivity().getSharedPreferences("prof_pref", Context.MODE_PRIVATE)
        var userPhone = sharedPreferences.getString("phone", null)
        if (userPhone != null) {
            GlobalScope.launch(Dispatchers.Main) {
                userData = fetchData(userPhone)

                if(userData.petClothesQty != 0) {
                    binding.buttonClothes.isEnabled = false
                    binding.buttonClothes.text = "Owned"
                    binding.buttonClothes.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                }
                else {
                    binding.buttonClothes.isEnabled = true
                    binding.buttonClothes.text = getString(R.string.pet_clothes_price)
                    binding.buttonClothes.setCompoundDrawablesWithIntrinsicBounds(R.drawable.gold_coin, 0, 0, 0)
                }

                if(userData.dogQty != 0) {
                    binding.buttonDog.isEnabled = false
                    binding.buttonDog.text = "Owned"
                    binding.buttonDog.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                }
                else {
                    binding.buttonDog.isEnabled = true
                    binding.buttonDog.text = getString(R.string.dog_price)
                    binding.buttonDog.setCompoundDrawablesWithIntrinsicBounds(R.drawable.gold_coin, 0, 0, 0)
                }

                if(userData.catQty != 0) {
                    binding.buttonCat.isEnabled = false
                    binding.buttonCat.text = "Owned"
                    binding.buttonCat.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                }
                else {
                    binding.buttonCat.isEnabled = true
                    binding.buttonCat.text = getString(R.string.cat_price)
                    binding.buttonCat.setCompoundDrawablesWithIntrinsicBounds(R.drawable.gold_coin, 0, 0, 0)
                }

                binding.buttonTotalCoins.text = userData.coins.toString()

                binding.buttonFood.setOnClickListener {
                    if (userData.coins.toString().toInt() >= 5) {
                        val newCoins = userData.coins.toString().toInt() - 5
                        val newFood = userData.petFoodQty + 1
                        val updateCoin = hashMapOf<String, Any>()
                        updateCoin["Coins"] = newCoins
                        updateCoin["PetFoodQty"] = newFood
                        binding.buttonTotalCoins.text = "$newCoins"
                        try {
                            databaseRef.child("user/$userPhone/coinShop").updateChildren(updateCoin)
                            createAnimations(R.drawable.gold_coin)
                            userData.coins = newCoins
                            userData.petFoodQty = newFood
                        } catch (e: Exception) {
                            Log.e("TAG", "Failed to update coins: ${e.message}")
                        }
                    } else {
                        Toast.makeText(context, "You do not have enough coins!", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                binding.buttonToy.setOnClickListener {
                    if (userData.coins.toString().toInt() >= 100) {
                        val newCoins = userData.coins.toString().toInt() - 100
                        val newToy = userData.petToyQty + 1
                        val updateCoin = hashMapOf<String, Any>()
                        updateCoin["Coins"] = newCoins
                        updateCoin["PetToyQty"] = newToy
                        binding.buttonTotalCoins.text = "$newCoins"
                        try {
                            databaseRef.child("user/$userPhone/coinShop")
                                .updateChildren(updateCoin)
                            createAnimations(R.drawable.gold_coin)
                            userData.coins = newCoins
                            userData.petToyQty = newToy
                        } catch (e: Exception) {
                            Log.e("TAG", "Failed to update coins: ${e.message}")
                        }
                    } else {
                        Toast.makeText(context, "You do not have enough coins!", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                binding.buttonClothes.setOnClickListener {
                    if(userData.petClothesQty == 0) {
                        if (userData.coins.toString().toInt() >= 200) {
                            val newCoins = userData.coins.toString().toInt() - 200
                            val newClothes = userData.petClothesQty + 1
                            val updateCoin = hashMapOf<String, Any>()
                            updateCoin["Coins"] = newCoins
                            updateCoin["PetClothesQty"] = newClothes
                            binding.buttonTotalCoins.text = "$newCoins"
                            binding.buttonClothes.isEnabled = false
                            binding.buttonClothes.text = "Owned"
                            binding.buttonClothes.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                            try {
                                databaseRef.child("user/$userPhone/coinShop")
                                    .updateChildren(updateCoin)
                                createAnimations(R.drawable.gold_coin)
                                userData.coins = newCoins
                            } catch (e: Exception) {
                                Log.e("TAG", "Failed to update coins: ${e.message}")
                            }
                        } else {
                            Toast.makeText(context, "You do not have enough coins!", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                    else {
                        Toast.makeText(context, "You already owned this item!", Toast.LENGTH_SHORT)
                            .show()
                    }

                }

                binding.buttonDog.setOnClickListener {
                    if(userData.dogQty == 0) {
                        if (userData.coins.toString().toInt() >= 5000) {
                            val newCoins = userData.coins.toString().toInt() - 5000
                            val newDog = userData.dogQty + 1
                            val updateCoin = hashMapOf<String, Any>()
                            updateCoin["Coins"] = newCoins
                            updateCoin["DogQty"] = newDog
                            binding.buttonTotalCoins.text = "$newCoins"
                            binding.buttonDog.isEnabled = false
                            binding.buttonDog.text = "Owned"
                            binding.buttonDog.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                            try {
                                databaseRef.child("user/$userPhone/coinShop")
                                    .updateChildren(updateCoin)
                                createAnimations(R.drawable.gold_coin)
                                userData.coins = newCoins
                            } catch (e: Exception) {
                                Log.e("TAG", "Failed to update coins: ${e.message}")
                            }
                        } else {
                            Toast.makeText(
                                context,
                                "You do not have enough coins!",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    }
                    else {
                        Toast.makeText(context, "You already owned this item!", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                binding.buttonCat.setOnClickListener {
                    if(userData.catQty == 0) {
                        if (userData.coins.toString().toInt() >= 5000) {
                            val newCoins = userData.coins.toString().toInt() - 5000
                            val newCat = userData.catQty + 1
                            val updateCoin = hashMapOf<String, Any>()
                            updateCoin["Coins"] = newCoins
                            updateCoin["CatQty"] = newCat
                            binding.buttonTotalCoins.text = "$newCoins"
                            binding.buttonCat.isEnabled = false
                            binding.buttonCat.text = "Owned"
                            binding.buttonCat.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                            try {
                                databaseRef.child("user/$userPhone/coinShop")
                                    .updateChildren(updateCoin)
                                createAnimations(R.drawable.gold_coin)
                                userData.coins = newCoins
                            } catch (e: Exception) {
                                Log.e("TAG", "Failed to update coins: ${e.message}")
                            }
                        } else {
                            Toast.makeText(
                                context,
                                "You do not have enough coins!",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    }
                    else {
                        Toast.makeText(context, "You already owned this item!", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                binding.buttonVoucher.setOnClickListener {
                    if (userData.coins.toString().toInt() >= 49999) {
                        val newCoins = userData.coins.toString().toInt() - 49999
                        val updateCoin = hashMapOf<String, Any>()
                        updateCoin["Coins"] = newCoins
                        binding.buttonTotalCoins.text = "$newCoins"
                        try {
                            databaseRef.child("user/$userPhone/coinShop")
                                .updateChildren(updateCoin)
                            createAnimations(R.drawable.gold_coin)
                            VoucherDialog().show(childFragmentManager, VoucherDialog.TAG)
                            userData.coins = newCoins
                        } catch (e: Exception) {
                            Log.e("TAG", "Failed to update coins: ${e.message}")
                        }
                    } else {
                        Toast.makeText(context, "You do not have enough coins!", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    data class UserData(
        var coins: Int,
        var petFoodQty: Int,
        var petToyQty: Int,
        val petClothesQty: Int,
        val dogQty: Int,
        val catQty: Int
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
        binding.animationContainer.addView(imageView)

        val parentHeight = binding.animationContainer.height // Height of the parent FrameLayout
        val marginTop: Float = ((parentHeight - 200) / 2).toFloat() // Adjust 200 based on the height you want

        // Create animations
        val translateY = ObjectAnimator.ofFloat(imageView, "translationY", marginTop, -200f)
        val fadeOut = ObjectAnimator.ofFloat(imageView, "alpha", 1f, 0f)

        var animatorSet = AnimatorSet()
        animatorSet.playTogether(translateY, fadeOut)
        animatorSet.duration = 3000
        animatorSet.interpolator = AccelerateDecelerateInterpolator()

        // Start animation
        animatorSet.start()
    }

    class VoucherDialog() : DialogFragment() {
        private val voucherCode = getRandomString(16)
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =

            AlertDialog.Builder(requireContext())
                .setMessage("Voucher Code: " + voucherCode)
                .setPositiveButton("ok") { _,_ -> }
                .setNegativeButton("Copy") { _, _ ->
                    val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Voucher Code", voucherCode)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(requireContext(), "Voucher code copied", Toast.LENGTH_SHORT).show()
                }
                .create()

        companion object {
            const val TAG = "VoucherDialog"
        }

        fun getRandomString(length: Int) : String {
            val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
            return (1..length)
                .map { allowedChars.random() }
                .joinToString("")
        }
    }
}