package net.teamtruta.tiaires.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.teamtruta.tiaires.data.models.GeocachingTourWithCaches
import net.teamtruta.tiaires.data.repositories.Repository
import java.lang.IllegalArgumentException


class MapActivityViewModel(private val repository: Repository) : ViewModel(){

    val currentTour: LiveData<GeocachingTourWithCaches> = repository.getCurrentTour()

    fun updateTour(tour: GeocachingTourWithCaches) {
        GlobalScope.launch {
            repository.updateGeocachingTour(tour)
        }
    }

}

class MapActivityViewModelFactory(private val repository: Repository): ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapActivityViewModel::class.java)){
            return MapActivityViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }

}