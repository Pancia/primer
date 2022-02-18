package com.dayzerostudio.primer.ui

import androidx.compose.material.MaterialTheme
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.mapLatest

fun <T> debounce(
    waitMs: Long,
    coroutineScope: CoroutineScope,
    destinationFunction: (T) -> Unit
): (T) -> Unit {
    var debounceJob: Job? = null
    return { param: T ->
        debounceJob?.cancel()
        debounceJob = coroutineScope.launch {
            delay(waitMs)
            destinationFunction(param)
        }
    }
}

@Composable
fun <T> MutableState<T>.debouncedObserver(
    waitMs: Long,
    scope: CoroutineScope,
    function: (T) -> Unit
): MutableState<T> {
    val debounced = remember {
        debounce(waitMs, scope) { s: T ->
            function(s)
        }
    }
    remember {
        snapshotFlow { value }.mapLatest {
            debounced(it)
        }
    }.collectAsState(initial = value)
    return this
}

@Composable
fun DebouncedTextField(
    initialValue: String,
    scope: CoroutineScope,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    textStyle: TextStyle = MaterialTheme.typography.h5,
    debouncedOnValueChange: (String) -> Unit
) {
    val text = remember { mutableStateOf(initialValue) }
        .debouncedObserver(300, scope, debouncedOnValueChange)
    TextField(
        value = text.value,
        onValueChange = { text.value = it },
        label = label,
        textStyle = textStyle,
        modifier = modifier
    )
}