package com.roxasjearom.scanmecalculator.presentation.home

import com.google.common.truth.Truth.assertThat
import com.roxasjearom.scanmecalculator.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var underTest: HomeViewModel

    @Before
    fun setup() {
        underTest = HomeViewModel()
    }

    @Test
    fun `Should have the Success state result when the input is valid`() = runTest {
        //Given a valid input
        val validInput = listOf("25+25")

        //When we call the validation
        underTest.validateLines(validInput)
        val actualState = underTest.homeUiState.first()

        //Then the UI state should has the success state
        assertThat(actualState.textResult).isInstanceOf(TextResult.Success::class.java)
        val successResult = actualState.textResult as TextResult.Success
        assertThat(successResult.input).isEqualTo("25+25")
        assertThat(successResult.result).isEqualTo("50")
    }

    @Test
    fun `Should compute the first valid equation when there's multiple valid inputs`() = runTest {
        //Given multiple valid input
        val validInput = listOf("10+10", "25+25")

        //When we call the validation
        underTest.validateLines(validInput)
        val actualState = underTest.homeUiState.first()

        //Then the UI state should compute the first valid equation
        assertThat(actualState.textResult).isInstanceOf(TextResult.Success::class.java)
        val successResult = actualState.textResult as TextResult.Success
        assertThat(successResult.input).isEqualTo("10+10")
        assertThat(successResult.result).isEqualTo("20")
    }

    @Test
    fun `Should compute the first valid equation when there's a mix of valid and invalid inputs`() =
        runTest {
            //Given a mix of valid and invalid inputs
            val validInput = listOf("abc", "xyz", "15+15")

            //When we call the validation
            underTest.validateLines(validInput)
            val actualState = underTest.homeUiState.first()

            //Then the UI state should compute the first valid equation
            assertThat(actualState.textResult).isInstanceOf(TextResult.Success::class.java)
            val successResult = actualState.textResult as TextResult.Success
            assertThat(successResult.input).isEqualTo("15+15")
            assertThat(successResult.result).isEqualTo("30")
        }

    @Test
    fun `Should have the NoResultFound state when the input is invalid`() = runTest {
        //Given the valid input
        val validInput = listOf("abcd")

        //When we call the validation
        underTest.validateLines(validInput)
        val actualState = underTest.homeUiState.first()

        //Then the UI state should has the NoResultFound state
        assertThat(actualState.textResult).isInstanceOf(TextResult.NoResultFound::class.java)
    }
}
