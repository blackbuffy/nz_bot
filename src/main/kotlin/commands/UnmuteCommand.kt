package commands

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class UnmuteCommand : Command {
    override fun execute(event: SlashCommandInteractionEvent) {
        val user = event.getOption("пользователь")!!.asUser
        val reason = event.getOption("причина")!!.asString
        event.guild!!.getMemberById(user.idLong)!!.removeTimeout().queue()
        event.reply("Пользователь " + user.asMention + " успешно помилован по причине: " + reason).queue()
        event.guild!!.getTextChannelById(1160244948789108808L)!!
            .sendMessage("Пользователь " + user.asMention + " был помилован " + event.member!!.asMention + " по причине: " + reason)
            .queue()
    }
}
