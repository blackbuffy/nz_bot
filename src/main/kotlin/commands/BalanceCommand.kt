package commands

import database.DBHandler
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

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
        }

        fun changeBalance(event: SlashCommandInteractionEvent) {
            TODO("Изменение баланса персонажа")
        }
    }
}