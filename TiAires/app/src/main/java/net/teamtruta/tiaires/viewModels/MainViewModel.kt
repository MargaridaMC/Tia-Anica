package net.teamtruta.tiaires.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.*
import net.teamtruta.tiaires.data.models.GeocachingTourWithCaches
import net.teamtruta.tiaires.data.repositories.Repository
import java.lang.IllegalArgumentException

class MainActivityViewModel(private val repository: Repository) : ViewModel(){

    val allTours : LiveData<List<GeocachingTourWithCaches>> = repository.allTours

    fun setCurrentTourID(tourID: Long) = repository.setCurrentTourID(tourID)
    fun deleteAllGeoCachesNotBeingUsed() {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {repository.deleteAllGeoCachesNotBeingUsed()}
    }

    fun userIsLoggedIn(): Boolean {
        val authenticationCookie = repository.getAuthenticationCookie()
        return  authenticationCookie != null && authenticationCookie != ""
    }

    fun getUsername(): String {
        return repository.getUsername()?:""
    }


}

class MainActivityViewModelFactory(private val repository: Repository): ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainActivityViewModel::class.java)){
            return MainActivityViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }

}