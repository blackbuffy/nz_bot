package buttons

import database.DBHandler
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import net.dv8tion.jda.api.interactions.modals.Modal
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder
import net.dv8tion.jda.api.utils.messages.MessageEditData
import java.awt.Color
import java.util.*

class ButtonListener : ListenerAdapter() {
    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        if (event.button.id == "create-character-button") {
            val name = TextInput.create("name", "ФИО, кличка/позывной", TextInputStyle.SHORT)
                .setPlaceholder("ФИО персонажа и его кличка/позывной")
                .setMinLength(8)
                .setMaxLength(128)
                .build()
            val biography = TextInput.create("biography", "Биография", TextInputStyle.PARAGRAPH)
                .setPlaceholder("Краткая биография персонажа")
                .setMinLength(32)
                .setMaxLength(1024)
                .build()
            val nature = TextInput.create("nature", "Характер", TextInputStyle.PARAGRAPH)
                .setPlaceholder("Характер персонажа, кратко")
                .setMinLength(16)
                .setMaxLength(512)
                .build()
            val history = TextInput.create("history", "История", TextInputStyle.PARAGRAPH)
                .setPlaceholder("История о том, как персонаж попал в Зону")
                .setMinLength(16)
                .setMaxLength(512)
                .build()
            val modal = Modal.create("create-character", "Создание персонажа")
                .addComponents(ActionRow.of(name), ActionRow.of(nature), ActionRow.of(history), ActionRow.of(biography))
                .build()
            event.replyModal(modal).queue()
        } else if (event.button.id == "close-ticket") {
            val dbHandler = DBHandler()
            val roleIdsStr: String = dbHandler.getRoleIds("verify", 2)
            val roleIds = roleIdsStr.split(", ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            mainLoop@ while (true) {
                for (role in event.member!!.roles) {
                    for (roleId in roleIds) {
                        if (roleId == role.id) {
                            val confirmCloseTicketButton = Button.success("confirm-close-ticket", "Подтвердить")
                                .withEmoji(Emoji.fromUnicode("U+2705"))
                            val cancelCloseTicketButton =
                                Button.danger("cancel-close-ticket", "Отменить").withEmoji(Emoji.fromUnicode("U+274C"))
                            val confirmCloseMsg = MessageCreateBuilder()
                                .addContent("Подтвердите закрытие тикета.")
                                .setComponents(ActionRow.of(confirmCloseTicketButton, cancelCloseTicketButton))
                                .build()
                            event.deferEdit().closeResources().queue()
                            event.channel.sendMessage(confirmCloseMsg).queue()
                            break@mainLoop
                        }
                    }
                }
            }
        } else if (event.button.id == "confirm-close-ticket") {
            val dbHandler = DBHandler()
            val roleIdsStr: String = dbHandler.getRoleIds("verify", 2)
            val roleIds = roleIdsStr.split(", ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            mainLoop@ while (true) {
                for (role in event.member!!.roles) {
                    for (roleId in roleIds) {
                        if (role.id == roleId) {
                            val embedBuilder = EmbedBuilder()
                            val ticketClosedEMsg = embedBuilder
                                .setColor(Color(251, 254, 50))
                                .setDescription("Тикет закрыт администратором " + event.member!!.asMention)
                                .build()
                            val ticketClosedMsg = MessageCreateBuilder()
                                .addEmbeds(ticketClosedEMsg)
                                .build()
                            event.channel.sendMessage(ticketClosedMsg).queue()
                            embedBuilder.clear()
                            val openTicketButton =
                                Button.success("open-ticket", "Открыть").withEmoji(Emoji.fromUnicode("U+1f513"))
                            val deleteTicketButton =
                                Button.danger("delete-ticket", "Удалить").withEmoji(Emoji.fromUnicode("U+26D4"))
                            val ticketControlEMsg = embedBuilder
                                .setColor(Color(18, 125, 181))
                                .setDescription("Управление тикетом")
                                .build()
                            val ticketControlMsg = MessageCreateBuilder()
                                .addEmbeds(ticketControlEMsg)
                                .setComponents(ActionRow.of(openTicketButton, deleteTicketButton))
                                .build()
                            event.channel.sendMessage(ticketControlMsg).queue()
                            dbHandler.closeTicket(
                                dbHandler.getTicketName(event.channel.idLong), event.channel, event.guild!!
                                    .getCategoryById(1153704500826226689L)
                            )
                            val id: Long = dbHandler.getCreatorId(dbHandler.getTicketName(event.channel.idLong))
                            event.guild!!
                                .removeRoleFromMember(
                                    event.guild!!.getMemberById(id)!!.user, event.guild!!
                                        .getRoleById(1054835544095473736L)!!
                                ).queue()
                            event.guild!!.addRoleToMember(
                                event.guild!!.getMemberById(id)!!.user, event.guild!!
                                    .getRoleById(1054124675665506354L)!!
                            ).queue()
                            event.message.delete().queue()
                            embedBuilder.clear()
                            val loggedInfoField = MessageEmbed.Field(
                                "Логи", """
     Тикет: ${event.channel.name}
     Действие: закрыт
     """.trimIndent(), true
                            )
                            val panelField = MessageEmbed.Field("Панель", "Панель тикета", true)
                            val ticketLogsEMsg = embedBuilder
                                .setAuthor(
                                    event.member!!.user.globalName,
                                    "https://cdn.discordapp.com/avatars/1108708352227291156/b3acbbe400865c23594fbe052a1b4fbc.png?size=4096",
                                    "https://cdn.discordapp.com/avatars/1108708352227291156/b3acbbe400865c23594fbe052a1b4fbc.png?size=4096"
                                )
                                .addField(loggedInfoField)
                                .addField(panelField)
                                .setColor(Color(255, 238, 88))
                                .build()
                            val ticketLogsMsg = MessageCreateBuilder()
                                .addEmbeds(ticketLogsEMsg)
                                .build()
                            event.guild!!.getTextChannelById(1153712702519255171L)!!.sendMessage(ticketLogsMsg).queue()
                            event.deferEdit().closeResources().queue()
                            break@mainLoop
                        }
                    }
                }
            }
        } else if (event.button.id == "cancel-close-ticket") {
            val dbHandler = DBHandler()
            val roleIdsStr: String = dbHandler.getRoleIds("verify", 2)
            val roleIds = roleIdsStr.split(", ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            mainLoop@ while (true) {
                for (role in event.member!!.roles) {
                    for (roleId in roleIds) {
                        if (roleId == role.id) {
                            event.guild!!.getTextChannelsByName(
                                event.channel.name,
                                false
                            )[0].deleteMessageById(event.messageIdLong).queue()
                            break@mainLoop
                        }
                    }
                }
            }
        } else if (event.button.id == "open-ticket") {
            val dbHandler = DBHandler()
            val roleIdsStr: String = dbHandler.getRoleIds("verify", 2)
            val roleIds = roleIdsStr.split(", ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            mainLoop@ while (true) {
                for (role in event.member!!.roles) {
                    for (roleId in roleIds) {
                        if (roleId == role.id) {
                            val embedBuilder = EmbedBuilder()
                            val ticketOpenedEMsg = embedBuilder
                                .setColor(Color(30, 196, 91))
                                .setDescription("Тикет открыт администратором " + event.member!!.asMention)
                                .build()
                            val ticketOpenedMsg = MessageCreateBuilder()
                                .addEmbeds(ticketOpenedEMsg)
                                .build()
                            event.channel.sendMessage(ticketOpenedMsg).queue()
                            event.message.delete().queue()
                            dbHandler.openTicket(
                                dbHandler.getTicketName(event.channel.idLong), event.channel, event.guild!!
                                    .getCategoryById(1153703697767997543L)
                            )
                            embedBuilder.clear()
                            val loggedInfoField = MessageEmbed.Field(
                                "Логи", """
     Тикет: ${event.channel.name}
     Действие: открыт
     """.trimIndent(), true
                            )
                            val panelField = MessageEmbed.Field("Панель", "Управление тикетом", true)
                            val ticketLogsEMsg = embedBuilder
                                .setAuthor(
                                    event.member!!.user.globalName,
                                    "https://cdn.discordapp.com/avatars/1108708352227291156/b3acbbe400865c23594fbe052a1b4fbc.png?size=4096",
                                    "https://cdn.discordapp.com/avatars/1108708352227291156/b3acbbe400865c23594fbe052a1b4fbc.png?size=4096"
                                )
                                .addField(loggedInfoField)
                                .addField(panelField)
                                .setColor(Color(0, 193, 241))
                                .build()
                            val ticketLogsMsg = MessageCreateBuilder()
                                .addEmbeds(ticketLogsEMsg)
                                .build()
                            event.guild!!.getTextChannelById(1153712702519255171L)!!.sendMessage(ticketLogsMsg).queue()
                            break@mainLoop
                        }
                    }
                }
            }
        } else if (event.button.id == "delete-ticket") {
            val dbHandler = DBHandler()
            val roleIdsStr: String = dbHandler.getRoleIds("verify", 1)
            val roleIds = roleIdsStr.split(", ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            mainLoop@ while (true) {
                for (role in event.member!!.roles) {
                    for (roleId in roleIds) {
                        if (roleId == role.id) {
                            event.channel.sendMessage("Тикет будет удален через несколько секунд...")
                            val embedBuilder = EmbedBuilder()
                            val loggedInfoField = MessageEmbed.Field(
                                "Логи", """
     Тикет: ${event.channel.name}
     Действие: удален
     """.trimIndent(), true
                            )
                            val panelField = MessageEmbed.Field("Панель", "Управление тикетом", true)
                            val ticketLogsEMsg = embedBuilder
                                .setAuthor(
                                    event.member!!.user.globalName,
                                    "https://cdn.discordapp.com/avatars/1108708352227291156/b3acbbe400865c23594fbe052a1b4fbc.png?size=4096",
                                    "https://cdn.discordapp.com/avatars/1108708352227291156/b3acbbe400865c23594fbe052a1b4fbc.png?size=4096"
                                )
                                .addField(loggedInfoField)
                                .addField(panelField)
                                .setColor(Color(239, 82, 80))
                                .build()
                            val ticketLogsMsg = MessageCreateBuilder()
                                .addEmbeds(ticketLogsEMsg)
                                .build()
                            event.guild!!.getTextChannelById(1153712702519255171L)!!.sendMessage(ticketLogsMsg).queue()
                            dbHandler.deleteTicket(event.channel)
                            break@mainLoop
                        }
                    }
                }
            }
        } else if (event.button.id == "claim-ticket") {
            val dbHandler = DBHandler()
            val roleIdsStr: String = dbHandler.getRoleIds("verify", 2)
            val roleIds = roleIdsStr.split(", ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            mainLoop@ while (true) {
                for (role in event.member!!.roles) {
                    for (roleId in roleIds) {
                        if (roleId == role.id) {
                            dbHandler.setAdminId(
                                event.user.idLong,
                                event.channel.name.split("-".toRegex()).dropLastWhile { it.isEmpty() }
                                    .toTypedArray()[1])
                            val eb = EmbedBuilder()
                            val emsg = eb
                                .setDescription("Администратор " + event.user.globalName + " взял этот тикет.")
                                .setColor(Color(18, 125, 181))
                                .build()
                            val manager = event.channel.asTextChannel().manager
                            manager.putMemberPermissionOverride(
                                dbHandler.getAdminId(
                                    event.channel.name.split("-".toRegex()).dropLastWhile { it.isEmpty() }
                                        .toTypedArray()[1]),
                                EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND),
                                null)
                            for (roleId2 in roleIds) {
                                if (roleId2.toLong() != dbHandler.getRoleId("Тех. Администратор")) {
                                    manager.putRolePermissionOverride(
                                        roleId2.toLong(),
                                        null,
                                        EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND)
                                    )
                                }
                            }
                            manager.queue()
                            val msg = MessageCreateBuilder()
                                .addEmbeds(emsg)
                                .build()
                            event.channel.sendMessage(msg).queue()
                            event.deferEdit().closeResources().queue()
                            break@mainLoop
                        }
                    }
                }
            }
        } else if (event.button.id == "close-list") {
            event.message.delete().queue()
            event.reply("Список закрыт").setEphemeral(true).queue()
        } else if (event.button.id == "page-next") {
            val dbHandler = DBHandler()
            val pages = event.message.embeds[0].footer!!.text!!.split("/".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
            val type = event.message.embeds[0].title.toString().split(" ".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()[1]
            var typeSrc = ""
            when (type) {
                "брони" -> {
                    typeSrc = "броня"
                }
                "оружия" -> {
                    typeSrc = "оружие"
                }
                "провизии" -> {
                    typeSrc = "провизия"
                }
            }
            val pageCurrent = pages[0].toInt()
            val pageLast = pages[1].toInt()
            val arr: Collection<String> = dbHandler.getAllItems(typeSrc)
            val descSB = StringBuilder()
            if (pageCurrent + 1 == pageLast) {
                for (i in pageCurrent * 10..<arr.size) {
                    val ez = arr.toTypedArray()[i]
                    descSB.append("**" + ez + " (ID: " + dbHandler.getItemId(typeSrc, ez) + ")**\n")
                }
            } else {
                for (i in pageCurrent * 10..<(pageCurrent + 1) * 10) {
                    val ez = arr.toTypedArray()[i]
                    descSB.append("**" + ez + " (ID: " + dbHandler.getItemId(typeSrc, ez) + ")**\n")
                }
            }
            val descStr = descSB.toString()
            val eb = EmbedBuilder()
            eb.setTitle("Список $type")
            eb.setDescription(descStr)
            eb.setColor(Color(0, 193, 241))
            eb.setFooter((pageCurrent + 1).toString() + "/" + pageLast)
            val emsg: MessageEditData = if (pageCurrent + 1 == pageLast) {
                val closeList = Button.danger("close-list", "X")
                val pagePrev = Button.primary("page-prev", "<")
                val pageFirst = Button.primary("page-first", "<<")
                MessageEditBuilder()
                    .setEmbeds(eb.build())
                    .setComponents(ActionRow.of(closeList, pagePrev, pageFirst))
                    .build()
            } else {
                val pagePrev = Button.primary("page-prev", "<")
                val pageFirst = Button.primary("page-first", "<<")
                val closeList = Button.danger("close-list", "X")
                val pageNext = Button.primary("page-next", ">")
                val pageLastBtn = Button.primary("page-last", ">>")
                MessageEditBuilder()
                    .setEmbeds(eb.build())
                    .setComponents(ActionRow.of(pageFirst, pagePrev, closeList, pageNext, pageLastBtn))
                    .build()
            }
            event.editMessage(emsg).queue()
        } else if (event.button.id == "page-prev") {
            val dbHandler = DBHandler()
            val pages = event.message.embeds[0].footer!!.text!!.split("/".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
            val type = event.message.embeds[0].title.toString().split(" ".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()[1]
            var typeSrc = ""
            when (type) {
                "брони" -> {
                    typeSrc = "броня"
                }
                "оружия" -> {
                    typeSrc = "оружие"
                }
                "провизии" -> {
                    typeSrc = "провизия"
                }
            }
            val pageCurrent = pages[0].toInt()
            val pageLast = pages[1].toInt()
            val arr: Collection<String> = dbHandler.getAllItems(typeSrc)
            val descSB = StringBuilder()
            if (pageCurrent - 1 == 1) {
                for (i in 0..9) {
                    val ez = arr.toTypedArray()[i]
                    descSB.append("**" + ez + " (ID: " + dbHandler.getItemId(typeSrc, ez) + ")**\n")
                }
            } else {
                for (i in (pageCurrent - 2) * 10..<(pageCurrent - 1) * 10) {
                    val ez = arr.toTypedArray()[i]
                    descSB.append("**" + ez + " (ID: " + dbHandler.getItemId(typeSrc, ez) + ")**\n")
                }
            }
            val descStr = descSB.toString()
            val eb = EmbedBuilder()
            eb.setTitle("Список $type")
            eb.setDescription(descStr)
            eb.setColor(Color(0, 193, 241))
            eb.setFooter((pageCurrent - 1).toString() + "/" + pageLast)
            val emsg: MessageEditData = if (pageCurrent - 1 == 1) {
                val closeList = Button.danger("close-list", "X")
                val pageNext = Button.primary("page-next", ">")
                val pageLastBtn = Button.primary("page-last", ">>")
                MessageEditBuilder()
                    .setEmbeds(eb.build())
                    .setComponents(ActionRow.of(closeList, pageNext, pageLastBtn))
                    .build()
            } else {
                val pagePrev = Button.primary("page-prev", "<")
                val pageFirst = Button.primary("page-first", "<<")
                val closeList = Button.danger("close-list", "X")
                val pageNext = Button.primary("page-next", ">")
                val pageLastBtn = Button.primary("page-last", ">>")
                MessageEditBuilder()
                    .setEmbeds(eb.build())
                    .setComponents(ActionRow.of(pageFirst, pagePrev, closeList, pageNext, pageLastBtn))
                    .build()
            }
            event.editMessage(emsg).queue()
        } else if (event.button.id == "page-last") {
            val dbHandler = DBHandler()
            val pages = event.message.embeds[0].footer!!.text!!.split("/".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
            val type = event.message.embeds[0].title.toString().split(" ".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()[1]
            var typeSrc = ""
            when (type) {
                "брони" -> {
                    typeSrc = "броня"
                }
                "оружия" -> {
                    typeSrc = "оружие"
                }
                "провизии" -> {
                    typeSrc = "провизия"
                }
            }
            val pageLast = pages[1].toInt()
            val arr: Collection<String> = dbHandler.getAllItems(typeSrc)
            val descSB = StringBuilder()
            for (i in (pageLast - 1) * 10..<arr.size) {
                val ez = arr.toTypedArray()[i]
                descSB.append("**" + ez + " (ID: " + dbHandler.getItemId(typeSrc, ez) + ")**\n")
            }
            val descStr = descSB.toString()
            val eb = EmbedBuilder()
            eb.setTitle("Список $type")
            eb.setDescription(descStr)
            eb.setColor(Color(0, 193, 241))
            eb.setFooter("$pageLast/$pageLast")
            val emsg: MessageEditData = if (pageLast == 1) {
                val closeList = Button.danger("close-list", "X")
                MessageEditBuilder()
                    .setEmbeds(eb.build())
                    .setComponents(ActionRow.of(closeList))
                    .build()
            } else {
                val pagePrev = Button.primary("page-prev", "<")
                val pageFirst = Button.primary("page-first", "<<")
                val closeList = Button.danger("close-list", "X")
                MessageEditBuilder()
                    .setEmbeds(eb.build())
                    .setComponents(ActionRow.of(pageFirst, pagePrev, closeList))
                    .build()
            }
            event.editMessage(emsg).queue()
        } else if (event.button.id == "page-first") {
            val dbHandler = DBHandler()
            val pages = event.message.embeds[0].footer!!.text!!.split("/".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
            val type = event.message.embeds[0].title.toString().split(" ".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()[1]
            var typeSrc = ""
            when (type) {
                "брони" -> {
                    typeSrc = "броня"
                }
                "оружия" -> {
                    typeSrc = "оружие"
                }
                "провизии" -> {
                    typeSrc = "провизия"
                }
            }
            val pageLast = pages[1].toInt()
            val arr: Collection<String> = dbHandler.getAllItems(typeSrc)
            val descSB = StringBuilder()
            for (i in 0..<if (arr.size < 10) arr.size else 10) {
                val ez = arr.toTypedArray()[i]
                descSB.append("**" + ez + " (ID: " + dbHandler.getItemId(typeSrc, ez) + ")**\n")
            }
            val descStr = descSB.toString()
            val eb = EmbedBuilder()
            eb.setTitle("Список $type")
            eb.setDescription(descStr)
            eb.setColor(Color(0, 193, 241))
            eb.setFooter("1/$pageLast")
            val emsg: MessageEditData = if (1 == pageLast) {
                val closeList = Button.danger("close-list", "X")
                MessageEditBuilder()
                    .setEmbeds(eb.build())
                    .setComponents(ActionRow.of(closeList))
                    .build()
            } else {
                val closeList = Button.danger("close-list", "X")
                val pageNext = Button.primary("page-next", ">")
                val pageLastBtn = Button.primary("page-last", ">>")
                MessageEditBuilder()
                    .setEmbeds(eb.build())
                    .setComponents(ActionRow.of(closeList, pageNext, pageLastBtn))
                    .build()
            }
            event.editMessage(emsg).queue()
        }
    }
}