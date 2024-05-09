package commands

import RUBLE_SYMBOL
import database.DBHandler
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import java.awt.Color

class BalanceCommand: Command {
    override fun execute(event: SlashCommandInteractionEvent) {
        when (event.subcommandName) {
            "посмотреть" -> {
                getBalance(event)
            }
            "изменить" -> {
                changeBalance(event)
            }
        }
    }

    companion object {
        fun getBalance(event: SlashCommandInteractionEvent) {
            val dbHandler = DBHandler()
            val embedBuilder = EmbedBuilder()
            val user = event.getOption("пользователь")?.asUser?: event.user

            val balance = dbHandler.getBalance(user.idLong)
            when (balance) {
                null -> {
                    event.reply("У пользователя нет профиля").queue()
                }
                else -> {
                    val embedMSG = embedBuilder
                        .setColor(Color(18, 125, 181))
                        .setTitle("Баланс игрока ${user.globalName}")
                        .setDescription("${balance}${RUBLE_SYMBOL}")
                        .build()

                    val msg = MessageCreateBuilder()
                        .setEmbeds(embedMSG)
                        .build()

                    event.reply(msg).queue()
                }
            }
        }

        fun changeBalance(event: SlashCommandInteractionEvent) {
            val dbHandler = DBHandler()
            val user = event.getOption("пользователь")?.asUser?: event.user
            val value = event.getOption("значение")!!.asInt

            when (dbHandler.getBalance(user.idLong)) {
                null -> {
                    event.reply("У пользователя нет профиля").queue()
                }
                else -> {
                    dbHandler.changeBalance(user.idLong, value)
                    event.reply("Баланс успешно изменен на $value").queue()
                }
            }
        }
    }
}