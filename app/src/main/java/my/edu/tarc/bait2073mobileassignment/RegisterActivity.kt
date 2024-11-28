package my.edu.tarc.bait2073mobileassignment

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.method.PasswordTransformationMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.firebase.Firebase
import com.google.firebase.database.database
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import my.edu.tarc.bait2073mobileassignment.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var isClicked = true
        //View or hide the new password
        binding.imageViewHidePass.setOnClickListener {

            if (isClicked) {
                binding.imageViewHidePass.setImageResource(R.drawable.view)
                binding.editTextNewPassword.transformationMethod = null
                isClicked = !isClicked
            } else if (!isClicked) {
                binding.imageViewHidePass.setImageResource(R.drawable.hide)
                binding.editTextNewPassword.transformationMethod = PasswordTransformationMethod()
                isClicked = true

            } //End of if-else

        } //End of imageHidePass

        //View or hide for confirm password
        binding.imageViewHideConfirmPass.setOnClickListener {

            if (isClicked) {
                binding.imageViewHideConfirmPass.setImageResource(R.drawable.view)
                binding.editTextConfirmPassword.transformationMethod = null
                isClicked = !isClicked
            } else if (!isClicked) {
                binding.imageViewHideConfirmPass.setImageResource(R.drawable.hide)
                binding.editTextConfirmPassword.transformationMethod =
                    PasswordTransformationMethod()
                isClicked = true

            } //End of if-else

        } //End of imageHidePass

        // Set the text
        val text = "Already have an account? Login here"

        // Create a SpannableString
        val spannableString = SpannableString(text)

        //Define the ClickableSpan for the word "Login"
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                // Handle click event for "Login" (e.g., navigate to LoginActivity)
                startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
            }

            override fun updateDrawState(ds: android.text.TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = true  // Underline the text
                ds.color = resources.getColor(R.color.purple_text, null)  // Highlight colour
            }
        }

        // Set the ClickableSpan for the word "Login"
        val startIndex = text.indexOf("Login")
        val endIndex = startIndex + "Login".length
        spannableString.setSpan(
            clickableSpan,
            startIndex,
            endIndex,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Set the ForegroundColorSpan for the word "Login"
        val colorSpan = ForegroundColorSpan(resources.getColor(R.color.purple_text, null))
        spannableString.setSpan(colorSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        // Set the UnderlineSpan for the word "Login" if needed (ClickableSpan already underlines)
        val underlineSpan = UnderlineSpan()
        spannableString.setSpan(
            underlineSpan,
            startIndex,
            endIndex,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Update TextView with the SpannableString
        binding.textViewLoginPrompt.text = spannableString
        binding.textViewLoginPrompt.movementMethod =
            LinkMovementMethod.getInstance() // Enable clickable

        // Register button logic
        binding.btnRegister.setOnClickListener {

            val firstName = binding.editTextFirstName.text.toString()
            val lastName = binding.editTextLastName.text.toString()
            val phoneNumber = binding.editTextPhoneNumber.text.toString()
            val emailAddress = binding.editTextNewEmailAddress.text.toString()
            val password = binding.editTextNewPassword.text.toString()
            val confirmPassword = binding.editTextConfirmPassword.text.toString()
            val checkBoxTerms = binding.checkBoxTerms

            if (firstName.isEmpty()) {
                binding.editTextFirstName.error = "This field is required"
                return@setOnClickListener
            }
            if (lastName.isEmpty()) {
                binding.editTextLastName.error = "This field is required"
                return@setOnClickListener
            }
            if (phoneNumber.isEmpty()) {
                binding.editTextPhoneNumber.error = "This field is required"
                return@setOnClickListener
            }
            if (emailAddress.isEmpty()) {
                binding.editTextNewEmailAddress.error = "This field is required"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                binding.editTextNewPassword.error = "This field is required"
                return@setOnClickListener
            }
            if (confirmPassword.isEmpty()) {
                binding.editTextConfirmPassword.error = "This field is required"
                return@setOnClickListener
            }
            if (binding.editTextNewPassword.text.toString() != binding.editTextConfirmPassword.text.toString()) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!checkBoxTerms.isChecked) {
                Toast.makeText(
                    this,
                    "You must agree to the Terms & Conditions and Privacy Policy to register",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            // Email validation
            if (!isValidEmail(emailAddress)) {
                binding.editTextNewEmailAddress.error = "Invalid email address. Expected format: example@gmail.com"
                return@setOnClickListener
            }

            // Phone number validation
            if (!isValidPhoneNumber(phoneNumber)) {
                binding.editTextPhoneNumber.error = "Invalid phone number format. Expected format: 0123456789"
                return@setOnClickListener
            }

            lifecycleScope.launch {
                if (verifyRegisterData(phoneNumber, emailAddress)) {
                    val name = "$firstName $lastName"
                    saveRegisterData(name, phoneNumber, emailAddress, password)
                    val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(
                        this@RegisterActivity,
                        "Phone number and/or email address has been registered",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } // end of buttonRegister

    } // end of onCreate

    // Function to validate email format
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // Function to validate phone number format
    private fun isValidPhoneNumber(phoneNumber: String): Boolean {
        val phonePattern = "^[0-9]{10,11}\$"
        return phoneNumber.matches(phonePattern.toRegex())
    }

    private suspend fun verifyRegisterData(phone: String, email: String): Boolean {
        val database = Firebase.database.reference.child("user")
        return try {
            val snapshot = database.get().await()
            var valid = true
            for (dataSnapshot in snapshot.children) {
                val phoneFromDataSnapshot = dataSnapshot.child("phone").getValue(String::class.java) ?: ""
                val emailFromDataSnapshot = dataSnapshot.child("email").getValue(String::class.java) ?: ""
                if (phoneFromDataSnapshot == phone || emailFromDataSnapshot == email) {
                    valid = false
                    break
                }
            }
            valid
        } catch (e: Exception) {
            Log.e("FirebaseData", "Error fetching data from Firebase: ${e.message}")
            false
        }
    }

    private fun saveRegisterData(name: String, phone: String, email: String, password: String) : Boolean{
        val database = Firebase.database.reference
        val userRef = database.child("user").child(phone)
        var success = true
        val userData = mapOf(
            "name" to name,
            "phone" to phone,
            "email" to email,
            "password" to password,
            "age" to 1,
            "distances" to 0,
            "gender" to "male",
            "height" to 1.0f,
            "weight" to 1.0f,
            "steps" to 100
        )
        userRef.setValue(userData)
            .addOnSuccessListener {
                Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                success = true
            }
            .addOnFailureListener {e ->
                Toast.makeText(this, "Registration failed: ${e.message}", Toast.LENGTH_SHORT).show()
                success = false
            }
        return success
    }
}