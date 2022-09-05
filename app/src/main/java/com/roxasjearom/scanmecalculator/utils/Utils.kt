package com.roxasjearom.scanmecalculator.utils

fun String.removeSpace() = this.replace(" ", "")

fun String.isDigit(): Boolean {
    return this.all { char -> char.isDigit() }
}
