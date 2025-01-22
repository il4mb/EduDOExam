package com.il4mb.edudoexam.tools

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class Debouncer(private val coroutineContext: CoroutineContext = Dispatchers.Main) {
    private var debounceJob: Job? = null

    /**
     * Executes the given action after the specified delay, canceling any previously scheduled action.
     *
     * @param delayMillis The delay in milliseconds before executing the action.
     * @param action The action to execute.
     */
    fun debounce(delayMillis: Long, action: () -> Unit) {
        debounceJob?.cancel() // Cancel any previously scheduled job
        debounceJob = CoroutineScope(coroutineContext).launch {
            delay(delayMillis) // Wait for the delay
            action() // Execute the action
        }
    }

    /**
     * Cancels the current debounce job, if any.
     */
    fun cancel() {
        debounceJob?.cancel()
    }
}