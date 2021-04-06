package net.teamtruta.tiaires.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.*
import net.teamtruta.tiaires.data.GeocachingTourWithCaches
import net.teamtruta.tiaires.data.Repository
import java.lang.IllegalArgumentException

class MainActivityViewModel(private val repository: Repository) : ViewModel(){

    fun setCurrentTourID(tourID: Long) = repository.setCurrentTourID(tourID)
    fun deleteAllGeoCachesNotBeingUsed() {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {repository.deleteAllGeoCachesNotBeingUsed()}
    }

    fun getNTours() : Int{
        return repository.getNTours()
    }

    val allTours : LiveData<List<GeocachingTourWithCaches>> = repository.allTours


    }

class MainActivityViewModelFactory(private val repository: Repository): ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainActivityViewModel::class.java)){
            return MainActivityViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }

}