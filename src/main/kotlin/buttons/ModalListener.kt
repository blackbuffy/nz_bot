package buttons

import database.DBHandler
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import java.awt.Color
import java.util.*

class ModalListener : ListenerAdapter() {
    override fun onModalInteraction(event: ModalInteractionEvent) {
        if (event.modalId == "create-character") {
            val name = event.getValue("name")!!.asString
            val biography = event.getValue("biography")!!.asString
            val nature = event.getValue("nature")!!.asString
            val history = event.getValue("history")!!.asString
            val dbHandler = DBHandler()
            dbHandler.insertModalData(event.user.idLong, name, biography, nature, history)
            val roleIdsStr: String = dbHandler.getRoleIds("verify", 2)
            val roleIds = roleIdsStr.split(", ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val closeTicketButton =
                Button.danger("close-ticket", "Закрыть тикет").withEmoji(Emoji.fromUnicode("U+1F512"))
            val claimTicketButton =
                Button.primary("claim-ticket", "Взять тикет").withEmoji(Emoji.fromUnicode("U+1F440"))
            val embedBuilder = EmbedBuilder()
            embedBuilder.setTitle("Панель тикета")
            embedBuilder.setColor(Color(0, 193, 241))
            embedBuilder.setDescription("В этом тикете вы можете задать **любой** вопрос администрации! (связанный с темой тикета)")
            embedBuilder.addField("Имя:", name, true)
            embedBuilder.addField("Биография:", biography, true)
            embedBuilder.addField("Характер:", nature, true)
            embedBuilder.addField("История:", history, true)
            val ticketPanelEmbedMsg = embedBuilder.build()
            val ticketPanelMsg = MessageCreateBuilder()
                .addEmbeds(ticketPanelEmbedMsg)
                .setComponents(ActionRow.of(closeTicketButton, claimTicketButton))
                .build()
            event.guild!!.createTextChannel(
                "ticket-" + event.member!!.user.globalName, event.guild!!
                    .getCategoryById(1153703697767997543L)
            )
                .addPermissionOverride(
                    event.member!!,
                    EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MESSAGE_HISTORY),
                    null
                )
                .addPermissionOverride(
                    event.guild!!.publicRole,
                    null,
                    EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MESSAGE_HISTORY)
                )
                .addRolePermissionOverride(
                    roleIds[0].toLong(),
                    EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MESSAGE_HISTORY),
                    null
                )
                .addRolePermissionOverride(
                    roleIds[1].toLong(),
                    EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MESSAGE_HISTORY),
                    null
                )
                .addRolePermissionOverride(
                    roleIds[2].toLong(),
                    EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MESSAGE_HISTORY),
                    null
                )
                .addRolePermissionOverride(
                    roleIds[3].toLong(),
                    EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MESSAGE_HISTORY),
                    null
                )
                .flatMap { channel: TextChannel ->
                    event.reply("Тикет создан: " + channel.asMention).setEphemeral(true).queue()
                    dbHandler.createTicket(
                        event.user.globalName.toString(),
                        event.user.idLong,
                        "Создание персонажа",
                        channel.idLong
                    )
                    channel.sendMessage(ticketPanelMsg)
                }.queue()
            embedBuilder.clear()
            val loggedInfoField = MessageEmbed.Field(
                "Логи", "Тикет: ticket-" + event.member!!
                    .user.globalName + "\nДействие: создан", true
            )
            val panelField = MessageEmbed.Field("Панель", "Создание персонажа", true)
            val ticketLogsEMsg = embedBuilder
                .setAuthor(
                    event.member!!.user.globalName,
                    "https://cdn.discordapp.com/avatars/1108708352227291156/b3acbbe400865c23594fbe052a1b4fbc.png?size=4096",
                    "https://cdn.discordapp.com/avatars/1108708352227291156/b3acbbe400865c23594fbe052a1b4fbc.png?size=4096"
                )
                .addField(loggedInfoField)
                .addField(panelField)
                .setColor(Color(102, 187, 106))
                .build()
            val ticketLogsMsg = MessageCreateBuilder()
                .addEmbeds(ticketLogsEMsg)
                .build()
            event.guild!!.addRoleToMember(
                event.user,
                event.guild!!.getRoleById(1054835544095473736L)!!
            ).queue()
            event.guild!!.removeRoleFromMember(
                event.user,
                event.guild!!.getRoleById(1054124675665506354L)!!
            ).queue()
            event.guild!!.getTextChannelById(1153712702519255171L)!!.sendMessage(ticketLogsMsg).queue()
        }
    }
}
