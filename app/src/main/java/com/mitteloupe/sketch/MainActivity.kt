package com.mitteloupe.sketch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mitteloupe.sketch.ui.theme.SketchTheme
import kotlin.random.Random
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SketchTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .padding(innerPadding)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Demo()
                    }
                }
            }
        }
    }
}

@Composable
fun Demo(modifier: Modifier = Modifier) {
    var showDialog by rememberSaveable { mutableStateOf(false) }

    Row(modifier = modifier) {
        VerticalDivider(
            modifier = Modifier
                .fillMaxHeight()
                .width(16.dp)
        )

        val cardShape by remember {
            mutableStateOf(
                SketchRoundRectangleShape(CornerSize(16.dp))
            )
        }
        Card(
            shape = cardShape,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .wrapContentHeight()
            ) {
                Text("Hello Android!")
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    var text by remember { mutableStateOf("Text") }
                    val seed by remember(text) { mutableIntStateOf(Random.nextInt()) }
                    TextField(
                        value = text,
                        label = { Text("Label") },
                        onValueChange = { text = it },
                        modifier = Modifier.weight(1f),
                        seed = seed
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Button(
                        shape = SketchCapsuleShape(),
                        modifier = Modifier
                            .defaultMinSize(minWidth = 96.dp)
                            .height(48.dp),
                        onClick = {}
                    ) {
                        Text("Hi!")
                    }
                }
                Spacer(modifier = Modifier.size(8.dp))
                Button(
                    onClick = {},
                    shape = SketchRectangleShape(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Sketchy Button")
                }
                val outlinedButtonShape by remember { mutableStateOf(SketchRectangleShape()) }
                androidx.compose.material3.OutlinedButton(
                    onClick = {},
                    shape = outlinedButtonShape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 8.dp)
                ) {
                    Text("Hi!")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    var checkboxChecked: Boolean by remember { mutableStateOf(false) }
                    val checkboxSeed by remember(checkboxChecked) {
                        mutableIntStateOf(Random.nextInt())
                    }
                    Checkbox(
                        checked = checkboxChecked,
                        onCheckedChange = { checkboxChecked = it },
                        seed = checkboxSeed
                    )
                    Text("Check!")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    var radioChecked: Boolean by remember { mutableStateOf(false) }
                    val radioSeed by remember(radioChecked) { mutableIntStateOf(Random.nextInt()) }
                    RadioButton(
                        selected = radioChecked,
                        onClick = { radioChecked = !radioChecked },
                        seed = radioSeed
                    )
                    Text("Radio!")
                }
                var switchChecked: Boolean by remember { mutableStateOf(false) }
                val switchSeed by remember(switchChecked) { mutableIntStateOf(Random.nextInt()) }
                Switch(
                    checked = switchChecked,
                    onCheckedChange = { switchChecked = it },
                    randomSeed = switchSeed
                )
                val interactionSource: MutableInteractionSource =
                    remember { MutableInteractionSource() }

                @OptIn(ExperimentalMaterial3Api::class)
                val sliderState = remember {
                    SketchSliderState(
                        valueRange = 0f..100f,
                        onValueChangeFinished = {}
                    )
                }
                @OptIn(ExperimentalMaterial3Api::class)
                Slider(
                    state = sliderState,
                    modifier = Modifier.fillMaxWidth(),
                    interactionSource = interactionSource,
                    randomSeed = 0
                )
                @OptIn(ExperimentalMaterial3Api::class)
                val rangeSliderState = remember {
                    SketchRangeSliderState()
                }
                @OptIn(ExperimentalMaterial3Api::class)
                RangeSlider(state = rangeSliderState)
                Spacer(modifier = Modifier.size(8.dp))

                var progressRandomSeed by remember { mutableIntStateOf(0) }
                LaunchedEffect(Unit) {
                    while (true) {
                        progressRandomSeed = Random.nextInt()
                        delay(125)
                    }
                }
                var progress by remember { mutableFloatStateOf(.5f) }
                LaunchedEffect(Unit) {
                    while (true) {
                        progress = (progress + .05f) % 1.5f
                        delay(100)
                    }
                }
                Row {
                    CircularProgressIndicator(randomSeed = progressRandomSeed)
                    Spacer(modifier = Modifier.size(8.dp))
                    CircularProgressIndicator(
                        progress = { progress },
                        randomSeed = progressRandomSeed
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Column {
                        LinearProgressIndicator(randomSeed = progressRandomSeed)
                        Spacer(modifier = Modifier.size(8.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            randomSeed = progressRandomSeed
                        )
                    }
                }
                Spacer(modifier = Modifier.size(8.dp))
                ElevatedButton(
                    onClick = { showDialog = true },
                    shape = SketchRectangleShape(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Open Dialog")
                }
            }
        }
    }

    if (showDialog) {
        val dialogShape by remember {
            mutableStateOf(SketchRoundRectangleShape(CornerSize(16.dp)))
        }
        SketchDialog(
            onDismissRequest = { showDialog = false },
            shape = dialogShape,
            borderSize = 1.dp
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Sketchy indeed...",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { showDialog = false },
                    shape = SketchRoundRectangleShape(CornerSize(8.dp))
                ) {
                    Text("Close")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SketchTheme {
        Demo(Modifier.wrapContentSize())
    }
}
