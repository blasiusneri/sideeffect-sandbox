package com.blas.x.sideeffectsandbox

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.blas.x.sideeffectsandbox.MainActivity.Companion.TAG
import com.blas.x.sideeffectsandbox.ui.theme.SideEffectSandboxTheme
import java.time.format.TextStyle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  companion object {
    const val TAG = "SideEffectSandbox"
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      SideEffectSandboxTheme { // A surface container using the 'background' color from the theme
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
          //LaunchedEffectDemo()
          //RememberCoroutineScopeDemo()
          //RememberUpdateStateDemo()
          //DisposableEffectDemo()
          //SideEffectDemo()
          //ProduceStateDemo()
          //DerivedStateOfDemo()
          //SnapshotFlowDemo()
        }
      }
    }
  }
}

@Composable
fun LaunchedEffectDemo() {
  val timerLength = 10000L
  val timesUp = remember {
    mutableStateOf(false)
  }
  val timer = remember {
    mutableStateOf(timerLength)
  }

  LaunchedEffect(key1 = Unit) {
    object : CountDownTimer(timerLength, 1000) {
      init {
        Log.d(TAG, "CountDownTimer: $this")
      }
      override fun onTick(millisUntilFinished: Long) {
        if (millisUntilFinished < 1) {
          onFinish()
        }
        timer.value = millisUntilFinished / 1000
      }

      override fun onFinish() {
        timesUp.value = true
      }
    }.start()
  }

  Box(
      modifier = Modifier.fillMaxSize(),
      contentAlignment = Alignment.Center
  ) {
    if (timesUp.value) {
      Text(text = "Time's Up", style = MaterialTheme.typography.displayLarge)
    } else {
      Text(text = timer.value.toString(),  style = MaterialTheme.typography.displayLarge)
    }
  }
}

@Composable
fun RememberCoroutineScopeDemo() {
  //create scope to run the suspend function later
  val scope = rememberCoroutineScope()
  var job: Job? by remember {
    mutableStateOf(null)
  }
  val timesUp = remember {
    mutableStateOf(false)
  }
  val countdownStarted = remember {
    mutableStateOf(false)
  }

  Column {
    Button(modifier = Modifier.wrapContentSize(), onClick = {
      countdownStarted.value = true
      job = scope.launch {
        startCounter(3000) {
          Log.d(TAG,"Times up")
        }
      }
    }) {
      Text(text = "Start Timer")
    }

    Button(modifier = Modifier.wrapContentSize(), onClick = {
      job?.cancel()
    }) {
      Text(text = "Cancel Timer")
    }
  }
}

suspend fun startCounter(time: Long, onTimeEnd: () -> Unit) {
  try {
    delay(time)
    onTimeEnd()
  } catch (exception: Exception) {
    Log.d(TAG,"counter stopped")
  }
}

@Composable
fun RememberUpdateStateDemo() {
  var currentValue by remember { mutableStateOf(0) }
  RememberUpdateStateFunction(100) {
    currentValue += 1
  }
  Text(text = currentValue.toString(), style = MaterialTheme.typography.displayLarge)
}

@Composable
fun RememberUpdateStateFunction(max: Int, onTimeOut: () -> Unit) {
  val currentOnTimeout by rememberUpdatedState(newValue = onTimeOut)
  LaunchedEffect(key1 = true, block = {
    repeat(max) {
      delay(1000)
      currentOnTimeout()
      //onTimeOut()
    }
  })
}

@Composable
fun DisposableEffectDemo() {
  DisposableEffectComposable(
      onStart = {
        Log.d(TAG, "On Start Disposable Effect")
      },
      onStop = {
        Log.d(TAG, "On Stop Disposable Effect")
      }
  )
}

