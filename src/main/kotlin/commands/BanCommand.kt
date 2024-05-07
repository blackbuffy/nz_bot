package commands

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import java.util.concurrent.TimeUnit

class BanCommand : Command {
    override fun execute(event: SlashCommandInteractionEvent) {
        val user = event.getOption("пользователь")!!.asUser
        val reason = event.getOption("причина")!!.asString
        event.deferReply(true).queue()
        event.guild!!
            .ban(user, if (event.getOption("время") == null) 0 else event.getOption("время")!!.asInt, TimeUnit.SECONDS)
            .reason(reason).queue()
        event.hook.sendMessage(
            if (event.getOption("время") == null) "Пользователь " + user.asMention + " был заблокирован " + event.user.globalName + " по причине: " + reason else "Пользователь " + user.asMention + " был заблокирован " + event.user.globalName + " на " + event.getOption(
                "время"
            )!!
                .asInt + " по причине: " + reason
        ).queue()
        event.guild!!.getTextChannelById("1160244948789108808")!!.sendMessage(
            if (event.getOption("время") == null) "Пользователь " + user.asMention + " был заблокирован " + event.user.asMention + " по причине: " + reason else "Пользователь " + user.asMention + " был заблокирован " + event.user.asMention + " на " + event.getOption(
                "время"
            )!!
                .asInt + " по причине: " + reason
        ).queue()
    }
}
