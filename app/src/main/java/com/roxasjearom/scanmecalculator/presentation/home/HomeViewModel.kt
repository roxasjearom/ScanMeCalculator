package com.roxasjearom.scanmecalculator.presentation.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
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

    fun recognizeText(inputImage: InputImage?) {
        viewModelScope.launch {
            inputImage?.let {
                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                recognizer.process(inputImage)
                    .addOnSuccessListener { result ->
                        for (block in result.textBlocks) {
                            for (line in block.lines) {
                                Log.e("LINE", line.text)
                                if (isInputValid(line.text)) {
                                    _homeUiState.update {
                                        it.copy(
                                            input = line.text,
                                            result = getOperationResult(line.text)
                                        )
                                    }
                                    return@addOnSuccessListener
                                }
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        e.printStackTrace()
                    }
            }
        }
    }

    private fun isInputValid(input: String): Boolean {
        val splitInput = input.replace(" ", "").split('/', '*', '+', '-')
        return splitInput.size == 2
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
)
