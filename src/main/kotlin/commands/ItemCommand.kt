package commands

import database.DBHandler
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import java.awt.Color

class ItemCommand : Command {
    override fun execute(event: SlashCommandInteractionEvent) {
        when (event.subcommandName) {
            "добавить_броню" -> {
                createArmor(event)
            }
            "удалить_броню" -> {
                deleteArmor(event)
            }
            "добавить_оружие" -> {
                createWeapon(event)
            }
            "удалить_оружие" -> {
                deleteWeapon(event)
            }
            "добавить_расходник" -> {
                createConsumable(event)
            }
            "удалить_расходник" -> {
                deleteConsumable(event)
            }
            "добавить_патроны" -> {
                createAmmo(event)
            }
            "удалить_патроны" -> {
                deleteAmmo(event)
            }
            "все" -> {
                getAllItems(event)
            }
            "узнать_о" -> {
                getItemInfo(event)
            }
        }
    }

    companion object {
        fun createAmmo(event: SlashCommandInteractionEvent) {
            val dbHandler = DBHandler()
            val roleIds: List<String> = dbHandler.getRoleIds("admin", 1).split(", ")
            var res = false
            mainLoop@ for (role in event.member!!.roles) {
                for (roleId in roleIds) {
                    if (role.id == roleId) {
                        val name: String = event.getOption("название_предмета")!!.asString
                        val price: Int = event.getOption("цена")!!.asInt
                        val amount: Int = event.getOption("количество")!!.asInt

                        dbHandler.addAmmo(name, price, amount)
                        event.reply("Предмет добавлен").queue()
                        res = true
                        break@mainLoop
                    }
                }
            }
            if (!res) {
                event.reply("У вас недостаточно прав").queue()
            }
        }

        fun deleteAmmo(event: SlashCommandInteractionEvent) {
            val dbHandler = DBHandler()
            val roleIds: List<String> = dbHandler.getRoleIds("admin", 1).split(", ")
            var res = false
            mainLoop@ for (role in event.member!!.roles) {
                for (roleId in roleIds) {
                    if (role.id == roleId) {
                        val id: Int = event.getOption("айди_предмета")!!.asInt
                        dbHandler.removeAmmo(id)
                        event.reply("Предмет удален, айди: $id").queue()
                        res = true
                        break@mainLoop
                    }
                }
            }
            if (!res) {
                event.reply("У вас недостаточно прав").queue()
            }
        }

        fun getAllItems(event: SlashCommandInteractionEvent) {
            val dbHandler = DBHandler()
            val type: String = event.getOption("тип")!!.asString
            var title = ""
            when (type) {
                "броня" -> {
                    title = "брони"
                }
                "оружие" -> {
                    title = "оружия"
                }
                "расходник" -> {
                    title = "расходников"
                }
                "патроны" -> {
                    title = "патронов"
                }
            }
            val arr: Collection<String> = dbHandler.getAllItems(type)
            if (arr.toTypedArray()[0] == "false") {
                event.reply("Неверный тип").queue()
            } else if (arr.toTypedArray()[0] == "empty") {
                val eb = EmbedBuilder()
                eb.setTitle("Список $title")
                eb.setDescription("Пусто")
                eb.setColor(Color(0, 193, 241))
                eb.setFooter("EMPTY")
                val emsg: MessageCreateData = MessageCreateBuilder()
                    .addEmbeds(eb.build())
                    .build()
                event.reply(emsg).queue()
            } else {
                val eb = EmbedBuilder()
                val descSB = StringBuilder()
                if (arr.size < 10) {
                    for (i in 0..<arr.size) {
                        val ez = arr.toTypedArray()[i]
                        descSB.append("**" + ez + " (ID: " + dbHandler.getItemId(type, ez) + ")**\n")
                    }
                } else {
                    for (i in 0..9) {
                        val ez = arr.toTypedArray()[i]
                        descSB.append("**" + ez + " (ID: " + dbHandler.getItemId(type, ez) + ")**\n")
                    }
                }
                val descStr = descSB.toString()
                val closeList = Button.danger("close-list", "X")
                val pageNext = Button.primary("page-next", ">")
                val pageLast = Button.primary("page-last", ">>")
                val pagesN = if (arr.size % 10 == 0) arr.size / 10 else arr.size / 10 + 1
                eb.setTitle("Список $title")
                eb.setDescription(descStr)
                eb.setColor(Color(0, 193, 241))
                eb.setFooter("1/$pagesN")
                val emsg: MessageCreateData = MessageCreateBuilder()
                    .addEmbeds(eb.build())
                    .setComponents(ActionRow.of(closeList, pageNext, pageLast))
                    .build()
                event.reply(emsg).queue()
            }
        }

        fun getItemInfo(event: SlashCommandInteractionEvent) {
            val dbHandler = DBHandler()
            val id: Int = event.getOption("айди")!!.asInt
            val arr: Collection<String> = dbHandler.getItemInfo(id)
            val descSB = StringBuilder()
            for (str in arr) {
                descSB.append("**$str**\n")
            }
            val descStr = descSB.toString()
            val eb = EmbedBuilder()
            eb.setTitle("Информация о предмете")
            eb.setDescription(descStr)
            eb.setColor(Color(0, 193, 241))
            val emsg: MessageCreateData = MessageCreateBuilder()
                .addEmbeds(eb.build())
                .build()
            event.reply(emsg).queue()
        }

        fun deleteArmor(event: SlashCommandInteractionEvent) {
            val dbHandler = DBHandler()
            val roleIds: List<String> = dbHandler.getRoleIds("admin", 1).split(", ")
            var res = false
            mainLoop@ for (role in event.member!!.roles) {
                for (roleId in roleIds) {
                    if (role.id == roleId) {
                        val id: Int = event.getOption("айди_предмета")!!.asInt
                        dbHandler.removeArmorItem(id)
                        event.reply("Предмет удален, айди: $id").queue()
                        res = true
                        break@mainLoop
                    }
                }
            }
            if (!res) {
                event.reply("У вас недостаточно прав").queue()
            }
        }

        fun createArmor(event: SlashCommandInteractionEvent) {
            val dbHandler = DBHandler()
            val roleIds: List<String> = dbHandler.getRoleIds("admin", 1).split(", ")
            var res = false
            mainLoop@ for (role in event.member!!.roles) {
                for (roleId in roleIds) {
                    if (role.id == roleId) {
                        val name: String = event.getOption("название_предмета")!!.getAsString()
                        val price: Int = event.getOption("цена")!!.getAsInt()
                        val termo: Int = event.getOption("термозащита")!!.getAsInt()
                        val electro: Int = event.getOption("электрозащита")!!.getAsInt()
                        val chemical: Int = event.getOption("химзащита")!!.getAsInt()
                        val radio: Int = event.getOption("радиозащита")!!.getAsInt()
                        val psi: Int = event.getOption("псизащита")!!.getAsInt()
                        val absorption: Int = event.getOption("гашение")!!.getAsInt()
                        val armor: Int = event.getOption("броня")!!.getAsInt()
                        val containers: Int = event.getOption("контейнеры")!!.getAsInt()
                        val rank: String = event.getOption("ранг")!!.getAsString()
                        dbHandler.addArmorItem(
                            name,
                            rank,
                            price,
                            termo,
                            electro,
                            chemical,
                            radio,
                            psi,
                            absorption,
                            armor,
                            containers
                        )
                        event.reply("Предмет добавлен").queue()
                        res = true
                        break@mainLoop
                    }
                }
            }
            if (!res) {
                event.reply("У вас недостаточно прав").queue()
            }
        }

        fun createWeapon(event: SlashCommandInteractionEvent) {
            val dbHandler = DBHandler()
            val roleIds: List<String> = dbHandler.getRoleIds("admin", 1).split(", ")
            var res = false
            mainLoop@ for (role in event.member!!.getRoles()) {
                for (roleId in roleIds) {
                    if (role.id == roleId) {
                        val name: String = event.getOption("название")!!.getAsString()
                        val firerate: Int = event.getOption("темп_огня")!!.getAsInt()
                        val accuracy: Double = event.getOption("точность")!!.getAsDouble()
                        val range: Int = event.getOption("дальность")!!.getAsInt()
                        val flatness: Int = event.getOption("настильность")!!.getAsInt()
                        val recoil: Double = event.getOption("отдача")!!.getAsDouble()
                        val ammo: Int = event.getOption("боезапас")!!.getAsInt()
                        val weight: Double = event.getOption("вес")!!.getAsDouble()
                        val ammotype: String = event.getOption("тип_патрон")!!.getAsString()
                        val rank: String = event.getOption("ранг")!!.getAsString()
                        val type: String = event.getOption("тип")!!.getAsString()
                        val price: Int = event.getOption("цена")!!.getAsInt()
                        dbHandler.addWeapon(
                            type,
                            rank,
                            name,
                            firerate,
                            accuracy,
                            range,
                            flatness,
                            recoil,
                            ammo,
                            weight,
                            ammotype,
                            price
                        )
                        event.reply("Предмет добавлен").queue()
                        res = true
                        break@mainLoop
                    }
                }
            }
            if (!res) {
                event.reply("У вас недостаточно прав").queue()
            }
        }

        fun deleteWeapon(event: SlashCommandInteractionEvent) {
            val dbHandler = DBHandler()
            val roleIds: List<String> = dbHandler.getRoleIds("admin", 1).split(", ")
            var res = false
            mainLoop@ for (role in event.member!!.getRoles()) {
                for (roleId in roleIds) {
                    if (role.id == roleId) {
                        val id: Int = event.getOption("айди_предмета")!!.getAsInt()
                        dbHandler.removeWeapon(id)
                        event.reply("Оружие удалено").queue()
                        res = true
                        break@mainLoop
                    }
                }
            }
            if (!res) {
                event.reply("У вас недостаточно прав").queue()
            }
        }

        fun createConsumable(event: SlashCommandInteractionEvent) {
            val dbHandler = DBHandler()
            val roleIds: List<String> = dbHandler.getRoleIds("admin", 1).split(", ")
            var res = false
            mainLoop@ for (role in event.member!!.getRoles()) {
                for (roleId in roleIds) {
                    if (role.id == roleId) {
                        val name: String = event.getOption("название")!!.getAsString()
                        val food: Int = event.getOption("сытость")!!.getAsInt()
                        val thirst: Int = event.getOption("жажда")!!.getAsInt()
                        val description: String = event.getOption("описание")!!.getAsString()
                        val rad = if (event.getOption("радиация") == null) 0 else event.getOption("радиация")!!.getAsInt()
                        val psi = if (event.getOption("пси") == null) 0 else event.getOption("пси")!!.getAsInt()
                        val bio = if (event.getOption("био") == null) 0 else event.getOption("био")!!.getAsInt()
                        dbHandler.addConsumable(name, rad, psi, bio, food, thirst, description)
                        event.reply("Предмет добавлен").queue()
                        res = true
                        break@mainLoop
                    }
                }
            }
            if (!res) {
                event.reply("У вас недостаточно прав").queue()
            }
        }

        fun deleteConsumable(event: SlashCommandInteractionEvent) {
            val dbHandler = DBHandler()
            val roleIds: List<String> = dbHandler.getRoleIds("admin", 1).split(", ")
            var res = false
            mainLoop@ for (role in event.member!!.getRoles()) {
                for (roleId in roleIds) {
                    if (role.id == roleId) {
                        val id: Int = event.getOption("айди")!!.getAsInt()
                        dbHandler.removeConsumable(id)
                        event.reply("Предмет удален").queue()
                        res = true
                        break@mainLoop
                    }
                }
            }
            if (!res) {
                event.reply("У вас недостаточно прав").queue()
            }
        }
    }
}
