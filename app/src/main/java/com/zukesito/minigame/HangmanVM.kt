package com.zukesito.minigame

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HangmanVM : ViewModel() {

    // Palabra misteriosa
    private var secretWord = ""

    // Palabra que ve el usuario
    private var _displayWord = MutableStateFlow("")
    val displayWord: StateFlow<String> = _displayWord.asStateFlow()

    // Letras acertadas y falladas
    private var _correctLetters = MutableStateFlow<List<Char>>(emptyList())
    val correctLetters: StateFlow<List<Char>> = _correctLetters.asStateFlow()

    private var _wrongLetters = MutableStateFlow<List<Char>>(emptyList())
    val wrongLetters: StateFlow<List<Char>> = _wrongLetters.asStateFlow()

    // Errores cometidos
    private var _errors = MutableStateFlow(0)
    val errors: StateFlow<Int> = _errors.asStateFlow()

    // Intentos restantes
    private var _remainingLives = MutableStateFlow(HangmanRules.MAX_ERRORS)
    val remainingLives: StateFlow<Int> = _remainingLives.asStateFlow()

    // Valor para la barra de progreso de errores
    private var _errorProgress = MutableStateFlow(0f)
    val errorProgress: StateFlow<Float> = _errorProgress.asStateFlow()

    // Mensaje de salida
    private var _message = MutableStateFlow("")
    val message: StateFlow<String> = _message.asStateFlow()

    // Validar input
    private var _lastGuessCorrect = MutableStateFlow<Boolean?>(null)
    val lastGuessCorrect: StateFlow<Boolean?> = _lastGuessCorrect.asStateFlow()

    // Estado del juego
    private var _status = MutableStateFlow(GameStatus.PLAYING)
    val status: StateFlow<GameStatus> = _status.asStateFlow()

    // Recibir input
    fun onLetterInput(value: String) {
        // Si la partida ya termino o no se escribio nada, no hace nada
        if (_status.value != GameStatus.PLAYING || value.isEmpty()) {
            return
        }

        // Validación A-Z
        val letter = value.toLetterOrNull()
        if (letter == null) {
            _message.value = "Escribe una letra de la A a la Z"
            _lastGuessCorrect.value = null
            return
        }

        // Validacion no repetición
        if (letter in _correctLetters.value || letter in _wrongLetters.value) {
            _message.value = "Ya usaste la letra $letter"
            _lastGuessCorrect.value = null
            return
        }

        // Cuenta espaciado
        val spaces = secretWord.count { it == letter }

        if (spaces > 0) {
            _correctLetters.value = _correctLetters.value + letter
            _message.value = spaces.toSpacesMessage()
            _lastGuessCorrect.value = true
        } else {
            _wrongLetters.value = _wrongLetters.value + letter
            _errors.value++
            _message.value = "Letra no encontrada"
            _lastGuessCorrect.value = false
        }

        updateGame()
    }

    // Nueva partida
    fun startNewGame(wordProvider: WordProvider) {
        secretWord = wordProvider.getWords().random()
        _correctLetters.value = emptyList()
        _wrongLetters.value = emptyList()
        _errors.value = 0
        _status.value = GameStatus.PLAYING
        _message.value = "Adivina la palabra"
        _lastGuessCorrect.value = null
        updateGame()
    }

    // Actualiza estados
    private fun updateGame() {
        _displayWord.value = secretWord.toDisplayWord(_correctLetters.value)
        _remainingLives.value = HangmanRules.MAX_ERRORS - _errors.value
        _errorProgress.value = _errors.value.toErrorProgress()

        if (secretWord.isGuessedWith(_correctLetters.value)) {
            _status.value = GameStatus.WON
            _message.value = "¡Ganaste!"
        } else if (_errors.value >= HangmanRules.MAX_ERRORS) {
            _status.value = GameStatus.LOST
            // Al perder se muestra la palabra completa
            _displayWord.value = secretWord.toDisplayWord(secretWord.toList())
            _message.value = "Perdiste, la palabra era $secretWord"
        }
    }
}