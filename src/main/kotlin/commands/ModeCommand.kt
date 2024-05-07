package commands

import database.DBHandler
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class ModeCommand : Command {
    override fun execute(event: SlashCommandInteractionEvent) {
        mode(event)
    }

    companion object {
        fun mode(event: SlashCommandInteractionEvent) {
            val dbHandler = DBHandler()
            var res = false
            val modeStr: String = event.getOption("режим")!!.getAsString()
            val roleIds: List<String> = dbHandler.getRoleIds("test", 2).split(", ")
            val mode = if (modeStr == "dev") 0 else if (modeStr == "test") 1 else 2
            val userid: Long = event.getUser().getIdLong()
            if (mode == 0) {
                val roleId: String = java.lang.String.valueOf(dbHandler.getRoleId("Тех. Администратор"))
                mainLoop@ for (role in event.getMember()!!.getRoles()) {
                    if (roleId == role.id) {
                        dbHandler.setMode(userid, mode)
                        event.reply("Режим изменен!").queue()
                        res = true
                        break@mainLoop
                    }
                }
            } else {
                mainLoop@ for (role in event.getMember()!!.getRoles()) {
                    for (roleId in roleIds) {
                        if (roleId == role.id) {
                            dbHandler.setMode(userid, mode)
                            event.reply("Режим изменен!").queue()
                            res = true
                            break@mainLoop
                        }
                    }
                }
            }
            if (!res) {
                event.reply("У вас недостаточно прав").queue()
            }
        }
    }
}
