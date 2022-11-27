package com.oruam.dogs.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.oruam.dogs.model.DogBreed
import com.oruam.dogs.model.DogDatabase
import kotlinx.coroutines.launch

class DetailViewModel(application: Application): BaseViewModel(application) {
    val dog = MutableLiveData<DogBreed>()

    fun fetch(uuid: Int) {
        getFromDatabase(uuid)
    }

    private fun getFromDatabase(uuid: Int) {
        launch {
            val dogFromDatabase = DogDatabase(getApplication()).dogDao().getDog(uuid)
            dog.value = dogFromDatabase
        }
    }
}