package sample

import javafx.fxml.FXML
import javafx.scene.control.*
import kotlinx.coroutines.experimental.*
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.experimental.javafx.JavaFx as UI
import kotlinx.coroutines.experimental.run as runIn
import javax.sound.sampled.*

enum class CurrentState {REST, WORK }

object Sound {
    var SAMPLE_RATE = 8000f

    fun tone(hz: Int, msecs: Int, vol: Double = 1.0) {
        val buf = ByteArray(1)
        val af = AudioFormat(SAMPLE_RATE, 8, 1, true, false)
        val sdl = AudioSystem.getSourceDataLine(af)
        sdl.open(af)
        sdl.start()
        for (i in 0..msecs * 8 - 1) {
            val angle = (i / (SAMPLE_RATE / hz)).toDouble() * 2.0 * Math.PI
            buf[0] = (Math.sin(angle) * 127.0 * vol).toByte()
            sdl.write(buf, 0, 1)
        }
        sdl.drain()
        sdl.stop()
        sdl.close()
    }
}

class Controller {
    @FXML var progress: ProgressBar? = null
    @FXML var label: Label? = null
    @FXML var stopButton:Button? = null
    @FXML var workTimeField:TextField? = null
    @FXML var restTimeField:TextField? = null

    var restTime = 10L
    var workTime = 40L
    var currentJob: Job? = null
    var destTime = LocalDateTime.now()
    var currentState: CurrentState = CurrentState.REST

    suspend fun beep(n:Int) = async(CommonPool){
        repeat(n){
            Sound.tone(5000, 150)
            delay(50)
        }
    }

    fun launchTimer() = launch(CommonPool) {
        while(isActive) {
            if (LocalDateTime.now() < destTime) {
                launch(UI) {
                    val dif = (ChronoUnit.NANOS.between(LocalDateTime.now(), destTime))
                    progress!!.progress = 1 - (1.0 / (if (currentState == CurrentState.WORK) workTime else restTime).toDouble()) * dif
                }
                delay(50)
            } else {
                when (currentState) {
                    CurrentState.REST -> {
                        beep(2)
                        startState(false)
                    }
                    CurrentState.WORK -> {
                        beep(1)

                        startState(true)
                    }
                }
            }
        }
    }

    fun startState(isRest: Boolean) {
        if (currentJob != null) {
            currentJob!!.cancel()
        }
        launch(UI) {
            try {
                val workT = workTimeField!!.text.toDouble()
                val restT = restTimeField!!.text.toDouble()
                restTime = (restT * 60 * 1000000000).toLong()
                workTime = (workT * 60 * 1000000000).toLong()
            } catch (ex: TypeCastException) {
                workTimeField!!.text = ""
                restTimeField!!.text = ""
            }

            progress!!.progress = 0.0
            label!!.text = if (isRest) "Rest" else "Work"
            currentState = if (isRest) CurrentState.REST else CurrentState.WORK

            destTime = LocalDateTime.now().plusNanos(if (isRest) restTime else workTime)
            currentJob = launchTimer()
            stopButton!!.isVisible = true
        }
    }

    @FXML
    fun startWork() = runBlocking {
        startState(false)
    }

    @FXML
    fun startRest() = runBlocking {
        startState(true)
    }

    @FXML
    fun stop() = runBlocking {
        if (currentJob != null) {
            currentJob!!.cancel()
        }

        label!!.text = "Stop"
        progress!!.progress = 0.0
        stopButton!!.isVisible = false
    }
}
