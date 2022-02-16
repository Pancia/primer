package com.dayzerostudio.primer.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
@Preview
fun LoadingBar() =
    Box(modifier = Modifier.fillMaxSize()) {
        CircularProgressIndicator(
            modifier = Modifier
                .fillMaxSize(.3f)
                .align(Alignment.Center),
            strokeWidth = 16.dp
        )
    }

sealed class IOResult<out R> {
    object Loading : IOResult<Nothing>()
    data class Success<out T>(val data: T) : IOResult<T>()
    data class Failure(val reason: String) : IOResult<Nothing>()
    data class Error(val exception: Exception) : IOResult<Nothing>()

    @Composable
    fun Render() {
        when (this) {
            Loading -> LoadingBar()
            is Failure -> Text(reason)
            is Error -> Text(exception.toString())
            else -> {}
        }
    }

    companion object {
        fun <R> justTry(fn: () -> IOResult<R>): IOResult<R> {
            return try {
                fn()
            } catch (e: Exception) {
                Error(e)
            }
        }

        fun <R> tryLoad(fn: () -> R): IOResult<R> {
            return try {
                Success(fn())
            } catch (e: Exception) {
                Error(e)
            }
        }
    }
}

fun <R> IOResult<R>.ifSuccess(fn: (R) -> Unit): IOResult<R> {
    when (this) {
        is IOResult.Success -> fn(this.data)
        else -> {}
    }
    return this
}
