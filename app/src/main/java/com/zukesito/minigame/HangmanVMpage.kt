package com.zukesito.minigame

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.saveable.rememberSaveable

@Composable
fun HangmanPage(
    viewModel: HangmanVM = viewModel()
) {
    val context = LocalContext.current

    // Lectura de estados
    val displayWord by viewModel.displayWord.collectAsState()
    val errors by viewModel.errors.collectAsState()
    val message by viewModel.message.collectAsState()
    val status by viewModel.status.collectAsState()

    // Inicializa el juego
    LaunchedEffect(Unit) {
        val wordProvider = ResourceWordProvider(context.resources)
        viewModel.startGameIfNeeded(wordProvider)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Imagen del ahorcado
        val imageRes = errors.toHangmanImage()
        if (imageRes != null) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = null,
                modifier = Modifier.size(180.dp)
            )
        } else {
            Spacer(modifier = Modifier.size(180.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Palabra con guiones
        Text(
            text = displayWord,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 4.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Mensaje de estado
        Text(
            text = message,
            fontSize = 18.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Input de letra o reinicio
        if (status == GameStatus.PLAYING) {
            LetterInput(onLetterSubmit = { letter -> viewModel.onLetterInput(letter) })
        } else {
            Button(
                onClick = {
                val wordProvider = ResourceWordProvider(context.resources)
                viewModel.startNewGame(wordProvider) }
            )
            {
                Text("Reiniciar")
            }
        }
    }
}

@Composable
fun LetterInput(onLetterSubmit: (String) -> Unit) {
    var textInput by rememberSaveable { mutableStateOf("") }

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = textInput,
            onValueChange = { newValue ->
                textInput = if (newValue.length <= 1) newValue else newValue.last().toString()
            },
            label = { Text("Letra") },
            singleLine = true,
            modifier = Modifier.width(80.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                capitalization = KeyboardCapitalization.Characters,
                imeAction = ImeAction.Send
            ),
            keyboardActions = KeyboardActions(
                onSend = {
                    if (textInput.isNotEmpty()) {
                        onLetterSubmit(textInput)
                        textInput = ""
                    }
                }
            )
        )

        Spacer(modifier = Modifier.width(12.dp))

        Button(
            onClick = {
                if (textInput.isNotEmpty()) {
                    onLetterSubmit(textInput)
                    textInput = ""
                }
            }
        ) {
            Text("Enviar")
        }
    }
}