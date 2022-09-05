package com.roxasjearom.scanmecalculator.presentation.home

import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roxasjearom.scanmecalculator.utils.removeSpace
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
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
        viewModelScope.launch(Dispatchers.IO) {
            textLines.forEach { textLine ->
                if (isInputValid(textLine.removeSpace())) {
                    _homeUiState.update { uiState ->
                        uiState.copy(
                            textResult = TextResult.Success(
                                input = textLine,
                                result = getOperationResult(textLine)
                            ),
                        )
                    }
                    return@launch
                }
            }
            _homeUiState.update { uiState ->
                uiState.copy(textResult = TextResult.NoResultFound)
            }
        }
    }

    private fun isInputValid(input: String): Boolean {
        val splitInput = input.split('/', '*', '+', '-')
        return splitInput.size == 2 && splitInput.all { it.isDigitsOnly() && it.isNotEmpty() }
    }

    private fun getOperationResult(equation: String): String {
        val operands = equation.split('/', '*', '+', '-').map { it.toInt() }
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
    val textResult: TextResult = TextResult.InitialState,
)
