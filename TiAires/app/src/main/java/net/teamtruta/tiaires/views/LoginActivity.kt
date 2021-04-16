package net.teamtruta.tiaires.views

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import net.teamtruta.tiaires.*
import net.teamtruta.tiaires.viewModels.LoginViewModel
import net.teamtruta.tiaires.viewModels.LoginViewModelFactory

class LoginActivity : AppCompatActivity() {

    var TAG: String = LoginActivity::class.java.simpleName

    private val viewModel: LoginViewModel by viewModels{
        LoginViewModelFactory((application as App).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Set version
        val version = findViewById<TextView>(R.id.version)
        val versionName: String = viewModel.getVersionName()
        val versionString = "Version: $versionName"
        version.text = versionString

        // Setup password field
        val passwordField = findViewById<EditText>(R.id.password)
        val mgr = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        passwordField.imeOptions = EditorInfo.IME_ACTION_GO
        passwordField.setOnEditorActionListener { v: TextView?, actionId: Int, event: KeyEvent ->
            var handled = false
            if (event.action == KeyEvent.ACTION_DOWN && actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
                mgr.hideSoftInputFromWindow(passwordField.windowToken, 0)
                login(v)
                handled = true
            }
            handled
        }

        // Check if we already have a authentication cookie
        val userIsLoggedIn = viewModel.userIsLoggedIn()
        if (!userIsLoggedIn) {
            val logoutButton = findViewById<Button>(R.id.logout_button)
            logoutButton.visibility = View.INVISIBLE
        } else {
            // Fill in username field if we already have that info
            val username: String  = viewModel.getUsername()
            val usernameField = findViewById<EditText>(R.id.username)
            usernameField.setText(username)
            passwordField.setText(R.string.password_placeholder)
        }

    }

    fun login(view: View?) {

        // On button click get username and password from input fields
        val usernameField = findViewById<EditText>(R.id.username)
        val username = usernameField.text.toString()
        val passwordField = findViewById<EditText>(R.id.password)
        val password = passwordField.text.toString()

        // Make keyboard disappear
        val mgr = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        mgr.hideSoftInputFromWindow(passwordField.windowToken, 0)



        if (username == "" || password == "") {
            //One of the input fields is empty. Request filling up the fields
            Toast.makeText(this, "Please fill in both the username and the password fields", Toast.LENGTH_LONG).show()
            return
        }

        // Else, if both string are there, try to login
        viewModel.loginSuccessful.observe(this, { loginEventContent ->
            loginEventContent.getContentIfNotHandled()?.let {
            if(it){
                // Login Successsful
                val t = Toast.makeText(this, "Login Successful.", Toast.LENGTH_LONG)
                t.show()

                // Open home page
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)

            } else{
                // Login not successful
                val t = Toast.makeText(this,
                        "Login NOT Successful. Please check your credentials", Toast.LENGTH_LONG)
                t.show()

            }
        } })
        viewModel.login(username, password)
    }



    fun logout(view: View?) {

        viewModel.logoutSuccessful.observe(this, { logoutEvent ->
            logoutEvent.getContentIfNotHandled()?.let{
            if(it){
                Toast.makeText(this, "Logout Successful.", Toast.LENGTH_SHORT).show()
                val logoutButton = findViewById<Button>(R.id.logout_button)
                logoutButton.visibility = View.INVISIBLE
                val usernameField = findViewById<EditText>(R.id.username)
                val passwordField = findViewById<EditText>(R.id.password)
                usernameField.setText("")
                passwordField.setText("")
            } else {
                Toast.makeText(this, "Logout NOT Successful.", Toast.LENGTH_SHORT).show()
            }
        } })

        viewModel.logout()

    }


}