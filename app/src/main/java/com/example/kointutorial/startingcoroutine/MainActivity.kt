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
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis

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

        //observeViewModelData()
        //printDifferentCoroutineScope()
        printDifferentCoroutineContext()
        //testFlowIntermidaterFlatMap()
        //testLaunchCoroutineBuilder()
        //sequentialExecution()
        //concurrentExecution()
        //lazyExecution()
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

    /***********************************************************************************************
     ***********************************            COROUTINE        *******************************
     ***********************************************************************************************/

    /**
     * Launch coroutine builder, just fire and forgot
     */
    private fun testLaunchCoroutineBuilder() {
        GlobalScope.launch {
            println("Outer Launch coroutine Start in ${Thread.currentThread().name}")
            delay(2000)
            launch {
                println("1st Inner Launch coroutine in ${Thread.currentThread().name}")
                delay(3000)

                println(" *** 1st Inner Launch coroutine End in ${Thread.currentThread().name}")
            }

            launch {
                println("2nd Inner Launch coroutine in ${Thread.currentThread().name}")
                delay(7000)

                println(" *** 2nd Inner Launch coroutine End in ${Thread.currentThread().name}")
            }

            println(" *** Outer Launch coroutine End in ${Thread.currentThread().name}")
        }

    }

    /**
     * Coroutine Demo 1
     * 1) Like thread, Coroutines can run in parallel
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
     * Coroutine Demo 2
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
     * Coroutine Demo 3
     *
     * Print Different Coroutine Scope Instance
     *  *Every coroutine has its own CoroutineScope instance attached to it
     *  *Child CoroutineScope instance is independent on parent CoroutineScope instance
     */

    private fun printDifferentCoroutineScope() = runBlocking {

        println("RunBlocking $this")

        launch {
            println("Launch $this")

            launch {
                println("Launch Child $this")
            }
        }

        async {
            println("Async $this")
        }
    }

    /**
     * Coroutine Demo 4
     *
     * Print Different Coroutine Context
     */

    private fun printDifferentCoroutineContext() = runBlocking {

        println("RunBlocking $coroutineContext in Thread ${Thread.currentThread().name}")

        launch(Dispatchers.Main) {
            println("Launch Dispatchers.Default $coroutineContext in Thread ${Thread.currentThread().name}")
        }

        launch(coroutineContext) {
            println("Launch $coroutineContext in Thread ${Thread.currentThread().name}")
        }

        async {
            println("Async $coroutineContext in Thread ${Thread.currentThread().name}")
        }

        launch(Dispatchers.IO) {
            println("Launch with changed Coroutine Context $coroutineContext in Thread ${Thread.currentThread().name}")

            async {
                println("Async with inherited Coroutine context $coroutineContext in Thread ${Thread.currentThread().name}")
            }
        }
    }

    /**
     *  Coroutine Demo 5
     *  Functions execution within a coroutine are sequential by default
     */
    private fun sequentialExecution() = runBlocking {

        println("Execution starts in ${Thread.currentThread().name}")

        val time = measureTimeMillis {
            val msg1 = doNetworkCall()
            val msg2 = doNetworkCall2()

            println("The msg is $msg1 $msg2")
        }

        println("Total time taken is $time ms")
        println("Execution ends in ${Thread.currentThread().name}")
    }

    /**
     *  Coroutine Demo 6
     *  To achieve concurrency within a coroutine,
     *  you can use async or launch as child coroutine builders
     */
    private fun concurrentExecution() = runBlocking {

        println("Execution starts in ${Thread.currentThread().name}")

        val time = measureTimeMillis {
            val msg1: Deferred<String> = async { doNetworkCall() }
            val msg2: Deferred<String> = async { doNetworkCall2() }

            println("The msg is ${msg1.await()} ${msg2.await()}")
        }

        println("Total time taken is $time ms")
        println("Execution ends in ${Thread.currentThread().name}")
    }

    /**
     *  Coroutine Demo 7
     *  To execute a coroutine only when its result needed
     */
    private fun lazyExecution() = runBlocking {

        println("Execution starts in ${Thread.currentThread().name}")


        val msg1: Deferred<String> = async(start = CoroutineStart.LAZY) { doNetworkCall() }
        val msg2: Deferred<String> = async(start = CoroutineStart.LAZY) { doNetworkCall2() }

        // The above 2 coroutines wont get executed unless msg1.await() & msg2.await() get called
        // TODO: Getting `A resource failed to call close` error, while commenting below line
        println("The msg is ${msg1.await()} ${msg2.await()}")

        println("Execution ends in ${Thread.currentThread().name}")
    }


    suspend fun doNetworkCall(): String {
        println("Netwrok call 1 executing in ${Thread.currentThread().name}")
        delay(1000)
        return "Hello"
    }

    suspend fun doNetworkCall2(): String {
        println("Netwrok call 2 executing in ${Thread.currentThread().name}")
        delay(1000)
        return "World"
    }

    /***********************************************************************************************
     ***********************************            THREAD           *******************************
     ***********************************************************************************************/

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

    /***********************************************************************************************
     ***********************************              FLOW           *******************************
     ***********************************************************************************************/

    /**
     * Flow Demo 1
     * Intermediaries operators aren't executed until the values are consumed
     *
     * In below function resultFlow intermediate operation will be triggered 4 times since its been
     * consumed 4 times
     */
    private fun testFlowIntermidaterFlatMap() {
        val _trigger = MutableStateFlow(true)

        /**
         * Exposes result of this use case
         */
        val resultFlow = _trigger.flatMapLatest {
            flowOf("Result")
        }

        lifecycleScope.launch {
            resultFlow.collectLatest { }
        }

        lifecycleScope.launch {
            resultFlow.collectLatest { }
        }

        lifecycleScope.launch {
            resultFlow.collectLatest { }
        }

        lifecycleScope.launch {
            resultFlow.collectLatest { }
        }
    }


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

