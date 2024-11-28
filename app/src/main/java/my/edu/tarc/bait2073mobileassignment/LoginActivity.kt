package my.edu.tarc.bait2073mobileassignment

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.database
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import my.edu.tarc.bait2073mobileassignment.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        rememberMe()

        //Login with existing account
        binding.buttonLogin.setOnClickListener {

            val email = binding.editTextEmailAddress.text.toString()
            val password = binding.editTextPassword.text.toString()

            if (email.isEmpty()) {
                binding.editTextEmailAddress.error = "This field is required"
                return@setOnClickListener

            }
            if (password.isEmpty()){
                binding.editTextPassword.error = "This field is required"
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val found = verifyLogin(email, password)
                if(found){
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    // start your next activity
                    startActivity(intent)
                    finish()
                }else{
                    Toast.makeText(
                        this@LoginActivity,
                        "Incorrect email address or password!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } //End of buttonLogin

        var isClicked = true
        //View or hide the password
        binding.imageViewHide.setOnClickListener {
            if (isClicked) {
                binding.imageViewHide.setImageResource(R.drawable.view)
                binding.editTextPassword.transformationMethod = null
                isClicked = !isClicked
            } else if (!isClicked) {
                binding.imageViewHide.setImageResource(R.drawable.hide)
                binding.editTextPassword.transformationMethod = PasswordTransformationMethod()
                isClicked = true

            } //End of if-else

        } //End of imageHide

        //Register an account
        binding.textViewRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            // start your next activity
            startActivity(intent)
        }

    }

    private fun rememberMe(){
        val sharedPreferences = getSharedPreferences("prof_pref", Context.MODE_PRIVATE)
        val remember = sharedPreferences.getBoolean("remember", false)
        if(remember){
            binding.editTextEmailAddress.setText(sharedPreferences.getString("email", ""))
            binding.editTextPassword.setText(sharedPreferences.getString("password", ""))
            binding.checkBoxRememberMe.isChecked = sharedPreferences.getBoolean("remember", false)
        }
    }

    private suspend fun verifyLogin(email: String, password: String): Boolean {
        val database = Firebase.database.reference.child("user")
        return try {
            val snapshot = database.get().await()
            var found = false
            for (dataSnapshot in snapshot.children) {
                val emailFromDataSnapshot = dataSnapshot.child("email").getValue(String::class.java) ?: ""
                val passwordFromDataSnapshot = dataSnapshot.child("password").getValue(String::class.java) ?: ""
                if (emailFromDataSnapshot == email && passwordFromDataSnapshot == password) {
                    found = true
                    saveProfPreferencesTemp(dataSnapshot)
                    break
                }
            }
            found
        } catch (e: Exception) {
            Log.e("FirebaseData", "Error fetching data from Firebase: ${e.message}")
            false
        }
    }

    private fun saveProfPreferencesTemp(dataSnapshot: DataSnapshot){
        val remember = binding.checkBoxRememberMe.isChecked
        val sharedPreferences = getSharedPreferences("prof_pref", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()){
            putString("email", dataSnapshot.child("email").getValue(String::class.java) ?: "")
            putString("password", dataSnapshot.child("password").getValue(String::class.java) ?: "")
            putString("name", dataSnapshot.child("name").getValue(String::class.java) ?: "")
            putString("phone", dataSnapshot.child("phone").getValue(String::class.java)?: "")
            putString("gender", dataSnapshot.child("gender").getValue(String::class.java) ?: "")
            putInt("age", dataSnapshot.child("age").getValue(Int::class.java) ?: 1)
            putFloat("height", dataSnapshot.child("height").getValue(Float::class.java) ?: 1.0f)
            putFloat("weight", dataSnapshot.child("weight").getValue(Float::class.java) ?: 1.0f)
            putInt("steps", dataSnapshot.child("steps").getValue(Int::class.java) ?: 100)
            putInt("distances", dataSnapshot.child("distances").getValue(Int::class.java) ?: 0)
            putBoolean("remember", remember)
            apply()
        }
    }

}



