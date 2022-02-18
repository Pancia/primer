package com.dayzerostudio.primer.ui

import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.beust.klaxon.Klaxon

fun Context.getCache() =
    getSharedPreferences("cache", 0)!!

val json = Klaxon().converter(uuidConverter)

@Composable
fun rememberStringPreference(
    keyName: String,
    initialValue: String
): MutableState<String> {
    val currentState: MutableState<String> = remember { mutableStateOf(initialValue) }
    val context = LocalContext.current
    val sp = context.getCache()
    sp.getString(keyName, null)?.let {
        currentState.value = it
    }
    return object : MutableState<String> {
        val save = debounce(300, rememberCoroutineScope()) { v: String ->
            sp.edit().putString(keyName, v).apply()
        }
        override var value: String
            get() = currentState.value
            set(v) {
                currentState.value = v
                save(v)
            }

        override fun component1() = value
        override fun component2(): (String) -> Unit = { value = it }
    }
}

@Composable
inline fun <reified T> rememberJsonPreference(
    keyName: String,
    initialValue: T
): MutableState<T> {
    val currentState: MutableState<T> = remember { mutableStateOf(initialValue) }
    val context = LocalContext.current
    val sp = context.getCache()
    sp.getString(keyName, null)?.let {
        currentState.value = json.parse(it)!!
    }
    return object : MutableState<T> {
        override var value: T
            get() = currentState.value
            set(v) {
                currentState.value = v
                val js = json.toJsonString(v)
                sp.edit().putString(keyName, js).apply()
            }

        override fun component1() = value
        override fun component2(): (T) -> Unit = { value = it }
    }
}

@Composable
inline fun <reified T> rememberJsonListPreference(
    keyName: String,
    initialValue: List<T>
): MutableState<List<T>> {
    val currentState: MutableState<List<T>> = remember { mutableStateOf(initialValue) }
    val context = LocalContext.current
    val sp = context.getCache()
    sp.getString(keyName, null)?.let {
        currentState.value = json.parseArray(it)!!
    }
    return object : MutableState<List<T>> {
        override var value: List<T>
            get() = currentState.value
            set(v) {
                currentState.value = v
                val js = json.toJsonString(v)
                sp.edit().putString(keyName, js).apply()
            }

        override fun component1() = value
        override fun component2(): (List<T>) -> Unit = { value = it }
    }
}
