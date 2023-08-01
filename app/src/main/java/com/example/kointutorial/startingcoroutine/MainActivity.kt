package com.example.kointutorial.startingcoroutine

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kointutorial.startingcoroutine.ui.theme.StartingCoroutineTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
    private val TAG = "MAIN ACTIVITY"

    private val mainViewModel: MainViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StartingCoroutineTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    DisplayCount()
                }
            }
        }

        observeViewModelData()
        //printDifferentCoroutineContext()
    }

    private fun observeViewModelData() {
        mainViewModel.liveData.observe(this) {
            showToast(it)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.stateFlow.collectLatest {
                    showToast(it)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.sharedFlow.collect {
                    showToast(it)
                }
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(
            this, message, Toast.LENGTH_SHORT
        ).show()
    }

    /**
     *  Demo 1
     *  1) Like thread, Coroutines can run in parallel
     */
    private fun launchTwoCoroutineInParallel() {

        lifecycleScope.launch {
            println("Started in ${Thread.currentThread().name}")

            var result1 = GlobalScope.async {
                delay(2000)
                println("1 Running in ${Thread.currentThread().name}")
                "Hello"
            }

            var result2 = GlobalScope.async {
                delay(3000)
                println(" 2 Running in ${Thread.currentThread().name}")
                "world"
            }

            println("${result1.await()} ${result2.await()}")
            println("End in ${Thread.currentThread().name}")
        }
    }


    /**
     * Demo 2
     *
     * Coroutine that inherit thread and scope from its parent with Unconfined Dispatcher, will continue in same thread as parent.
     * But after the coroutine being suspended and resumed it may not continue in same thread as it started
     */
    private fun launchCoroutineWithLocalScope() = runBlocking {

        println("Started in ${Thread.currentThread().name}")

        launch(Dispatchers.Unconfined) {
            println("1, Started  in ${Thread.currentThread().name}")

            delay(200)
            println("1, Ending in ${Thread.currentThread().name}")
        }

        //delay(2000)
        for (i in 0..1000) {

        }
        println("End in ${Thread.currentThread().name}")
    }

    /**
     * Demo 3
     *
     * Print Different Coroutine Scope
     */

    private fun printDifferentCoroutineScope() = runBlocking {

        println("RunBlocking $this")

        launch {
            println("Launch $this")
        }

        async {
            println("Async $this")
        }
    }

    /**
     * Demo 4
     *
     * Print Different Coroutine Context
     */

    private fun printDifferentCoroutineContext() = runBlocking {

        println("RunBlocking $coroutineContext")

        launch(coroutineContext) {
            println("Launch $coroutineContext")
        }

        async {
            println("Async $coroutineContext")
        }

        launch(Dispatchers.IO) {
            println("Launch with changed Coroutine Context $coroutineContext")

            async {
                println("Async with inherited Coroutine context $coroutineContext")
            }
        }
    }

    private fun launchThread() {

        println("Started in ${Thread.currentThread().name}")

        Thread(Runnable {
            println(" 1 Running in ${Thread.currentThread().name}")
        }).start()


        Thread(Runnable {
            println(" 2 Running in ${Thread.currentThread().name}")
        }).start()

        println("Started in ${Thread.currentThread().name}")
    }


}

suspend fun doNetworkCall(): String {
    delay(3000)
    return "Completed Network Call 1"
}

suspend fun doNetworkCall2(): String {
    delay(3000)
    return "Completed Network Call 2"
}

@Composable
fun DisplayCount(modifier: Modifier = Modifier, viewModel: MainViewModel = viewModel()) {
    val count = viewModel.countState.collectAsState()
    Column() {

        Text(
            text = "Hello ${count.value}!", modifier = modifier
        )

        ButtonToTriggerDataHolder(modifier = modifier,
            buttonText = "Trgger Live Data",
            onClick = { viewModel.triggerLiveData() })

        ButtonToTriggerDataHolder(modifier = modifier,
            buttonText = "Trgger State Flow",
            onClick = { viewModel.triggerStateFlow() })

        ButtonToTriggerDataHolder(modifier = modifier,
            buttonText = "Trgger Shared Flow",
            onClick = { viewModel.triggerSharedFlow() })

        ButtonToTriggerDataHolder(
            modifier = modifier,
            buttonText = "Trgger Flow",
            onClick = { viewModel.triggerFlow() })
    }
}

@Composable
fun ButtonToTriggerDataHolder(modifier: Modifier, buttonText: String, onClick: () -> Unit) {
    Spacer(modifier = modifier.padding(vertical = 8.dp))

    Button(onClick = onClick) {
        Text(
            text = buttonText, modifier = modifier
        )
    }

}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    StartingCoroutineTheme {
        DisplayCount()
    }

}

