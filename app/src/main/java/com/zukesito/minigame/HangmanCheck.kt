package com.zukesito.minigame

//Normalizar input
fun String.toLetterOrNull(): Char? {
    val letter = this.last().uppercaseChar()
    if (letter in 'A'..'Z') {
        return letter
    }
    return null
}

// Armar la palabra con guiones
fun String.toDisplayWord(guessedLetters: List<Char>): String {
    var result = ""
    for (c in this) {
        if (c in guessedLetters) {
            result += "$c "
        } else {
            result += "_ "
        }
    }
    return result.trim()
}

// Revisa si se adivinaron todas las letras
fun String.isGuessedWith(guessedLetters: List<Char>): Boolean {
    return this.all { it in guessedLetters }
}

// Mensaje de acierto
fun Int.toSpacesMessage(): String {
    if (this == 1) {
        return "Letra correcta en 1 espacio"
    }
    return "Letra correcta en $this espacios"
}

// Convierte los errores a un valor de 0 a 1 para la barra de progreso
fun Int.toErrorProgress(): Float {
    return this.toFloat() / HangmanRules.MAX_ERRORS
}