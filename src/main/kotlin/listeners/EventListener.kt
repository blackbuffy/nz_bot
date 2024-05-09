package listeners

import ADMIN_STATUS_CHANNEL_ID
import GHWU_MEMBER_ID
import PAKETOV_MEMBER_ID
import SERVER_MEMBER_ROLE_ID
import WELCOME_ZONE_CHANNEL_ID
import database.DBHandler
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent
import net.dv8tion.jda.api.events.user.update.UserUpdateOnlineStatusEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class EventListener: ListenerAdapter() {
    override fun onGuildMemberJoin(event: GuildMemberJoinEvent) {
        val user = event.user
        val guild = event.guild.name

        val msg = "${user.asMention} зашел на сервер $guild!"
        event.guild.getTextChannelById(WELCOME_ZONE_CHANNEL_ID)!!.sendMessage(msg).queue()
        event.guild.addRoleToMember(user, event.guild.getRoleById(SERVER_MEMBER_ROLE_ID)!!).queue()

        val userId = user.idLong
        val dbHandler = DBHandler()
        dbHandler.signUpUser(userId)
    }

    override fun onGuildMemberRemove(event: GuildMemberRemoveEvent) {
        val user = event.user
        val guild = event.guild.name

        val msg = user.asMention + " покинул сервер " + guild + "!"
        event.guild.getTextChannelById(WELCOME_ZONE_CHANNEL_ID)!!.sendMessage(msg).queue()
    }

    override fun onUserUpdateOnlineStatus(event: UserUpdateOnlineStatusEvent) {
        val user = event.user
        val newStatus = event.newOnlineStatus

        if (user.idLong == GHWU_MEMBER_ID || user.idLong == PAKETOV_MEMBER_ID) {
            val statusMessage = if (newStatus == OnlineStatus.ONLINE) "онлайн" else "оффлайн"
            val msg = "${user.globalName} $statusMessage!"
            event.guild.getTextChannelById(ADMIN_STATUS_CHANNEL_ID)!!.sendMessage(msg).queue()
        }
    }
}