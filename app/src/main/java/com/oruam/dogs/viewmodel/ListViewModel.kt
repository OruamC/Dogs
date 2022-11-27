package com.oruam.dogs.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.oruam.dogs.model.DogBreed
import com.oruam.dogs.model.DogDatabase
import com.oruam.dogs.model.DogsApiService
import com.oruam.dogs.util.SharedPreferencesHelper
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.observers.DisposableSingleObserver
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.launch

class ListViewModel(application: Application) : BaseViewModel(application) {

    private var prefHelper = SharedPreferencesHelper(getApplication())
    private var refreshTime = 5 * 60 * 1000 * 1000 * 1000L

    private val dogsService = DogsApiService()

    // avoid memory leaks
    private val disposable = CompositeDisposable()

    val dogs = MutableLiveData<List<DogBreed>>()
    val dogsLoadError = MutableLiveData<Boolean>()
    val loading = MutableLiveData<Boolean>()

    fun refresh() {
        val updateTime = prefHelper.getUpdateTime()
        if (updateTime != null && updateTime != 0L && System.nanoTime() - updateTime < refreshTime) {
            fetchFromDatabase()
        } else {
            fetchFromRemote()
        }
    }

    fun refreshBypassCache() {
        fetchFromRemote()
    }

    private fun fetchFromDatabase() {
        loading.value = true
        launch {
            val dogs = DogDatabase(getApplication()).dogDao().getAllDogs()
            dogsRetrieved(dogs)
            Toast.makeText(getApplication(), "Dogs retrieved from database", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun fetchFromRemote() {
        loading.value = true
        disposable.add(
            dogsService.getDogs()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<List<DogBreed>>() {
                    override fun onSuccess(dogsList: List<DogBreed>) {
                        storeDogsLocally(dogsList)
                        Toast.makeText(
                            getApplication(),
                            "Dogs retrieved from api",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }

                    override fun onError(e: Throwable) {
                        dogsLoadError.value = false
                        loading.value = false
                        e.printStackTrace()
                    }
                })
        )
    }

    private fun dogsRetrieved(dogsList: List<DogBreed>) {
        dogs.value = dogsList
        dogsLoadError.value = false
        loading.value = false
    }

    private fun storeDogsLocally(list: List<DogBreed>) {
        launch {
            val dao = DogDatabase(getApplication()).dogDao()
            dao.deleteAllDogs()
            val result = dao.insertAll(*list.toTypedArray())
            var i = 0
            while (i < list.size) {
                list[i].uuid = result[i].toInt()
                ++i
            }
            dogsRetrieved(list)
        }
        prefHelper.saveUpdateTime(System.nanoTime())
    }

    override fun onCleared() {
        super.onCleared()
        // clean up
        disposable.clear()
    }
}