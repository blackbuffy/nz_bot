package commands

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import java.time.Duration

class MuteCommand : Command {
    override fun execute(event: SlashCommandInteractionEvent) {
        val user = event.getOption("пользователь")!!.asUser
        val duration = event.getOption("секунд")!!.asInt
        val reason = event.getOption("причина")!!.asString
        event.guild!!.getMemberById(user.idLong)!!.timeoutFor(Duration.ofSeconds(duration.toLong())).queue()
        event.reply("Пользователь " + user.asMention + " успешно получил мут на " + duration + " секунд по причине: " + reason)
            .queue()
        event.guild!!.getTextChannelById(1160244948789108808L)!!
            .sendMessage("Пользователь " + user.asMention + " был наказан " + event.member!!.asMention + " мутом на " + duration + " секунд по причине: " + reason)
            .queue()
    }
}
