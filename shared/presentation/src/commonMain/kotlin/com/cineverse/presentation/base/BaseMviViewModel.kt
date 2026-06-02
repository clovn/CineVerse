package com.cineverse.presentation.base

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

abstract class BaseMviViewModel<State, Intent, Effect>(initialState: State) {
    
    // Pure Kotlin ViewModelScope that works on both Android and iOS
    protected val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<State> = _state.asStateFlow()
    
    private val _effect = MutableSharedFlow<Effect>()
    val effect: SharedFlow<Effect> = _effect.asSharedFlow()
    
    fun sendIntent(intent: Intent) {
        handleIntent(intent)
    }
    
    protected abstract fun handleIntent(intent: Intent)
    
    protected fun updateState(reducer: (State) -> State) {
        _state.value = reducer(_state.value)
    }
    
    protected fun sendEffect(effect: Effect) {
        viewModelScope.launch {
            _effect.emit(effect)
        }
    }
}
