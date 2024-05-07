package commands

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

interface Command {
    fun execute(event: SlashCommandInteractionEvent)
}
