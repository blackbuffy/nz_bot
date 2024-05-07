package time

import database.DBHandler
import kotlinx.coroutines.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class TimeController {
    private var isPaused = false
    private val dbHandler = DBHandler()
    private var dateTime = dbHandler.getDateTime()
    private lateinit var job: Job

    @OptIn(DelicateCoroutinesApi::class)
    fun startTime() {
        job = GlobalScope.launch {
            pausableTask()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun resumeTime() {
        isPaused = false
        job = GlobalScope.launch {
            pausableTask()
        }
    }

    fun pauseTime() {
        isPaused = true
    }

    fun getDate(): LocalDate = dateTime.toLocalDate()

    fun getTime(): LocalTime = dateTime.toLocalTime()

    fun setDate(date: LocalDate) {
        val time = dateTime.toLocalTime()
        dateTime = LocalDateTime.of(date, time)
    }

    fun setTime(time: LocalTime) {
        val date = dateTime.toLocalDate()
        dateTime = LocalDateTime.of(date, time)
    }

    private suspend fun pausableTask() {
        while (true) {
            if (!isPaused) {
                dateTime = dateTime.plusSeconds(1L)
                delay(125L)
            }
        }
    }

    fun saveTime() {
        isPaused = true
        val dateTime = LocalDateTime.of(getDate(), getTime())
        dbHandler.setDateTime(dateTime)
    }
}