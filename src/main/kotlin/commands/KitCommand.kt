package commands

import RP_MEMBER_ROLE_ID
import RUBLE_SYMBOL
import database.DBHandler
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class KitCommand: Command {
    override fun execute(event: SlashCommandInteractionEvent) {
        getKit(event)
    }

    private fun getKit(event: SlashCommandInteractionEvent) {
        val dbHandler = DBHandler()
        val user = event.user

        when (dbHandler.getIsKitUsedStatus(user.idLong)) {
            true -> {
                event.reply("Вы уже забирали набор!").queue()
            }
            else -> {
                var res = false
                val member = event.member
                member!!.roles.forEach {
                    when (it.idLong) {
                        RP_MEMBER_ROLE_ID -> {
                            dbHandler.giveArmorItem(user.idLong, "Кожаная куртка")
                            dbHandler.giveWeapon(user.idLong, "Обрез ТОЗ-66")
                            dbHandler.giveWeapon(user.idLong, "ПМ")
                            dbHandler.giveConsumable(user.idLong, "Хлеб")
                            dbHandler.giveConsumable(user.idLong, "Хлеб")
                            dbHandler.giveConsumable(user.idLong, "Хлеб")
                            dbHandler.giveBonus(user.idLong, 1.5f, 500)
                            dbHandler.changeIsKitUsedStatusTo(user.idLong, true)

                            event.reply("Вы получили: Кожаная куртка, Обрез ТОЗ-66, ПМ, 3 хлеба, 500$RUBLE_SYMBOL и *x1.5* опыта на следующее задание").queue()
                            res = true
                        }
                    }
                }

                if (!res) {
                    event.reply("Вы еще не участник РП :(").queue()
                }
            }
        }
    }
}