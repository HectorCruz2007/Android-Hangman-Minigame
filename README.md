# Minigame — El Ahorcado

Aplicación de **Utilería** para Android que implementa el minijuego de **El Ahorcado** (adivinanza de palabras). Desarrollada con **Kotlin** y **Jetpack Compose**, aplicando MVVM, UDF, SRP, DIP y Atomic Design.

Proyecto de la materia *Desarrollo de Aplicaciones Móviles — Unidad I* (Ingeniería en Tecnologías de la Información e Innovación Digital).

---

## Descripción del juego

En cada partida se elige al azar una palabra misteriosa de una lista de ~100 palabras. El jugador ingresa una letra a la vez desde el teclado normal y la app valida la entrada:

| Entrada | Proceso | Salida |
|---|---|---|
| Una letra individual | Validar que sea A–Z, que no esté repetida y si existe en la palabra | `"Letra correcta en X espacios"` o `"Letra no encontrada"` |

- Si la letra existe, se revela en todos los espacios donde aparece.
- Si no existe, se suma un error y avanza la imagen del ahorcado.
- Se permiten **6 errores** como máximo (`HangmanRules.MAX_ERRORS`). Al llegar a 6 se pierde y se revela la palabra.
- Si se adivinan todas las letras, se gana.
- Al terminar la partida aparece el botón **Reiniciar**.

### Mensajes de salida

| Situación | Mensaje |
|---|---|
| Acierto (1 espacio) | `Letra correcta en 1 espacio` |
| Acierto (varios) | `Letra correcta en X espacios` |
| Fallo | `Letra no encontrada` |
| Carácter inválido | `Escribe una letra de la A a la Z` |
| Letra repetida | `Ya usaste la letra X` |
| Victoria | `¡Ganaste!` |
| Derrota | `Perdiste, la palabra era X` |

---

## Stack tecnológico

- **Lenguaje:** Kotlin
- **UI:** Jetpack Compose (Material 3)
- **Arquitectura:** MVVM + UDF
- **Estado:** `StateFlow` (`MutableStateFlow` / `asStateFlow`)
- **IDE:** Android Studio

---

## Arquitectura y principios

### MVVM
- **Model:** reglas del juego (`HangmanRules`), estado (`GameStatus`) y fuente de palabras (`WordProvider`).
- **ViewModel:** `HangmanVM` concentra toda la lógica: validación, conteo de espacios, errores, vidas, progreso y mensajes.
- **View:** composables que solo observan estado y reportan eventos.

### UDF (Unidirectional Data Flow)

```
 ┌──────────────┐   evento: onLetterInput(letra)   ┌─────────────┐
 │  Composable  │ ───────────────────────────────▶ │  HangmanVM  │
 │   (View)     │                                  │             │
 │              │ ◀─────────────────────────────── │  StateFlow  │
 └──────────────┘   estado: collectAsState()       └─────────────┘
```

1. El usuario escribe una letra y pulsa **Enviar** (o la tecla Send del teclado).
2. El composable llama a `viewModel.onLetterInput(letra)`.
3. El ViewModel valida, actualiza los `StateFlow` y ejecuta `updateGame()`.
4. La UI recompone automáticamente al recolectar los nuevos valores con `collectAsState()`.

El estado fluye **hacia abajo** y los eventos **hacia arriba**; la UI nunca modifica el estado directamente.

### SRP (Single Responsibility Principle)
| Archivo | Responsabilidad única |
|---|---|
| `HangmanRules.kt` | Constantes de reglas y enum de estado de la partida |
| `HangmanCheck.kt` | Funciones de extensión de validación y formateo |
| `WordProvider.kt` | Proveer la lista de palabras |
| `HangmanVM.kt` | Lógica y estado del juego |
| `HangmanVMpage.kt` | Composables de la pantalla |
| `MainActivity.kt` | Punto de entrada y tema |

### DIP (Dependency Inversion Principle)
`HangmanVM` no depende de Android ni de `R`: recibe una abstracción `WordProvider` en `startNewGame(wordProvider)`. La implementación concreta `ResourceWordProvider` (que lee `R.array.hangman_words`) se inyecta desde la capa de UI, lo que permite sustituirla por una implementación falsa en pruebas.

