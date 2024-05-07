package commands

import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import java.util.concurrent.ExecutionException

class ClearCommand : Command {
    override fun execute(event: SlashCommandInteractionEvent) {
        val count = event.getOption("количество")!!.asInt
        val channel = event.channel
        var i = 0
        try {
            i = channel.iterableHistory.takeAsync(count).get().size
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: ExecutionException) {
            e.printStackTrace()
        }
        channel.iterableHistory.takeAsync(count).thenAccept { messages: List<Message?>? ->
            channel.purgeMessages(
                messages!!
            )
        }
        event.reply("Успешно удалено $i сообщений!").queue()
    }
}