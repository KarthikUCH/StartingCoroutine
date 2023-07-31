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
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.lang.Exception
import java.lang.NullPointerException

class MainActivity : ComponentActivity() {
    private val TAG = "MAIN ACTIVITY"

    private val mainViewModel: MainViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StartingCoroutineTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DisplayCount()
                }
            }
        }

        observeViewModelData()
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
            this,
            message,
            Toast.LENGTH_SHORT
        ).show()
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
            text = "Hello ${count.value}!",
            modifier = modifier
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

        ButtonToTriggerDataHolder(modifier = modifier,
            buttonText = "Trgger Flow",
            onClick = { viewModel.triggerFlow() })
    }
}

@Composable
fun ButtonToTriggerDataHolder(modifier: Modifier, buttonText: String, onClick: () -> Unit) {
    Spacer(modifier = modifier.padding(vertical = 8.dp))

    Button(onClick = onClick) {
        Text(
            text = buttonText,
            modifier = modifier
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

