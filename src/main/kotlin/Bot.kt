import buttons.ButtonListener
import buttons.ModalListener
import commands.CommandManager
import listeners.EventListener
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.cache.CacheFlag
import time.TimeController

val timeController = TimeController()

fun main() {
    // Установка значения токена
    val token = TOKEN
    // Создание билдера бота
    val jda = JDABuilder.createDefault(token).build()
    // Конфигурация бота
    jda.presence.activity = Activity.watching("за игроками")
    jda.presence.setStatus(OnlineStatus.ONLINE)
    jda.gatewayIntents.addAll(setOf(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_PRESENCES))
    jda.cacheFlags.addAll(setOf(CacheFlag.ACTIVITY, CacheFlag.MEMBER_OVERRIDES))
    jda.addEventListener(EventListener(), ButtonListener(), ModalListener(), CommandManager())

    // Запуск корутины для времени
    timeController.startTime()

    Runtime.getRuntime().addShutdownHook(Thread {
        timeController.saveTime()
    })
}