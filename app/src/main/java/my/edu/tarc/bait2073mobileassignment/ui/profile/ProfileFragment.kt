package my.edu.tarc.bait2073mobileassignment.ui.profile

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.launch
import my.edu.tarc.bait2073mobileassignment.LoginActivity
import my.edu.tarc.bait2073mobileassignment.R
import my.edu.tarc.bait2073mobileassignment.databinding.FragmentProfileBinding
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get()= _binding!!

    private lateinit var db: DatabaseReference
    private lateinit var storage: StorageReference

    private val getProfilePicture = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            binding.ImageViewProfile.setImageURI(it)
            saveProfilePictureToLocalFile(it)
            saveProfilePictureToCloud(it)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Initialize Firebase instance
        db = FirebaseDatabase.getInstance().getReference("user")
        storage = FirebaseStorage.getInstance().reference

        //Load profile data
        lifecycleScope.launch {
            loadProfile()
        }

        //Edit profile (click edit icon)
        binding.imageViewEdit.setOnClickListener(){
            enableEditing(true)
        }

        //Save Profile (click save icon)
        binding.imageViewSave.setOnClickListener(){
            saveProfile()
        }

        //Log out
        binding.btnLogout.setOnClickListener(){
            Toast.makeText(context, "Logged out", Toast.LENGTH_SHORT).show()

            //Navigate to login screen
            val intent = Intent(requireActivity(), LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish() // Optional: Finish current activity to prevent back navigation
        }

    }

    private fun loadProfile(){
        val sharedPreferences = requireActivity().getSharedPreferences("prof_pref", Context.MODE_PRIVATE)

        val name = sharedPreferences.getString("name", "")
        val height = sharedPreferences.getFloat("height",1.0f)
        val weight = sharedPreferences.getFloat("weight",1.0f)
        val age = sharedPreferences.getInt("age",1)
        val gender = sharedPreferences.getString("gender", "male")?.lowercase()
        val steps = sharedPreferences.getInt("steps", 100)
        val distances = sharedPreferences.getInt("distances", 0)

        //assign the value get from shared preference to the UI
        binding.TextViewName.text = name
        binding.tvGender.text = gender
        binding.edtTextAge.setText(age.toString())
        binding.edtTextWeight.setText(weight.toString())
        binding.edtTextHeight.setText(height.toString())
        binding.edtTextSteps.setText(steps.toString())
        binding.edtTextDistances.setText(distances.toString())

        // Set the correct radio button checked based on gender
        if (gender == "male") {
            binding.radioBtnMale.isChecked = true
        } else {
            binding.radioBtnFemale.isChecked = true
        }

        readProfilePicture()

        enableEditing(false)
    }

    private fun enableEditing(enable: Boolean){
        binding.edtTextAge.isEnabled = enable
        binding.edtTextWeight.isEnabled = enable
        binding.edtTextHeight.isEnabled = enable
        binding.edtTextSteps.isEnabled = enable
        binding.edtTextDistances.isEnabled = enable
        binding.radioBtnGender.visibility = if (enable) View.VISIBLE else View.GONE
        binding.btnLogout.visibility = if (enable) View.GONE else View.VISIBLE
        binding.imageViewEdit.visibility = if (enable) View.GONE else View.VISIBLE
        binding.imageViewSave.visibility = if (enable) View.VISIBLE else View.GONE
        binding.tvGender.visibility =if (enable) View.GONE else View.VISIBLE

        if (enable) {
            binding.cardViewProfile.setOnClickListener {
                getProfilePicture.launch("image/*")
            }
        } else {
            binding.cardViewProfile.setOnClickListener(null)
        }

    }

    private fun saveProfile(){
        val sharedPreferences = requireActivity().getSharedPreferences("prof_pref", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("phone", "")

            //Get the edited value
        val gender = when (binding.radioBtnGender.checkedRadioButtonId) {
            R.id.radioBtnMale -> "Male"
            R.id.radioBtnFemale -> "Female"
            else -> "Male" // Default to Male if no selection
        }
        val age = binding.edtTextAge.text.toString().toIntOrNull() ?: 1
        val weight = binding.edtTextWeight.text.toString().toFloatOrNull() ?: 1.0f
        val height = binding.edtTextHeight.text.toString().toFloatOrNull() ?: 1.0f
        val steps = binding.edtTextSteps.text.toString().toIntOrNull() ?: 100
        val distances = binding.edtTextDistances.text.toString().toIntOrNull() ?: 0

        //Update the value into the firebase
        //using Hashmap
        val updateProfile = hashMapOf<String, Any>(
            "gender" to gender,
            "age" to age,
            "weight" to weight,
            "height" to height,
            "steps" to steps,
            "distances" to distances
        )


        if(userId != null){
            db.child(userId).updateChildren(updateProfile).addOnSuccessListener {
                Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                enableEditing(false)
            } .addOnFailureListener{
                Log.e("Profile Fragment", "Error updating profile")
            }
        }

        //Update in the SharedPreferences
        with(sharedPreferences.edit()){
             putString("gender", gender)
             putInt("age", age)
             putFloat("height", height)
             putFloat("weight", weight)
             putInt("steps", steps)
             putInt("distances", distances)
             apply()
        }

        // Update the TextViewGender with the new gender
        binding.tvGender.text = gender
    }

    private fun saveProfilePictureToLocalFile(uri: Uri) {
        val sharedPreferences = requireActivity().getSharedPreferences("prof_pref", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("name", "")
        val filename = "$username.png" // Use username to name the PNG file
        val file = File(this.context?.filesDir, filename)

        try {
            val inputStream = requireActivity().contentResolver.openInputStream(uri)
            val outputStream: OutputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.flush()
            outputStream.close()
            Log.d("ProfileFragment", "Profile picture saved locally at ${file.absolutePath}")
        } catch (e: FileNotFoundException) {
            Log.e("ProfileFragment", "File not found while saving profile picture", e)
        } catch (e: IOException) {
            Log.e("ProfileFragment", "IO Exception while saving profile picture", e)
        }
    }

    private fun readProfilePicture() {
        val sharedPreferences = requireActivity().getSharedPreferences("prof_pref", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("phone", "")
        val username = sharedPreferences.getString("name", "")
        val filename = "$username.png" // Use username to name the PNG file
        val file = File(this.context?.filesDir, filename)

        if (file.exists()) {
            // Load the profile picture from the local file
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            binding.ImageViewProfile.setImageBitmap(bitmap)
        } else {
            // Load the profile picture from Firebase Storage
            if (!userId.isNullOrEmpty()) {
                val storageReference = storage.child("images/$userId")
                storageReference.downloadUrl.addOnSuccessListener { uri ->
                    Log.d("ProfileFragment", "Profile picture URL: $uri")
                    // Use Glide to download and display the image
                    Glide.with(this)
                        .asBitmap()
                        .load(uri)
                        .placeholder(R.drawable.default_profile) // Placeholder image
                        .into(object : CustomTarget<Bitmap>() {
                            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                binding.ImageViewProfile.setImageBitmap(resource)
                                // Save the bitmap to local storage
                                saveBitmapToLocalFile(resource, file)
                            }

                            override fun onLoadCleared(placeholder: Drawable?) {
                                // Handle placeholder if needed
                            }

                            override fun onLoadFailed(errorDrawable: Drawable?) {
                                super.onLoadFailed(errorDrawable)
                                Log.e("ProfileFragment", "Failed to load image using Glide")
                            }
                        })
                }.addOnFailureListener { exception ->
                    Log.e("ProfileFragment", "Error retrieving profile picture from Firebase Storage", exception)
                    binding.ImageViewProfile.setImageResource(R.drawable.default_profile)
                }
            } else {
                binding.ImageViewProfile.setImageResource(R.drawable.default_profile)
            }
        }
    }

    private fun saveBitmapToLocalFile(bitmap: Bitmap, file: File) {
        try {
            val outputStream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 50, outputStream)
            outputStream.flush()
            outputStream.close()
            Log.d("ProfileFragment", "Profile picture saved locally at ${file.absolutePath}")
        } catch (e: FileNotFoundException) {
            Log.e("ProfileFragment", "File not found while saving profile picture", e)
        } catch (e: IOException) {
            Log.e("ProfileFragment", "IO Exception while saving profile picture", e)
        }
    }


    private fun saveProfilePictureToCloud(uri: Uri) {
        val sharedPreferences = requireActivity().getSharedPreferences("prof_pref", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("phone", "")
        if (!userId.isNullOrEmpty()) {
            val storageReference = storage.child("images/$userId")
            storageReference.putFile(uri).addOnSuccessListener {
                Log.d("ProfileFragment", "Profile picture uploaded to Firebase Storage")
            }.addOnFailureListener {
                Log.e("ProfileFragment", "Error uploading profile picture to Firebase Storage")
            }
        } else {
            Toast.makeText(requireActivity(), "Profile info incomplete", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}