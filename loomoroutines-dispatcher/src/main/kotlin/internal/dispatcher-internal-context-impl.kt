package dev.reformator.loomoroutines.dispatcher.internal

import dev.reformator.loomoroutines.common.internal.*
import dev.reformator.loomoroutines.dispatcher.*
import java.time.Duration
import java.util.concurrent.Semaphore

private val log = getLogger()

class DispatcherContextImpl<T>: DispatcherContext<T>, Promise<T> {
    private val _state = atomic<DispatcherContextImplState<T>>(EmptyRunningDispatcherContextImplState)
    private val _lastEvent = atomic<DispatcherEvent?>(null)

    override val state: PromiseState
        get() = _state.value.state

    override fun join(): T {
        //Completed
        _state.value.let { state ->
            if (state is CompletedDispatcherContextImplState) {
                return state.result.get()
            }
        }

        //In dispatcher coroutine
        if (isInDispatcher) {
            var result: PromiseResult<T>? = null
            await { notifier ->
                subscribe {
                    result = it
                    notifier()
                }
            }
            return result!!.get()
        }

        //Blocking
        run {
            val semaphore = Semaphore(0)
            var result: PromiseResult<T>? = null
            subscribe {
                result = it
                semaphore.release()
            }
            semaphore.acquire()
            return result!!.get()
        }
    }

    override fun subscribe(callback: Consumer<PromiseResult<T>>) {
        loop {
            val state = _state.value
            if (
                state is RunningDispatcherContextImplState<T> &&
                _state.cas(state, NotEmptyRunningDispatcherContextImplState(callback, state))
            ) {
                return
            }
            if (state is CompletedDispatcherContextImplState<T>) {
                callCallback(callback, state.result)
                return
            }
        }
    }

    override val promise: Promise<T>
        get() = this

    override var dispatcher: Dispatcher? = null

    override val lastEvent: DispatcherEvent
        get() = _lastEvent.exchange(null) ?: error("Last event is not set.")

    override fun setAwaitLastEvent(callback: Consumer<Notifier>) {
        setLastEvent(AwaitDispatcherEvent(callback))
    }

    override fun setDelayLastEvent(duration: Duration) {
        setLastEvent(DelayDispatcherEvent(duration))
    }

    override fun setSwitchLastEvent(newDispatcher: Dispatcher) {
        setLastEvent(SwitchDispatcherEvent(newDispatcher))
    }

    override fun complete(result: PromiseResult<T>) {
        loop {
            var state = _state.value
            if (state is RunningDispatcherContextImplState<T>) {
                if (_state.cas(state, CompletedDispatcherContextImplState(result))) {
                    while (state is NotEmptyRunningDispatcherContextImplState<T>) {
                        callCallback(state.callback, result)
                        state = state.next
                    }
                    assert { state == EmptyRunningDispatcherContextImplState }
                    return
                }
            }
            if (state is CompletedDispatcherContextImplState) {
                error("Dispatcher context is already completed.")
            }
        }
    }

    private fun setLastEvent(event: DispatcherEvent) {
        if (!_lastEvent.cas(null, event)) {
            error("Last event is already set")
        }
    }
}

private sealed interface DispatcherContextImplState<out T> {
    val state: PromiseState
}

private sealed interface RunningDispatcherContextImplState<out T>: DispatcherContextImplState<T> {
    override val state: PromiseState
        get() = PromiseState.RUNNING
}

private data object EmptyRunningDispatcherContextImplState: RunningDispatcherContextImplState<Nothing>

private class NotEmptyRunningDispatcherContextImplState<T>(
    val callback: Consumer<PromiseResult<T>>,
    val next: RunningDispatcherContextImplState<T>
): RunningDispatcherContextImplState<T>

private class CompletedDispatcherContextImplState<out T>(val result: PromiseResult<T>): DispatcherContextImplState<T> {
    override val state: PromiseState
        get() = if (result.succeed) PromiseState.COMPLETED else PromiseState.EXCEPTIONAL
}

private fun <T> callCallback(callback: Consumer<T>, result: T) {
    try {
        callback(result)
    } catch (e: Throwable) {
        log.error(e) { "Callback [$callback] failed." }
    }
}
