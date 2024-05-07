package commands

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import timeController
import java.time.LocalDate
import java.time.LocalTime

class TimeCommand : Command {
    override fun execute(event: SlashCommandInteractionEvent) {
        if (event.subcommandName == "установить") {
            timeSet(event)
        } else if (event.subcommandName == "узнать") {
            event.reply(
                """
                    Дата: ${timeController.getDate()}
                    Время: ${timeController.getTime()}
                """.trimIndent()
            ).queue()
        } else if (event.subcommandName == "пауза") {
            timeController.pauseTime()
            event.deferReply(true).queue()
            event.hook.sendMessage("Время успешно поставлено на паузу!").queue()
        } else if (event.subcommandName == "возобновить") {
            timeController.resumeTime()
            event.deferReply(true).queue()
            event.hook.sendMessage("Время успешно возобновлено!").queue()
        }
    }

    companion object {
        fun timeSet(event: SlashCommandInteractionEvent) {
            event.deferReply().queue()
            val day: Int = event.getOption("день")!!.asInt
            val month: Int = event.getOption("месяц")!!.asInt
            val year: Int = event.getOption("год")!!.asInt
            val hour: Int = event.getOption("час")!!.asInt
            val minutes: Int = event.getOption("минут")!!.asInt
            val seconds: Int = event.getOption("секунд")!!.asInt
            timeController.pauseTime()
            timeController.setDate(LocalDate.of(year, month, day))
            timeController.setTime(LocalTime.of(hour, minutes, seconds))
            event.hook.sendMessage("Время успешно изменено на: " + timeController.getDate() + " " + timeController.getTime()).queue()
            timeController.resumeTime()
        }
    }
}
