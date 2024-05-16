package commands

import RUBLE_SYMBOL
import database.DBHandler
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import java.time.LocalDateTime

class BonusCommand: Command {
    override fun execute(event: SlashCommandInteractionEvent) {
        when (event.subcommandName) {
            "забрать" -> {
                collectBonus(event)
            }
        }
    }

    private fun collectBonus(event: SlashCommandInteractionEvent) {
        val user = event.user
        val userId = user.idLong
        val dbHandler = DBHandler()

        if (dbHandler.checkDateForBonus(userId) == "true") {
            val dayStreak = dbHandler.getBonusStreak(userId)
            dbHandler.incrementBonusStreak(userId)

            val bonusDataMap = dbHandler.getBonus(dayStreak)
            val modifier = bonusDataMap["modifier"] as Float
            val money = bonusDataMap["money"] as Int

            dbHandler.giveBonus(userId, modifier, money)

            dbHandler.setNewDateForBonus(userId, LocalDateTime.now())

            when (modifier) {
                1.0f -> {
                    event.reply("Вы успешно забрали бонус за *${dayStreak+1} день*: *x1* опыта на следующее задание и *$money$RUBLE_SYMBOL*").queue()
                }
                else -> {
                    event.reply("Вы успешно забрали бонус за *${dayStreak+1} день*: *x$modifier* опыта на следующее задание и *$money$RUBLE_SYMBOL*").queue()
                }
            }
        } else {
            val dateTime = LocalDateTime.parse(dbHandler.checkDateForBonus(userId))
            event.reply("Вы сможете забрать бонус не раньше, чем " +
                    "${dateTime.dayOfMonth}.${dateTime.month}.${dateTime.year} ${dateTime.hour}:${dateTime.minute}.${dateTime.second}")
                .queue()
        }
    }
}