### Funciones de extensión
Definidas en `HangmanCheck.kt`:

| Función | Uso |
|---|---|
| `String.toLetterOrNull()` | Normaliza la entrada a mayúscula y valida A–Z |
| `String.toDisplayWord(guessed)` | Construye la palabra con guiones (`_ A _ _ O`) |
| `String.isGuessedWith(guessed)` | Verifica si ya se adivinó toda la palabra |
| `Int.toSpacesMessage()` | Mensaje de acierto con singular/plural |
| `Int.toErrorProgress()` | Convierte errores a un valor 0–1 para barras de progreso |
| `Int.toHangmanImage()` | Devuelve el drawable de la fase del ahorcado |

### Atomic Design
Los composables se organizan por nivel de complejidad:

| Nivel | Ejemplos |
|---|---|
| **Átomos** | `Text`, `Button`, `OutlinedTextField`, `Image`, `Spacer` |
| **Moléculas** | `LetterInput` (campo de letra + botón Enviar) |
| **Organismos** | Bloque del juego: imagen + palabra + mensaje + entrada/reinicio |
| **Páginas** | `HangmanPage` |

---

## Estructura del proyecto

```
com.zukesito.minigame
├── MainActivity.kt        # Entrada de la app y tema
├── HangmanVMpage.kt       # HangmanPage y LetterInput (composables)
├── HangmanVM.kt           # ViewModel con StateFlow
├── HangmanRules.kt        # MAX_ERRORS y GameStatus
├── HangmanCheck.kt        # Funciones de extensión
├── WordProvider.kt        # Interfaz + ResourceWordProvider
└── ui/theme/              # Tema Material 3

res/
├── values/words.xml       # string-array "hangman_words" (~100 palabras)
└── drawable/              # hangman_0 … hangman_6 (imágenes del ahorcado)
```

---

## Estado expuesto por el ViewModel

| StateFlow | Tipo | Descripción |
|---|---|---|
| `displayWord` | `String` | Palabra con guiones para mostrar |
| `correctLetters` | `List<Char>` | Letras acertadas |
| `wrongLetters` | `List<Char>` | Letras falladas |
| `errors` | `Int` | Errores cometidos |
| `remainingLives` | `Int` | Intentos restantes |
| `errorProgress` | `Float` | Progreso de errores (0f–1f) |
| `message` | `String` | Mensaje de salida |
| `lastGuessCorrect` | `Boolean?` | Resultado del último intento (`null` si fue inválido/repetido) |
| `status` | `GameStatus` | `PLAYING`, `WON` o `LOST` |

---

## Lista de palabras

Las palabras viven fuera del código en `res/values/words.xml` como un `string-array` y se acceden con `R.array.hangman_words`. Están en mayúsculas y **sin acentos ni ñ**, para ser compatibles con la validación A–Z.

```xml
<string-array name="hangman_words">
    <item>ARBOL</item>
    <item>CASA</item>
    <!-- ... -->
</string-array>
```

Para agregar palabras basta con añadir un `<item>` en mayúsculas.

---

## Imágenes del ahorcado

Cada fase corresponde al número de errores. Coloca tus imágenes en `res/drawable/` con estos nombres:

| Errores | Archivo |
|---|---|
| 0 | `hangman_0` |
| 1 | `hangman_1` |
| 2 | `hangman_2` |
| 3 | `hangman_3` |
| 4 | `hangman_4` |
| 5 | `hangman_5` |
| 6 | `hangman_6` |

La asignación está en `Int.toHangmanImage()` (`HangmanCheck.kt`).

---

## Cómo ejecutar

1. Clona el repositorio y ábrelo en **Android Studio**.
2. Verifica que existan las imágenes `hangman_0` a `hangman_6` en `res/drawable/`.
3. Sincroniza Gradle (*Sync Project with Gradle Files*).
4. Ejecuta en un emulador o dispositivo con el botón **Run**.
