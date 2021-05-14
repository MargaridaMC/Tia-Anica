package net.teamtruta.tiaires.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.*
import net.teamtruta.tiaires.data.models.GeocachingTourWithCaches
import net.teamtruta.tiaires.data.repositories.Repository
import net.teamtruta.tiaires.extensions.Event
import net.teamtruta.tiaires.extensions.Resource
import java.lang.IllegalArgumentException

class MainActivityViewModel(private val repository: Repository) : ViewModel(){

    val allTours : LiveData<List<GeocachingTourWithCaches>> = repository.allTours
    private val _userIsLoggedIn =  MutableLiveData<Event<Boolean>>()
    val userIsLoggedIn: LiveData<Event<Boolean>>
        get() = _userIsLoggedIn

    fun setCurrentTourID(tourID: Long) = repository.setCurrentTourID(tourID)
    fun deleteAllGeoCachesNotBeingUsed() {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {repository.deleteAllGeoCachesNotBeingUsed()}
    }

    fun userIsLoggedIn() {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            val success = repository.userIsLoggedIn()
            if (success){
                _userIsLoggedIn.postValue(Event(success, "User is logged in"))
            } else{
                _userIsLoggedIn.postValue(Event(success, "It seems you are no longer logged in. Please login again."))
            }
        }
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