package com.example.kointutorial.startingcoroutine

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {


    private val _liveData = MutableLiveData<String>()
    val liveData: LiveData<String> = _liveData

    private val _stateFlow = MutableStateFlow("Hello State Flow ")
    val stateFlow: StateFlow<String> = _stateFlow

    private val _sharedFlow = MutableSharedFlow<String>(1)
    val sharedFlow = _sharedFlow.onEach { _sharedFlow.resetReplayCache() }


    init {
        triggerSharedFlow()
    }

    fun triggerLiveData() {
        _liveData.value = "LiveData Triggered"
    }

    fun triggerStateFlow() {
        _stateFlow.value = "StateFlow Triggered"
    }

    fun triggerSharedFlow() {
        viewModelScope.launch {
            delay(5000)
            _sharedFlow.emit("SharedFlow Triggered")
        }
    }

    fun triggerFlow(): Flow<String> = flow {
        repeat(5) {
            emit("Time $it")
            delay(1000)
        }
    }

    init {
        //startThreadCount()
    }

    private val _countState = MutableStateFlow(0)
    val countState: StateFlow<Int> = _countState

    private fun startThreadCount() {
        println("Hello from ${Thread.currentThread().name}")
        for (i in 1..100) {
            Thread {
                println("Hello from Thread ${Thread.currentThread().name}")

                //println(i)
                _countState.value = i
                Thread.sleep(1000)
            }.start()
        }

    }

    private fun startCoroutineCount() {
        println("Hello from ${Thread.currentThread().name}")
        for (i in 1..100) {
            viewModelScope.launch(Dispatchers.IO) {
                println("Hello from Thread ${Thread.currentThread().name}")
                //println(i)
                _countState.value = i
                delay(1000)
            }
        }
    }
}