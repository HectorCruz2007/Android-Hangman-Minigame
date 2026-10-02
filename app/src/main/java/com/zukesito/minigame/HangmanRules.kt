package com.zukesito.minigame

// Reglas de juego
object HangmanRules {
    const val MAX_ERRORS = 6
}

// Estado de la partida
enum class GameStatus { PLAYING, WON, LOST }