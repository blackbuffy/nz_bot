package commands

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class UnbanCommand : Command {
    override fun execute(event: SlashCommandInteractionEvent) {
        val user = event.getOption("пользователь")!!.asUser
        val reason = event.getOption("причина")!!.asString
        event.deferReply(true).queue()
        event.guild!!.unban(user).queue()
        event.hook.sendMessage("Пользователь " + user.asMention + " был разблокирован " + event.user.globalName + " по причине: " + reason)
            .queue()
        event.guild!!.getTextChannelById("1160244948789108808")!!
            .sendMessage("Пользователь " + user.asMention + " был разблокирован " + event.user.asMention + " по причине: " + reason)
            .queue()
    }
}
