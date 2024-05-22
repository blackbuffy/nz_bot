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
            "создать" -> {
                createBonus(event)
            }
        }
    }

    private fun createBonus(event: SlashCommandInteractionEvent) {
        val dbHandler = DBHandler()
        val roleIds: List<String> = dbHandler.getRoleIds("admin", 1).split(", ")
        var res = false

        mainLoop@ for (role in event.member!!.roles) {
            for (roleId in roleIds) {
                if (role.id == roleId) {
                    val modifier = event.getOption("модификатор")?.asDouble?.toFloat() ?: 1.0f
                    val day = event.getOption("день")!!.asInt
                    val money = event.getOption("деньги")?.asInt ?: 0

                    dbHandler.createBonus(modifier, money, day)
                    event.reply("Добавлено").setEphemeral(true).queue()

                    res = true
                    break@mainLoop
                }
            }
        }

        if (!res) {
            event.reply("У вас недостаточно прав").queue()
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
                    "${dateTime.dayOfMonth.toString().padStart(2, '0')}.${dateTime.month.value.toString().padStart(2, '0')}.${dateTime.year} " +
                    "${dateTime.hour.toString().padStart(2, '0')}:${dateTime.minute.toString().padStart(2, '0')}:${dateTime.second.toString().padStart(2, '0')}")
                .queue()
        }
    }
}