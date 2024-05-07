package commands

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class KickCommand : Command {
    override fun execute(event: SlashCommandInteractionEvent) {
        val reason = event.getOption("причина")!!.asString
        val user = event.getOption("пользователь")!!.asUser
        val executor = event.user.asMention
        event.guild!!.kick(user, reason).queue()
        event.reply("Пользователь " + user.name + " был выгнан " + event.user.globalName + " по причине: " + reason)
            .queue()
        event.guild!!.getTextChannelById("1160244948789108808")!!
            .sendMessage("Пользователь " + user.asMention + " был выгнан " + executor + " по причине: " + reason)
            .queue()
    }
}
