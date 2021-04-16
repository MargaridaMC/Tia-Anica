package net.teamtruta.tiaires.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import net.teamtruta.tiaires.data.models.GeoCacheInTour
import net.teamtruta.tiaires.data.repositories.Repository
import java.lang.IllegalArgumentException

class GeoCacheListAdapterViewModel(private val repository: Repository) : ViewModel(){

    fun updateGeoCacheInTour(geoCacheInTour: GeoCacheInTour){
        repository.updateGeoCacheInTour(geoCacheInTour)
    }
}

class GeoCacheListAdapterViewModelFactory(private val repository: Repository): ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GeoCacheListAdapterViewModel::class.java)){
            return GeoCacheListAdapterViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }

}