@Composable
fun DisposableEffectComposable(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    onStart: () -> Unit,
    onStop: () -> Unit
) {
  //val currentOnStart by rememberUpdatedState(newValue = onStart)
  //val currentOnStop by rememberUpdatedState(newValue = onStop)

  DisposableEffect(key1 = lifecycleOwner, effect = {
    val observer = LifecycleEventObserver { _, event ->
      if (event == Lifecycle.Event.ON_START) {
        onStart()
      } else if (event == Lifecycle.Event.ON_STOP) {
        onStop()
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose {
      lifecycleOwner.lifecycle.removeObserver(observer)
    }
  })
}

@Composable
fun SideEffectDemo() {
  //No demo
}

@Composable
fun ProduceStateDemo() {
  /*
  val text by produceState(initialValue ="") {
      repeat(10) { count ->
          delay(1000)
          value = count.toString()
      }
  }
  */

  val text by produceState(initialValue = "", producer = {
    val job = MainScope().launch {
      repeat(100) {
        delay(1000)
        value = it.toString()
      }
    }

    awaitDispose {
      job.cancel()
    }
  })
  Box(
      modifier = Modifier.fillMaxSize(),
      contentAlignment = Alignment.Center
  ) {
    Text(text = text, style = MaterialTheme.typography.displayLarge)
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DerivedStateOfDemo() {
  val listState = rememberLazyListState()
  val coroutineScope = rememberCoroutineScope()

  val showButton by remember {
    derivedStateOf {
      listState.firstVisibleItemIndex > 0
    }
  }

  /*var showButton by remember { mutableStateOf(false) }
  showButton = listState.firstVisibleItemIndex > 0*/

  val data = mutableListOf<String>().also {
    for (i in 0 .. 100) {
      it.add(i.toString())
    }
  }
  Box() {
    LazyColumn(state = listState) {
      items(data) {
        Spacer(modifier = Modifier.height(10.dp))
        Box(modifier = Modifier
            .height(200.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFC99DD3))
            .fillParentMaxWidth()) {
          Text(text = it, modifier = Modifier.align(Alignment.Center))
        }
      }
    }

    AnimatedVisibility(
        modifier = Modifier.align(Alignment.BottomEnd),
        visible = showButton,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
      FloatingActionButton(
          modifier = Modifier
              .padding(8.dp)
              .wrapContentSize(),
          onClick = {
            coroutineScope.launch { listState.animateScrollToItem(0, 0) }
          }) {
        Text(modifier = Modifier.padding(8.dp), text = "Scroll top")
      }
    }
    Log.d(TAG, "show button: $showButton")
  }
}

@Composable
fun SnapshotFlowDemo() {
  val listState = rememberLazyListState()
  val coroutineScope = rememberCoroutineScope()

  var showButton by remember { mutableStateOf(false) }
  LaunchedEffect(listState) {
    snapshotFlow { listState.firstVisibleItemIndex }
        .map { index -> index > 0 }
        .distinctUntilChanged()
        .collect { showButton = it }
  }

  val data = mutableListOf<String>().also {
    for (i in 0 .. 100) {
      it.add(i.toString())
    }
  }
  Box() {
    LazyColumn(state = listState) {
      items(data) {
        Spacer(modifier = Modifier.height(10.dp))
        Box(modifier = Modifier
            .height(200.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFC99DD3))
            .fillParentMaxWidth()) {
          Text(text = it, modifier = Modifier.align(Alignment.Center))
        }
      }
    }

    AnimatedVisibility(
        modifier = Modifier.align(Alignment.BottomEnd),
        visible = showButton,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
      FloatingActionButton(
          modifier = Modifier
              .padding(8.dp)
              .wrapContentSize(),
          onClick = {
            coroutineScope.launch { listState.animateScrollToItem(0, 0) }
          }) {
        Text(modifier = Modifier.padding(8.dp), text = "Scroll top")
      }
    }
    Log.d(TAG, "show button: $showButton")
  }
}

@Composable
fun DemoLaunchedEffect(){
  var text by remember { mutableStateOf("")}

  LaunchedEffect(true) {
    repeat(10) { count ->
      delay(1000)
      text = count.toString()
    }
  }
}