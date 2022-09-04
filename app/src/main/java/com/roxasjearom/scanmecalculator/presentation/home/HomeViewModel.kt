package com.roxasjearom.scanmecalculator.presentation.home

import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    private val _homeUiState = MutableStateFlow(HomeUiState())
    val homeUiState: StateFlow<HomeUiState> = _homeUiState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = _homeUiState.value
    )

    fun validateLines(textLines: List<String>) {
        viewModelScope.launch {
            for (textLine in textLines) {
                if (isInputValid(textLine)) {
                    _homeUiState.update { uiState ->
                        uiState.copy(
                            input = textLine,
                            result = getOperationResult(textLine)
                        )
                    }
                    return@launch
                }
            }
            _homeUiState.update { uiState ->
                uiState.copy(
                    input = "No valid input found",
                    result = "No result to show",
                )
            }
        }
    }

    private fun isInputValid(input: String): Boolean {
        val splitInput = input.replace(" ", "").split('/', '*', '+', '-')
        return splitInput.size == 2 && splitInput.all { it.isDigitsOnly() }
    }

    private fun getOperationResult(equation: String): String {
        val operands = equation.replace(" ", "").split('/', '*', '+', '-').map { it.toInt() }
        return when {
            equation.contains("+") -> {
                (operands[0] + operands[1]).toString()
            }
            equation.contains("-") -> {
                (operands[0] - operands[1]).toString()
            }
            equation.contains("*") -> {
                (operands[0] * operands[1]).toString()
            }
            equation.contains("/") -> {
                (operands[0] / operands[1]).toString()
            }
            else -> "Unsupported operation"
        }
    }
}

data class HomeUiState(
    val input: String = "",
    val result: String = "",
    val hasNoResult: Boolean = false,
)
