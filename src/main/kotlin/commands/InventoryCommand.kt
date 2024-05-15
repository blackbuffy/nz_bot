package commands

import database.DBHandler
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import java.awt.Color

class InventoryCommand : Command {
    override fun execute(event: SlashCommandInteractionEvent) {
        when (event.subcommandName) {
            "выдать_броню" -> {
                giveArmor(event)
            }
            "забрать_броню" -> {
                takeArmor(event)
            }
            "посмотреть" -> {
                getInventory(event)
            }
            "выдать_оружие" -> {
                giveWeapon(event)
            }
            "забрать_оружие" -> {
                takeWeapon(event)
            }
            "выдать_провизию" -> {
                giveConsumable(event)
            }
            "забрать_провизию" -> {
                takeConsumable(event)
            }
        }
    }

    companion object {
        fun getInventory(event: SlashCommandInteractionEvent) {
            val dbHandler = DBHandler()
            var sb = StringBuilder()
            val user: User =
                if (event.getOption("пользователь") == null) event.user else event.getOption("пользователь")!!
                    .asUser
            val userid = user.idLong
            val armorList: Array<String> = dbHandler.getUserArmor(userid)
            for (armor in armorList) {
                sb.append(armor + "\n")
            }
            val armorFieldStr: String = if (sb.toString() == "\n") "Пусто" else sb.toString()
            sb = StringBuilder()
            val weaponList: Array<String> = dbHandler.getUserWeapon(userid)
            for (weapon in weaponList!!) {
                sb.append(weapon + "\n")
            }
            val weaponFieldStr: String = if (sb.toString() == "\n") "Пусто" else sb.toString()
            sb = StringBuilder()
            val consumablesList: Array<String> = dbHandler.getUserConsumables(userid)
            for (consumable in consumablesList!!) {
                sb.append(consumable + "\n")
            }
            val consumablesFieldStr: String = if (sb.toString() == "\n") "Пусто" else sb.toString()
            val eb = EmbedBuilder()
            val emsgArmorField: MessageEmbed.Field = MessageEmbed.Field("Броня:", armorFieldStr, true)
            val emsgWeaponField: MessageEmbed.Field = MessageEmbed.Field("Оружие:", weaponFieldStr, true)
            val emsgConsumablesField: MessageEmbed.Field = MessageEmbed.Field("Провизия:", consumablesFieldStr, true)
            val emsg: MessageEmbed = eb
                .setColor(Color(18, 125, 181))
                .setTitle("Инвентарь " + user.globalName)
                .addField(emsgArmorField)
                .addField(emsgWeaponField)
                .addField(emsgConsumablesField)
                .build()
            val msg: MessageCreateData = MessageCreateBuilder()
                .addEmbeds(emsg)
                .build()
            event.reply(msg).queue()
        }

        fun giveArmor(event: SlashCommandInteractionEvent) {
            val dbHandler = DBHandler()
            val roleIds: List<String> = dbHandler.getRoleIds("gm", 1).split(", ")
            var result = false
            mainLoop@ for (role in event.member!!.roles) {
                for (roleId in roleIds) {
                    if (role.id == roleId) {
                        val user: User = event.getOption("пользователь")!!.asUser
                        val armorName: String = event.getOption("название_предмета")!!.asString
                        val userid = user.idLong
                        val res: Boolean = dbHandler.giveArmorItem(userid, armorName)
                        event.reply(if (res) "Предмет " + armorName + " выдан пользователю " + user.globalName + " успешно!" else "Предмета $armorName не существует в базе данных.")
                            .queue()
                        result = true
                        break@mainLoop
                    }
                }
            }
            if (!result) {
                event.reply("У вас недостаточно прав").queue()
            }
        }

        fun takeArmor(event: SlashCommandInteractionEvent) {
            val dbHandler = DBHandler()
            val roleIds: List<String> = dbHandler.getRoleIds("gm", 1).split(", ")
            var result = false
            mainLoop@ for (role in event.member!!.roles) {
                for (roleId in roleIds) {
                    if (role.id == roleId) {
                        val user: User = event.getOption("пользователь")!!.asUser
                        val armorName: String = event.getOption("название_предмета")!!.asString
                        val userid = user.idLong
                        val res: Boolean = dbHandler.takeArmorItem(userid, armorName)
                        event.reply(if (res) "Предмет " + armorName + " у пользователя " + user.asMention + " удален успешно!" else "У пользователя " + user.asMention + " нет данного предмета.")
                            .queue()
                        result = true
                        break@mainLoop
                    }
                }
            }
            if (!result) {
                event.reply("У вас недостаточно прав").queue()
            }
        }

        fun giveWeapon(event: SlashCommandInteractionEvent) {
            val dbHandler = DBHandler()
            val roleIds: List<String> = dbHandler.getRoleIds("gm", 1).split(", ")
            var result = false
            mainLoop@ for (role in event.member!!.roles) {
                for (roleId in roleIds) {
                    if (role.id == roleId) {
                        val user: User = event.getOption("пользователь")!!.asUser
                        val name: String = event.getOption("название_предмета")!!.asString
                        val userid = user.idLong
                        val res: Boolean = dbHandler.giveWeapon(userid, name)
                        event.reply(if (res) "Оружие " + name + " выдан пользователю " + user.globalName + " успешно!" else "Оружия $name не существует в базе данных.")
                            .queue()
                        result = true
                        break@mainLoop
                    }
                }
            }
            if (!result) {
                event.reply("У вас недостаточно прав").queue()
            }
        }

        fun takeWeapon(event: SlashCommandInteractionEvent) {
            val dbHandler = DBHandler()
            val roleIds: List<String> = dbHandler.getRoleIds("gm", 1).split(", ")
            var result = false
            mainLoop@ for (role in event.member!!.roles) {
                for (roleId in roleIds) {
                    if (role.id == roleId) {
                        val user: User = event.getOption("пользователь")!!.asUser
                        val armorName: String = event.getOption("название")!!.asString
                        val userid = user.idLong
                        val res: Boolean = dbHandler.takeWeapon(userid, armorName)
                        event.reply(if (res) "Оружие " + armorName + " у пользователя " + user.asMention + " удален успешно!" else "У пользователя " + user.asMention + " нет данного оружия.")
                            .queue()
                        result = true
                        break@mainLoop
                    }
                }
            }
            if (!result) {
                event.reply("У вас недостаточно прав").queue()
            }
        }

        fun giveConsumable(event: SlashCommandInteractionEvent) {
            val dbHandler = DBHandler()
            val roleIds: List<String> = dbHandler.getRoleIds("gm", 1).split(", ")
            var result = false
            mainLoop@ for (role in event.member!!.roles) {
                for (roleId in roleIds) {
                    if (role.id == roleId) {
                        val user: User = event.getOption("пользователь")!!.asUser
                        val name: String = event.getOption("название")!!.asString
                        val userid = user.idLong
                        val res: Boolean = dbHandler.giveConsumable(userid, name)
                        event.reply(if (res) "Предмет " + name + " выдан пользователю " + user.globalName + " успешно!" else "Предмета $name не существует в базе данных.")
                            .queue()
                        result = true
                        break@mainLoop
                    }
                }
            }
            if (!result) {
                event.reply("У вас недостаточно прав").queue()
            }
        }

        fun takeConsumable(event: SlashCommandInteractionEvent) {
            val dbHandler = DBHandler()
            val roleIds: List<String> = dbHandler.getRoleIds("gm", 1).split(", ")
            var result = false
            mainLoop@ for (role in event.member!!.roles) {
                for (roleId in roleIds) {
                    if (role.id == roleId) {
                        val user: User = event.getOption("пользователь")!!.asUser
                        val armorName: String = event.getOption("название")!!.asString
                        val userid = user.idLong
                        val res: Boolean = dbHandler.takeConsumable(userid, armorName)
                        event.reply(if (res) "Предмет " + armorName + " у пользователя " + user.asMention + " удален успешно!" else "У пользователя " + user.asMention + " нет данного предмета.")
                            .queue()
                        result = true
                        break@mainLoop
                    }
                }
            }
            if (!result) {
                event.reply("У вас недостаточно прав").queue()
            }
        }
    }
}
