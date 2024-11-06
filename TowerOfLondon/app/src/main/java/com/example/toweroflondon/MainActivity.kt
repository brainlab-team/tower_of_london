package com.example.toweroflondon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.toweroflondon.ui.theme.TowerOfLondonTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TowerOfLondonTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(1200F)
                ){
                    TowerOfLondonContent()
                }
            }
        }
    }

    data class Ball(val name: String,val color: Color)
    data class Stick(val name: String, var balls: MutableList<Ball> = mutableListOf(), val size: Int)
    class Counter {
        var value by mutableIntStateOf(60)
            private set

        fun decrement() {
            value--
        }

        fun reset() {
            value = 60
        }
    }

    private var start : Boolean = false
    private var completed : Boolean = false

    private var id: Int = 0
    private var phase : Int = 0
    private var modality: Int = 0
    private var numberOfMove : Int = 0
    private var numberOfHint : Int = 0
    private var numberOfPlay : Int = 0
    private var numberOfMovesAfterHint : Int = -2
    private var numberOfTest : Int = 0
    private var numberOfMovesMin : Int = 0

    private val stick1 = mutableStateOf(Stick("stick1", balls = mutableStateListOf(), size = 3))
    private val stick2 = mutableStateOf(Stick("stick2", balls = mutableStateListOf(), size = 2))
    private val stick3 = mutableStateOf(Stick("stick3", balls = mutableStateListOf(), size = 1))

    private val stickFinal1 = mutableStateOf(Stick("stickFinal1", balls = mutableListOf(), size = 3))
    private val stickFinal2 = mutableStateOf(Stick("stickFinal2",balls = mutableListOf(), size = 2))
    private val stickFinal3 = mutableStateOf(Stick("stickFinal3", balls = mutableListOf(), size = 1))

    private val ballRed = Ball("ballRed", Color.Red)
    private val ballGreen = Ball("ballGreen", Color.Green)
    private val ballBlue = Ball("ballBlue", Color.Blue)
    private val ballFinalRed = Ball("ballFinalRed", Color.Red)
    private val ballFinalGreen = Ball("ballFinalGreen", Color.Green)
    private val ballFinalBlue = Ball("ballFinalBlue", Color.Blue)

    private val counter = Counter()

    private val namesOfPhase = arrayOf("Baseline", "Training", "Assessment")

    private fun initializeSticks() {
        //funzione che si occupa di inizializzare le palline sui bastoncini
        //le configurazioni sono state impostate con un livello di difficoltà graduale e per fase
        //per la fase baseline e assessment sono uguali mentre per la training no
        //per ogni configurazione c'è un numero ottimale di mosse
        when (phase) {
            1 -> {
                when (numberOfPlay) {
                    0 -> {
                        stick1.value.balls.add(0, ballGreen)
                        stick1.value.balls.add(0, ballRed)
                        stick2.value.balls.add(0, ballBlue)
                        stickFinal1.value.balls.add(0, ballFinalGreen)
                        stickFinal1.value.balls.add(0, ballFinalBlue)
                        stickFinal1.value.balls.add(0, ballFinalRed)
                        numberOfMovesMin = 3
                    }

                    1 -> {
                        stick1.value.balls.add(0, ballRed)
                        stick2.value.balls.add(0, ballGreen)
                        stick3.value.balls.add(0, ballBlue)
                        stickFinal1.value.balls.add(0, ballFinalBlue)
                        stickFinal2.value.balls.add(0, ballFinalRed)
                        stickFinal3.value.balls.add(0, ballFinalGreen)
                        numberOfMovesMin = 5
                    }

                    2 -> {
                        stick1.value.balls.add(0, ballRed)
                        stick2.value.balls.add(0, ballBlue)
                        stick3.value.balls.add(0, ballGreen)
                        stickFinal1.value.balls.add(0, ballFinalBlue)
                        stickFinal1.value.balls.add(0, ballFinalGreen)
                        stickFinal3.value.balls.add(0, ballFinalRed)
                        numberOfMovesMin = 6
                    }

                    3 -> {
                        stick1.value.balls.add(0, ballBlue)
                        stick1.value.balls.add(0, ballRed)
                        stick1.value.balls.add(0, ballGreen)
                        stickFinal1.value.balls.add(0, ballFinalRed)
                        stickFinal2.value.balls.add(0, ballFinalBlue)
                        stickFinal2.value.balls.add(0, ballFinalGreen)
                        numberOfMovesMin = 7
                    }

                    else -> {
                        resetVariables()
                        println("test finished")
                        numberOfMovesMin = 0
                    }
                }
            }
            3 -> {
                when (numberOfPlay) {
                    0 -> {
                        stick2.value.balls.add(0,ballRed)
                        stick2.value.balls.add(0,ballBlue)
                        stick3.value.balls.add(0,ballGreen)
                        stickFinal1.value.balls.add(0,ballFinalBlue)
                        stickFinal2.value.balls.add(0,ballFinalGreen)
                        stickFinal3.value.balls.add(0,ballFinalRed)
                        numberOfMovesMin = 4
                    }

                    1 -> {
                        stick1.value.balls.add(0,ballRed)
                        stick1.value.balls.add(0,ballBlue)
                        stick1.value.balls.add(0,ballGreen)
                        stickFinal1.value.balls.add(0,ballFinalBlue)
                        stickFinal1.value.balls.add(0,ballFinalRed)
                        stickFinal2.value.balls.add(0,ballFinalGreen)
                        numberOfMovesMin = 5
                    }

                    2 -> {
                        stick1.value.balls.add(0,ballRed)
                        stick2.value.balls.add(0,ballBlue)
                        stick3.value.balls.add(0,ballGreen)
                        stickFinal1.value.balls.add(0,ballFinalBlue)
                        stickFinal1.value.balls.add(0,ballFinalGreen)
                        stickFinal3.value.balls.add(0,ballFinalRed)
                        numberOfMovesMin = 6
                    }

                    3 -> {
                        stick1.value.balls.add(0,ballBlue)
                        stick1.value.balls.add(0,ballGreen)
                        stick2.value.balls.add(0,ballRed)
                        stickFinal1.value.balls.add(0,ballFinalRed)
                        stickFinal2.value.balls.add(0,ballFinalBlue)
                        stickFinal2.value.balls.add(0,ballFinalGreen)
                        numberOfMovesMin = 8
                    }

                    else -> {
                        resetVariables()
                        println("test finished")
                        numberOfMovesMin = 0
                    }
                }
            }
            else -> {
                when (numberOfPlay) {
                    0 -> {
                        start = true
                        stick1.value.balls.add(0,ballBlue)
                        stick1.value.balls.add(0,ballGreen)
                        stick2.value.balls.add(0,ballRed)
                        stickFinal1.value.balls.add(0,ballFinalBlue)
                        stickFinal2.value.balls.add(0,ballFinalRed)
                        stickFinal2.value.balls.add(0,ballFinalGreen)
                        numberOfMovesMin = 1
                    }
                    1 -> {
                        stick1.value.balls.add(0,ballBlue)
                        stick2.value.balls.add(0,ballGreen)
                        stick3.value.balls.add(0,ballRed)
                        stickFinal1.value.balls.add(0,ballFinalBlue)
                        stickFinal1.value.balls.add(0,ballFinalGreen)
                        stickFinal1.value.balls.add(0,ballFinalRed)
                        numberOfMovesMin = 2
                    }
                    2 -> {
                        stick1.value.balls.add(0,ballGreen)
                        stick1.value.balls.add(0,ballBlue)
                        stick1.value.balls.add(0,ballRed)
                        stickFinal1.value.balls.add(0,ballFinalGreen)
                        stickFinal1.value.balls.add(0,ballFinalRed)
                        stickFinal2.value.balls.add(0,ballFinalBlue)
                        numberOfMovesMin = 3
                    }
                    3 -> {
                        stick1.value.balls.add(0,ballBlue)
                        stick2.value.balls.add(0,ballGreen)
                        stick3.value.balls.add(0,ballRed)
                        stickFinal1.value.balls.add(0,ballFinalRed)
                        stickFinal1.value.balls.add(0,ballFinalBlue)
                        stickFinal2.value.balls.add(0,ballFinalGreen)
                        numberOfMovesMin = 3
                    }
                    4 -> {
                        stick1.value.balls.add(0,ballBlue)
                        stick1.value.balls.add(0,ballGreen)
                        stick3.value.balls.add(0,ballRed)
                        stickFinal1.value.balls.add(0,ballFinalGreen)
                        stickFinal2.value.balls.add(0,ballFinalRed)
                        stickFinal2.value.balls.add(0,ballFinalBlue)
                        numberOfMovesMin = 4
                    }
                    5 -> {
                        stick1.value.balls.add(0,ballRed)
                        stick1.value.balls.add(0,ballGreen)
                        stick1.value.balls.add(0,ballBlue)
                        stickFinal1.value.balls.add(0,ballFinalRed)
                        stickFinal1.value.balls.add(0,ballFinalBlue)
                        stickFinal1.value.balls.add(0,ballFinalGreen)
                        numberOfMovesMin = 4
                    }
                    6 -> {
                        stick1.value.balls.add(0,ballBlue)
                        stick2.value.balls.add(0,ballRed)
                        stick3.value.balls.add(0,ballGreen)
                        stickFinal1.value.balls.add(0,ballFinalRed)
                        stickFinal2.value.balls.add(0,ballFinalGreen)
                        stickFinal3.value.balls.add(0,ballFinalBlue)
                        numberOfMovesMin = 5
                    }
                    7 -> {
                        stick1.value.balls.add(0,ballBlue)
                        stick1.value.balls.add(0,ballRed)
                        stick2.value.balls.add(0,ballGreen)
                        stickFinal1.value.balls.add(0,ballFinalRed)
                        stickFinal1.value.balls.add(0,ballFinalBlue)
                        stickFinal1.value.balls.add(0,ballFinalGreen)
                        numberOfMovesMin = 5
                    }
                    8 -> {
                        stick1.value.balls.add(0,ballRed)
                        stick1.value.balls.add(0,ballGreen)
                        stick3.value.balls.add(0,ballBlue)
                        stickFinal1.value.balls.add(0,ballFinalBlue)
                        stickFinal1.value.balls.add(0,ballFinalGreen)
                        stickFinal2.value.balls.add(0,ballFinalRed)
                        numberOfMovesMin = 6
                    }
                    9 -> {
                        stick1.value.balls.add(0,ballBlue)
                        stick1.value.balls.add(0,ballGreen)
                        stick3.value.balls.add(0,ballRed)
                        stickFinal1.value.balls.add(0,ballFinalRed)
                        stickFinal2.value.balls.add(0,ballFinalBlue)
                        stickFinal3.value.balls.add(0,ballFinalGreen)
                        numberOfMovesMin = 6
                    }
                    10 -> {
                        stick1.value.balls.add(0,ballRed)
                        stick1.value.balls.add(0,ballBlue)
                        stick1.value.balls.add(0,ballGreen)
                        stickFinal1.value.balls.add(0,ballFinalBlue)
                        stickFinal2.value.balls.add(0,ballFinalRed)
                        stickFinal2.value.balls.add(0,ballFinalGreen)
                        numberOfMovesMin = 7
                    }
                    11 -> {
                        stick1.value.balls.add(0,ballRed)
                        stick2.value.balls.add(0,ballBlue)
                        stick2.value.balls.add(0,ballGreen)
                        stickFinal1.value.balls.add(0,ballFinalBlue)
                        stickFinal1.value.balls.add(0,ballFinalGreen)
                        stickFinal2.value.balls.add(0,ballFinalRed)
                        numberOfMovesMin = 8
                    }
                    else -> {
                        resetVariables()
                        println("test finished")
                        numberOfMovesMin = 0
                    }
                }
            }
        }
    }

    private fun findStickUnderPosition(position: Float, stick : Stick): Stick {
        //funzione che ricerca a seconda dei valori del dragging il bastoncino
        //su cui si è cercato di spostare la pallina
        //i valori sono stati calcolati con prove effettive e ridimensionati per
        //lo schermo di Pepper
        val stickIndex : Int
        when (stick.name) {
            "stick1" -> {
                stickIndex = when (position) {
                    in 33f..333f -> 1
                    in 334f..666f -> 2
                    else -> 0
                }
            }
            "stick2" -> {
                stickIndex = when (position) {
                    in 100f..400f -> 2
                    in -333f..-133f -> 0
                    else -> 1
                }
            }
            else -> {
                stickIndex = when (position) {
                    in -333f..-113f -> 1
                    in -666f..-333f -> 0
                    else -> 2
                }
            }
        }

        println("Stick found: $stickIndex")
        return when (stickIndex) {
            0 -> stick1.value
            1 -> stick2.value
            2 -> stick3.value
            else -> throw IllegalArgumentException("Asta non trovata")
        }
    }

    private fun moveball(ball: Ball, targetStick: Stick) {
        //funzione che verifica se il movimento è valido e poi richiama updateSticks per
        //l'aggiornamento logico del movimento della pallina
        val sourceStick = findStickContainingball(ball)

        //se il movimento è valido
        if (targetStick.size - targetStick.balls.size > 0) {
            println("Can be placed")
            updateSticks(sourceStick, targetStick, ball)
        } else {
            println("Can't be placed")
        }
    }

    private fun findStickContainingball(ball: Ball): Stick {
        //funzione che si occupa di calcolare il bastoncino
        //in cui si trova la pallina passata
        if (ball in stick1.value.balls) return stick1.value
        if (ball in stick2.value.balls) return stick2.value
        if (ball in stick3.value.balls) return stick3.value

        // Se non viene trovata alcuna lista, gestisci il caso a tuo piacimento
        throw IllegalArgumentException("The selected ball hasn't stick")
    }

    private fun updateSticks(sourceStick: Stick, targetStick: Stick, ball: Ball) {
        //funzione che aggiorna lo stato logico dei bastoncini richiamando la
        //ricostruzione grafica della schermata
        sourceStick.balls.remove(ball)
        targetStick.balls.add(0, ball)

        //incrementiamo il numero di mosse soltanto se la pallina si è spostata
        //da un bastoncino all'altro
        if(sourceStick != targetStick) {numberOfMove++}
        println("moves: $numberOfMove")
        println("ball: ${ball.name} moved from ${sourceStick.name} to ${targetStick.name}")

        //dopo il movimento controlliamo se l'utente ha ottenuto la configurazione finale
        checkConfiguration()
    }

    private fun checkConfiguration() {
        //funzione che controlla se la configurazione finale è stata raggiunta
        //mappiamo i colori di entrambe le configurazioni e li controlliamo
        val colorsStick1 = stick1.value.balls.map { it.color }
        val colorsStick2 = stick2.value.balls.map { it.color }
        val colorsStick3 = stick3.value.balls.map { it.color }

        val colorsStickFinal1 = stickFinal1.value.balls.map { it.color }
        val colorsStickFinal2 = stickFinal2.value.balls.map { it.color }
        val colorsStickFinal3 = stickFinal3.value.balls.map { it.color }

        if(colorsStick1 == colorsStickFinal1){
            if(colorsStick2 == colorsStickFinal2){
                if(colorsStick3 == colorsStickFinal3){
                    println("all correct")
                    completed = true
                }
            }
        }
    }

    private fun resetVariables() {
        //funzione dovuta al ripristino delle variabili chiamata dopo ogni test
        stick1.value = Stick("stick1", balls = mutableStateListOf(), size = 3)
        stick2.value = Stick("stick2", balls = mutableStateListOf(), size = 2)
        stick3.value = Stick("stick3", balls = mutableStateListOf(), size = 1)

        stickFinal1.value = Stick("stickFinal1", balls = mutableListOf(), size = 3)
        stickFinal2.value = Stick("stickFinal2",balls = mutableListOf(), size = 2)
        stickFinal3.value = Stick("stickFinal3", balls = mutableListOf(), size = 1)
        numberOfMove = 0
        numberOfHint = 0
        numberOfMovesAfterHint = 0
        completed = false
        start = false
    }

    private suspend fun sendDataToServer(vararg params: String): Boolean {
        //funzione che si occupa di inviare i dati al server da salvare nel db
        return withContext(Dispatchers.IO) {
            val url = URL("http://10.113.123.210:8080")
            //val url = URL("http://192.168.1.27:8080")
            val connection = url.openConnection() as HttpURLConnection

            return@withContext try {
                connection.requestMethod = "POST"
                connection.connectTimeout = 5000
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")

                val jsonData = JSONObject().apply {
                    put("result", params.getOrNull(0))
                    put("test", params.getOrNull(1))
                    put("time", params.getOrNull(2))
                    put("moves", params.getOrNull(3))
                    put("hints", params.getOrNull(4))
                    put("modality", params.getOrNull(5))
                    put("user", params.getOrNull(6))
                    put("minimumMoves", params.getOrNull((7)))
                }

                val outputStream = connection.outputStream
                val outputStreamWriter = OutputStreamWriter(outputStream, "UTF-8")

                outputStreamWriter.use {
                    it.write(jsonData.toString())
                    it.flush()
                }

                val responseCode = connection.responseCode
                responseCode == HttpURLConnection.HTTP_OK
            } catch (e: IOException) {
                e.printStackTrace()
                false
            } finally {
                connection.disconnect()
            }
        }
    }

    private fun colorToString(color: Color): String {
        //funzione che serve per convertire il valore Color.* in una stringa
        //senza si invierebbe un dato del tipo [[0,1],[0,0]]
        return when (color) {
            Color.Green -> "green"
            Color.Red -> "red"
            Color.Blue -> "blue"
            else -> "unknown" // Gestione dei colori non riconosciuti, se necessario
        }
    }

    private suspend fun sendConfiguration(): Boolean {
        //funzione che serve per inviare le configurazione al server e ricevere
        //un suggerimento da pepper
        return withContext(Dispatchers.IO) {
            val url = URL("http://10.113.123.210:8080")
            //val url = URL("http://192.168.1.27:8080")
            val connection = url.openConnection() as HttpURLConnection

            return@withContext try {
                connection.requestMethod = "POST"
                connection.connectTimeout = 5000
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")

                //inviamo tutti i dati necessari per conoscere le configurazioni
                val jsonData = JSONObject().apply {
                    put("stick1", JSONArray(stick1.value.balls.map { colorToString(it.color) }))
                    put("stick2", JSONArray(stick2.value.balls.map { colorToString(it.color) }))
                    put("stick3", JSONArray(stick3.value.balls.map { colorToString(it.color) }))
                    put("stickFinal1", JSONArray(stickFinal1.value.balls.map { colorToString(it.color) }))
                    put("stickFinal2", JSONArray(stickFinal2.value.balls.map { colorToString(it.color) }))
                    put("stickFinal3", JSONArray(stickFinal3.value.balls.map { colorToString(it.color) }))
                }

                val outputStream = connection.outputStream
                val outputStreamWriter = OutputStreamWriter(outputStream, "UTF-8")

                outputStreamWriter.write(jsonData.toString())
                outputStreamWriter.flush()

                //verifichiamo la risposta del server
                connection.responseCode == HttpURLConnection.HTTP_OK
            } catch (e: IOException) {
                e.printStackTrace()
                false
            } finally {
                connection.disconnect()
            }
        }
    }

    private suspend fun sendMessageConsecutiveHint(): Boolean {
        //funzione che invia al server un flag di troppe richieste di aiuto e fa
        //intervenire pepper
        return withContext(Dispatchers.IO) {
            val url = URL("http://10.113.123.210:8080")
            //val url = URL("http://192.168.1.27:8080")
            val connection = url.openConnection() as HttpURLConnection

            return@withContext try {
                connection.requestMethod = "POST"
                connection.connectTimeout = 5000
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")

                val consecutiveHint = true
                val jsonData = JSONObject().apply {
                    put("consecutiveHint", consecutiveHint.toString())
                }

                val outputStream = connection.outputStream
                val outputStreamWriter = OutputStreamWriter(outputStream, "UTF-8")

                outputStreamWriter.write(jsonData.toString())
                outputStreamWriter.flush()

                //verifichiamo la risposta del server
                connection.responseCode == HttpURLConnection.HTTP_OK
            } catch (e: IOException) {
                e.printStackTrace()
                false
            } finally {
                connection.disconnect()
            }
        }
    }

    private suspend fun sendMessageOfAviableHelp(): Boolean {
        //funzione che invia al server un flag di possibile richiesta di aiuto e fa
        //intervenire pepper
        return withContext(Dispatchers.IO) {
            val url = URL("http://10.113.123.210:8080")
            //val url = URL("http://192.168.1.27:8080")
            val connection = url.openConnection() as HttpURLConnection

            return@withContext try {
                connection.requestMethod = "POST"
                connection.connectTimeout = 5000
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")

                val aviableHelp = true
                val jsonData = JSONObject().apply {
                    put("aviableHelp", aviableHelp.toString())
                }

                val outputStream = connection.outputStream
                val outputStreamWriter = OutputStreamWriter(outputStream, "UTF-8")

                outputStreamWriter.write(jsonData.toString())
                outputStreamWriter.flush()

                //verifichiamo la risposta del server
                connection.responseCode == HttpURLConnection.HTTP_OK
            } catch (e: IOException) {
                e.printStackTrace()
                false
            } finally {
                connection.disconnect()
            }
        }
    }

    private suspend fun explainTest(): Boolean {
        //funzione che fa intervenire Pepper per la spiegazione del test
        return withContext(Dispatchers.IO) {
            val url = URL("http://10.113.123.210:8080")
            //val url = URL("http://192.168.1.27:8080")
            val connection = url.openConnection() as HttpURLConnection

            return@withContext try {
                connection.requestMethod = "POST"
                connection.connectTimeout = 5000
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")

                val explainTest = "test"
                val jsonData = JSONObject().apply {
                    put("explainTest", explainTest)
                }

                val outputStream = connection.outputStream
                val outputStreamWriter = OutputStreamWriter(outputStream, "UTF-8")

                outputStreamWriter.write(jsonData.toString())
                outputStreamWriter.flush()

                //verifichiamo la risposta del server
                connection.responseCode == HttpURLConnection.HTTP_OK
            } catch (e: IOException) {
                e.printStackTrace()
                false
            } finally {
                connection.disconnect()
            }
        }
    }

    @Composable
    fun InsertID(onChosenID: (Int) -> Unit) {
        //composable incaricata di mostrare una schermata di inserimento dell'ID
        var inputValue by remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))
            Image(
                painter = painterResource(id = R.drawable.app_icon),
                contentDescription = "Logo",
                modifier = Modifier.size(150.dp) // Imposta le dimensioni desiderate, ad esempio 48.dp
            )
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = inputValue,
                onValueChange = { newValue ->
                    inputValue = newValue
                },
                label = { Text("Inserisci il tuo ID") },
                modifier = Modifier
                    .width(300.dp)
                    .padding(bottom = 16.dp)
            )

            Button(
                onClick = {
                    // Controlla se il valore inserito è un intero valido
                    val intValue = inputValue.toIntOrNull()
                    if (intValue != null) {
                        onChosenID(intValue)
                        id = intValue
                    } else {
                        // Gestione degli errori per input non valido
                        // (Puoi aggiungere una notifica all'utente o altre azioni qui)
                    }
                    CoroutineScope(Dispatchers.Main).launch {
                        //chiediamo a pepper di spiegarci il gioco
                        val success = explainTest()
                        if (success) {
                            // Invio riuscito, puoi gestire qui eventuali azioni da eseguire
                            println("Phase chosen correctly")
                        } else {
                            // Gestire l'invio fallito qui
                            println("Phase doesn't choesn")
                        }
                    }
                }
            ) {
                Text("Conferma")
            }
        }
    }

    @Composable
    fun ChooseModality(onChosenModality: (Int) -> Unit) {
        //funzione che mostra due button con la scelta modalità
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))
            Image(
                painter = painterResource(id = R.drawable.app_icon),
                contentDescription = "Logo",
                modifier = Modifier.size(150.dp) // Imposta le dimensioni desiderate, ad esempio 48.dp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = { onChosenModality(1)},
                    modifier = Modifier.padding(end = 16.dp)
                ) {
                    Text("Full support")
                }
                Button(
                    onClick = { onChosenModality(2) },
                ) {
                    Text("Fostering Autonomy")
                }
            }
        }
    }

    @Composable
    fun ChoosePhase(onChosenPhase: (Int) -> Unit) {
        //funzione che mostra tre button con la scelta fase
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))
            Image(
                painter = painterResource(id = R.drawable.app_icon),
                contentDescription = "Logo",
                modifier = Modifier.size(150.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = { onChosenPhase(1)
                    },
                    enabled = numberOfTest == 0, // Abilita il pulsante solo se numberOfTest è 0
                    modifier = Modifier.padding(end = 16.dp)
                ) {
                    Text("Baseline")
                }
                Button(
                    onClick = { onChosenPhase(2)
                    },
                    enabled = numberOfTest == 1, // Abilita il pulsante solo se numberOfTest è 1
                    modifier = Modifier.padding(end = 16.dp)
                ) {
                    Text("Training")
                }
                Button(
                    onClick = { onChosenPhase(3)
                    },
                    enabled = numberOfTest == 2, // Abilita il pulsante solo se numberOfTest è 2
                ) {
                    Text("Assessment")
                }
            }
        }
    }

    @Composable
    fun TowerOfLondonContent() {
        //funzione che rappresenta l'esecuzione del test per quanto riguarda la parte grafica
        var showingInsertId by remember { mutableStateOf(true) }
        var showingModality by remember { mutableStateOf(true) }
        var showingChoosephase by remember { mutableStateOf(true) }

        //controlliamo se l'ID è stato inserito
        if(showingInsertId){
            InsertID(onChosenID = { inputValue ->
                id = inputValue // Aggiorna la variabile con l'ID inserito
                showingInsertId = false
            })
        }
        else{
            //controlliamo se è stata scelta la modalità
            if (showingChoosephase) {
                ChoosePhase { chosenPhase ->
                    phase = chosenPhase
                    showingChoosephase = false
                }
            }
            else{
                //controlliamo se è stata scelta la fase
                if(showingModality && phase == 2){
                    ChooseModality{ chosenModality ->
                        modality = chosenModality
                        showingModality = false
                    }
                }else{
                    //inizialmente verifichiamo se è avvenuta una prima inizializzazione
                    //altrimenti la richiamiamo
                    if (!start) {initializeSticks()}
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(200.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Spacer(modifier = Modifier.height(10.dp))
                            //se ci troviamo nella fase di training Pepper può fornire aiuto tramite un button
                            if(phase == 2){
                                Button(onClick = {
                                    numberOfHint++
                                    CoroutineScope(Dispatchers.Main).launch {
                                        //se ci troviamo nella modalità fostering Pepper non può fornire aiuti consecutivi
                                        if(modality == 2){
                                            //numberOfMovesAfterHint se è stato richiesto un aiuto nella mossa precedente
                                            //viene impostato uguale al numero di mosse quindi tale controllo blocca la
                                            //richiesta di aiuti consecutivi
                                            if(numberOfMovesAfterHint + 1 == numberOfMove){
                                                //in tal caso inviamo al server il messaggio di richiesta di aiuti consecutivi
                                                val consecutiveHint = sendMessageConsecutiveHint()
                                                if (consecutiveHint) {
                                                    println("Success to send data")
                                                } else {
                                                    // Gestire l'invio fallito qui
                                                    println("Failed to send data")
                                                }
                                            }else {
                                                //come detto prima ci salviamo al momento dell'aiuto il numero di mosse
                                                numberOfMovesAfterHint = numberOfMove
                                                //in questo caso forniamo l'aiuto poichè non ci sono state richieste
                                                //consecutive
                                                val success = sendConfiguration()
                                                if (success) {
                                                    // Invio riuscito, puoi gestire qui eventuali azioni da eseguire
                                                    println("Send Configuration Data sent successfully")
                                                } else {
                                                    // Gestire l'invio fallito qui
                                                    println("Send Configuration Failed to send data")
                                                }
                                            }
                                        }
                                        else{
                                            //se ci troviamo in modalità full support l'utente può chiedere l'aiuto
                                            //ogni volta che vuole
                                            val success = sendConfiguration()
                                            if (success) {
                                                // Invio riuscito, puoi gestire qui eventuali azioni da eseguire
                                                println("Send Configuration Data sent successfully")
                                            } else {
                                                // Gestire l'invio fallito qui
                                                println("Send Configuration Failed to send data")
                                            }
                                        }
                                    }
                                }) {
                                    Text("Aiuto ")
                                    Image(
                                        painter = painterResource(id = R.drawable.help), // Immagine dell'icona
                                        contentDescription = "Icona Suggerimento",
                                        modifier = Modifier
                                            .size(30.dp)
                                    )
                                }
                            }

                        }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f)
                        ) {
                            //In questo spazio sono rappresentati i bastoncini e le palline spostabili
                            Spacer(modifier = Modifier.height(66.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(333.dp)
                                    .padding(0.dp, 0.dp, 0.dp, 0.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .weight(1f)
                                        .padding(0.dp, 0.dp, 0.dp, 0.dp)
                                ) {
                                    StickColumn(
                                        stick = stick1.value,
                                        height = 333.dp,
                                        width = 33.dp,
                                        clickable = true
                                    )
                                }

                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(
                                            0.dp,
                                            110.dp,
                                            0.dp,
                                            0.dp
                                        )
                                        .weight(1f)
                                ) {
                                    StickColumn(
                                        stick = stick2.value,
                                        height = 223.dp,
                                        width = 33.dp,
                                        clickable = true
                                    )
                                }

                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(0.dp, 222.dp, 0.dp, 0.dp)
                                        .weight(1f)
                                ) {
                                    StickColumn(
                                        stick = stick3.value,
                                        height = 111.dp,
                                        width = 33.dp,
                                        clickable = true
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(33.dp)
                                    .background(Color(0xFFC2B280))
                            ) {}
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(200.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            //in questa sezione c'è il counter che si occupa inoltre di inviare dati al server
                            //con i suoi alert e inoltre di riaggiorna il composable per quanto riguarda
                            //il ritorno alla scelta della modalità o della fase
                            CounterComponent(
                                counter = counter,
                                onShowChoosePhase = { newShowingChoosePhase ->
                                    showingChoosephase = newShowingChoosePhase
                                },
                                onShowInsertID = { newShowingInsertId ->
                                    showingInsertId = newShowingInsertId
                                },
                                onShowChooseModality = { newShowingChoosingModality ->
                                    showingModality = newShowingChoosingModality
                                }
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            //qui invece troviamo la configurazione da raggiungere miniaturizzata rispetto
                            //alla principale e messa in un riquadro giallo
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(170.dp)
                                    .border(7.dp, Color.Yellow) ,
                            ){
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                ) {
                                    Spacer(modifier = Modifier.height(20.dp))
                                    Spacer(modifier = Modifier.width(33.dp))
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .weight(1f)
                                            .padding(0.dp, 13.dp, 0.dp, 0.dp)
                                    ) {
                                        StickColumn(
                                            stick = stickFinal1.value,
                                            height = 140.dp,
                                            width = 20.dp,
                                            clickable = false
                                        )
                                    }

                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(
                                                0.dp, 60.dp, 0.dp, 0.dp
                                            ) // Distanza dalla colonna precedente
                                            .weight(1f)
                                    ) {
                                        StickColumn(
                                            stick = stickFinal2.value,
                                            height = 93.dp,
                                            width = 20.dp,
                                            clickable = false
                                        )
                                    }

                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(0.dp, 107.dp, 0.dp, 0.dp)
                                            .weight(1f)
                                    ) {
                                        StickColumn(
                                            stick = stickFinal3.value,
                                            height = 47.dp,
                                            width = 20.dp,
                                            clickable = false
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun StickColumn(stick: Stick, height: Dp, width: Dp, clickable: Boolean = true) {
        //funzione che disegna un bastoncino a cui va passato altezza, larghezza e se le palline sopra
        //di esso possono essere spostate o meno
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height),
            contentAlignment = Alignment.Center
        ) {
            // Colonna principale
            Column(
                modifier = Modifier
                    .width(width)
                    .height(height)
                    .background(Color(0xFFC2B280))
            ) {}

            // Sovrapponi la colonna dei dischi sopra la colonna principale
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val emptyPositions = stick.size - stick.balls.size
                val initialSpacerHeight = if (emptyPositions > 0) height / stick.size.toFloat() * emptyPositions else 0.dp

                Spacer(modifier = Modifier
                    .height(initialSpacerHeight)
                    .fillMaxWidth())
                //richiamo della funzione che disegna le palline
                Drawballs(stick = stick, clickable = clickable)
            }
        }
    }

    @Composable
    fun Drawballs(stick: Stick, clickable: Boolean) {
        //funzione che disegna le palline in un bastoncino
        //scorre la lista di palline che è un attributo di una qualsiasi
        //istanza della classe Stick e disegna la pallina
        for (ball in stick.balls) {
            //il tag key è fondamentale per il funzionamento
            //in quanto consente di dare un identificativo grafico ad ogni oggetto
            key(ball.name) {
                println("Stick: ${stick.name} ball: ${ball.name}")
                if (clickable) {
                    //disegna le palline che si possono spostare
                    BallItem(
                        ball = ball
                    )
                    Box {
                        Spacer(
                            modifier = Modifier
                                .height(7.dp)
                                .fillMaxWidth()
                        )
                    }
                } else {
                    //disenga le palline che non si possono spostare
                    //ovvero quelle della configurazione finale
                    Box(
                        modifier = Modifier
                            .size(47.dp)
                            .padding(3.dp)
                    ) {
                        NonClickableballItem(ball = ball)
                    }
                }
            }
        }
    }

    @Composable
    fun NonClickableballItem(ball: Ball) {
        //funzione che rappresenta l'oggetto della pallina nella configurazione finale graficamente
        Box(
            modifier = Modifier
                .background(ball.color, shape = CircleShape)
                .size(40.dp)
        )
    }

    @Composable
    fun BallItem(ball: Ball) {
        //funzione che rappresenta l'oggetto della pallina spostabile graficamente
        //tali variabili di offset servono per calcolare lo spostamento
        //invece il nome della pallina funziona
        var offsetX by remember { mutableFloatStateOf(0f) }
        var offsetY by remember { mutableFloatStateOf(0f) }

        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .background(ball.color, shape = CircleShape)
                .size(100.dp)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            //iniziamo col trascinamento
                            change.consume()
                            println("ball selected: ${ball.name}")
                            //se la pallina selezionata è quella che si trova in cima alla sua lista
                            //consideriamo valido lo spostamento e modifichiamo i parametri di spostamento
                            if (ball == findStickContainingball(ball).balls.first()) {
                                offsetX += dragAmount.x
                                offsetY += dragAmount.y
                            } else {
                                //non può muoversi altrimenti
                                println(
                                    "${ball.name} isn't at the top, try moving ${
                                        findStickContainingball(ball).balls.first().name
                                    } first"
                                )
                            }
                            println("Dragging @ X: $offsetX, Y: $offsetY on ball: ${ball.name}")
                        },
                        onDragEnd = {
                            //alla fine del trascinamento
                            if (ball == findStickContainingball(ball).balls.first()) {
                                println("End of dragging @ X: $offsetX, Y: $offsetY on ball: ${ball.name}")
                                //controlliamo da quale bastoncino arriva la pallina
                                val sourceStick = findStickContainingball(ball)
                                //controlliamo in quale bastoncino deve andare la pallina
                                val targetStick = findStickUnderPosition(offsetX, sourceStick)
                                if (offsetX != 0f && offsetY != 0f) {
                                    //se il movimento è valido chiamiamo le funzioni apposta per l'aggiornamento
                                    //logico e grafico
                                    moveball(ball, targetStick)
                                    //resettiamo i parametri di offset dopo uno spostamento in modo tale
                                    //da considerare lo spostamento dal punto nuovo in cui si trova
                                    offsetX = 0f
                                    offsetY = 0f
                                } else {
                                    println("Not a valid movement")
                                }
                            } else {
                                println(
                                    "${ball.name} isn't at the top, try moving ${
                                        findStickContainingball(ball).balls.first().name
                                    } first"
                                )
                            }
                        }
                    )
                }
        ){}
    }

    @Composable
    fun CounterComponent(counter: Counter, onShowChoosePhase: (Boolean) -> Unit, onShowInsertID: (Boolean) -> Unit, onShowChooseModality: (Boolean) -> Unit) {
        //funzione che si occupa di gestire il counter, inviare dati al server e inoltre di inviare al content il ritorno
        //alla pagina di selezione modalità o fase o inserimento ID
        var showDialog by remember { mutableStateOf(false) }
        var showEndDialog by remember { mutableStateOf(false) }
        var hintGiven by remember {mutableStateOf(false)}

        LaunchedEffect(Unit) {
            while (true) {
                //ogni secondo verifichiamo se effettivamente l'utente ha completato il test
                delay(1000)
                if (completed) {
                    showDialog = true
                } else if ( counter.value > 0) {
                    //nel caso in cui non abbiamo ancora completato il test viene decrementato il counter
                    counter.decrement()
                }

                if(phase == 2 && modality == 1){
                    //se ci troviamo nella modalità training dobbiamo ricordare ogni 15 secondi
                    //che l'utente può richiedere il suggerimento
                    if (numberOfPlay>0 && numberOfPlay%2 == 0 && !hintGiven) {
                        sendMessageOfAviableHelp()
                        hintGiven = true
                        println("aiuto possibile")
                    }
                }
            }
        }

        //show dell'alert nella fase di training e assessment
        if (showDialog) {
            Dialog(
                properties = DialogProperties(dismissOnClickOutside = false, dismissOnBackPress = false),
                onDismissRequest = {
                    showDialog = false
                },
                content = {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .width(280.dp) // Adjust width as needed
                            .height(180.dp) // Adjust height as needed
                            .background(color = Color.White, shape = RoundedCornerShape(20.dp)) // Set white background
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Image at top center
                            Spacer(modifier = Modifier.height(7.dp))
                            Image(
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .size(50.dp, 50.dp), // Adjust image size
                                painter = painterResource(R.drawable.app_icon), // Replace with your image resource ID
                                contentDescription = "Your image description"
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            // Text centered below image
                            Text(
                                text = "Test n° ${numberOfPlay + 1} completato in ${60 - counter.value} secondi",
                                fontSize = MaterialTheme.typography.titleMedium.fontSize, // Use h6 for title
                                textAlign = TextAlign.Center,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )

                            // Spacer for vertical positioning
                            Spacer(modifier = Modifier.weight(1f))

                            // "Salvataggio in corso..." at bottom right
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .padding(0.dp, 0.dp, 5.dp,0.dp),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Text(
                                    text = "Salvataggio in corso...",
                                    fontSize = MaterialTheme.typography.bodySmall.fontSize, // Use caption for smaller text
                                    color = Color.Gray // Use gray for less emphasis
                                )
                                Spacer(modifier = Modifier.width(8.dp)) // Add spacing between text and progress indicator
                                CircularProgressIndicator(modifier = Modifier.size(10.dp)) // Show loading indicator
                            }
                        }

                        LaunchedEffect(Unit) {
                            delay(3000)
                            // Inviamo i dati al server
                            val success = sendDataToServer(
                                completed.toString(),
                                phase.toString(),
                                counter.value.toString(),
                                numberOfMove.toString(),
                                numberOfHint.toString(),
                                modality.toString(),
                                id.toString(),
                                numberOfMovesMin.toString()
                            )
                            if (success) {
                                println("Send Success Data sent successfully")
                            } else {
                                println("Send Success Data Failed to send data")
                            }

                            // Handle unsuccessful data sending (similar logic as above)
                            // ...
                            counter.reset()
                            numberOfPlay++
                            resetVariables()
                            if (numberOfPlay == 4 && phase != 2) {
                                numberOfTest++
                                showEndDialog = true
                            } else if (numberOfPlay == 12) {
                                numberOfTest++
                                showEndDialog = true
                            }
                            showDialog = false
                            hintGiven = false
                        }
                    }
                }
            )
        }

        if (showEndDialog) {
            val nameOfPhase : String = namesOfPhase[phase-1]
            AlertDialog(
                properties = DialogProperties(dismissOnClickOutside = false, dismissOnBackPress = false),
                onDismissRequest = {
                    showEndDialog = false
                },
                title = { Text(text = "Fase $nameOfPhase completata") },
                confirmButton = {
                    Button(
                        onClick = {
                            //Alert che viene mostrato quando finiscono tutte le esecuzioni di una fase
                            counter.reset() // Resettiamo il contatore
                            //se sono state effettuate test in tutte e tre le fasi torniamo alla
                            //scelta dell'ID
                            showEndDialog = false
                            if(numberOfTest == 3)  {
                                onShowInsertID(true)
                                onShowChoosePhase(true)
                                onShowChooseModality(true)
                                phase = 0
                                modality = 0
                                numberOfPlay = 0
                                numberOfTest = 0
                                resetVariables()
                            }
                            else{
                                //altrimenti ritorniamo nella selezione della fasi se sono state completate
                                //tutte le esecuzioni di una fase
                                numberOfPlay = 0
                                phase = 0
                                modality = 0
                                resetVariables()
                                onShowChoosePhase(true)
                            }
                        }
                    ) {
                        Text("OK")
                    }
                }
            )
        }
    }

}