package commands

import database.DBHandler
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import java.time.LocalDateTime

class BonusCommand: Command {
    override fun execute(event: SlashCommandInteractionEvent) {
        val user = event.user
        val userId = user.idLong
        val dbHandler = DBHandler()

        if (dbHandler.checkDateForBonus(userId) == "true") {
            val dayStreak = dbHandler.getBonusStreak(userId)
            dbHandler.incrementBonusStreak(userId)

            TODO("Выдачу награды и подпись какой конкретно бонус получен")

            dbHandler.setNewDateForBonus(userId, LocalDateTime.now())

            event.reply("Вы успешно забрали бонус").queue()
        } else {
            val dateTime = LocalDateTime.parse(dbHandler.checkDateForBonus(userId))
            event.reply("Вы сможете забрать бонус не раньше, чем в " +
                    "${dateTime.dayOfMonth}.${dateTime.month}.${dateTime.year} ${dateTime.hour}:${dateTime.minute}.${dateTime.second}")
                .queue()
        }
    }
}