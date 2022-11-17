package com.oruam.dogs.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.oruam.dogs.model.DogBreed

class DetailViewModel: ViewModel() {
    val dog = MutableLiveData<DogBreed>()

    fun fetch() {
        val dog1 = DogBreed("1", "Corgi Test", "15 years", "breadGroup", "breadFor", "temperament", "")
        dog.value = dog1
    }
}