package net.teamtruta.tiaires.viewModels


import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.teamtruta.tiaires.App
import net.teamtruta.tiaires.extensions.Event
import net.teamtruta.tiaires.data.repositories.Repository


class LoginViewModel(private val dbRepository: Repository) : ViewModel(){

    private val _loginSuccessful = MutableLiveData<Event<String>>()
    val loginSuccessful : LiveData<Event<String>>
        get() = _loginSuccessful

    private val _logoutSuccessful = MutableLiveData<Event<String>>()
    val logoutSuccessful : LiveData<Event<String>>
        get() = _logoutSuccessful

    fun getVersionName(): String{
        return try {
            val context = App.applicationContext()
            val pInfo: PackageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            ""
        }
    }

    fun userIsLoggedIn(): Boolean {
        val authenticationCookie = dbRepository.getAuthenticationCookie()
        return  authenticationCookie != null && authenticationCookie != ""
    }

    fun getUsername(): String {
        return dbRepository.getUsername() ?: ""
    }

    fun login(username: String, password: String) {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            val success = dbRepository.login(username, password)
            if(success){
                _loginSuccessful.postValue(Event(success, "Login successful!"))
            } else {
                _loginSuccessful.postValue(Event(success, "Login not successful. Please check your credentials."))
            }

        }
    }

    fun logout(){
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            val success = dbRepository.logout()
            if(success){
                _logoutSuccessful.postValue(Event(success, "Logout successful!"))
            } else {
                _logoutSuccessful.postValue(Event(success, "Logout not successful."))
            }
        }
    }

}

class LoginViewModelFactory(private val repository: Repository): ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)){
            return LoginViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }

}