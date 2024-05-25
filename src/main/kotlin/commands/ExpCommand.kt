package commands

import database.DBHandler
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import java.awt.Color

class ExpCommand : Command {
    override fun execute(event: SlashCommandInteractionEvent) {
        when (event.subcommandGroup) {
            "модификатор" -> {
                when (event.subcommandName) {
                    "посмотреть" -> {
                        getModificator(event)
                    }
                    "использовать" -> {
                        useModificator(event)
                    }
                }
            }
            else -> {
                when (event.subcommandName) {
                    "посмотреть" -> {
                        getExp(event)
                    }
                    "выдать" -> {
                        giveExp(event)
                    }
                }
            }
        }
    }

    companion object {
        fun useModificator(event: SlashCommandInteractionEvent) {
            val dbHandler = DBHandler()

            val user = event.user
            dbHandler.useModificator(user.idLong)

            var sum = 1.0
            dbHandler.getModificator(user.idLong, true).forEach {
                sum *= it
            }

            event.reply("Ваш новый модификатор: *x${String.format("%.2f", sum)}*").queue()
        }

        fun getModificator(event: SlashCommandInteractionEvent) {
            val dbHandler = DBHandler()

            val user = event.user
            val status = event.getOption("активные")!!.asBoolean

            val list = dbHandler.getModificator(user.idLong, status)
            when (status) {
                true -> {
                    var sum = 1.0

                    list.forEach {
                        sum *= it
                    }

                    event.reply("Ваш активный модификатор на следующее задание: x*${String.format("%.2f", sum)}*").queue()
                }
                else -> {
                    var sum = 1.0f

                    list.forEach {
                        sum *= it
                    }

                    event.reply("Ваш неактивный модификатор: x*${String.format("%.2f", sum)}*").queue()
                }
            }
        }

        fun getExp(event: SlashCommandInteractionEvent) {
            val dbHandler = DBHandler()
            val eb = EmbedBuilder()
            val user: User =
                if (event.getOption("пользователь") == null) event.user else event.getOption("пользователь")!!
                    .asUser
            val arr: Collection<String> = dbHandler.getExp(user.idLong)
            val descSB = StringBuilder()
            for (e in arr) {
                descSB.append(e).append("\n")
            }
            val descStr = descSB.toString()
            if (descStr == "false\n") {
                event.reply("У пользователя нет профиля").queue()
            } else {
                val emsg: MessageEmbed = eb
                    .setColor(Color(18, 125, 181))
                    .setTitle("Опыт игрока " + user.globalName)
                    .setDescription(descStr)
                    .build()
                val msg: MessageCreateData = MessageCreateBuilder()
                    .addEmbeds(emsg)
                    .build()
                event.reply(msg).queue()
            }
        }

        fun giveExp(event: SlashCommandInteractionEvent) {
            val dbHandler = DBHandler()
            val roleIds: List<String> = dbHandler.getRoleIds("gm", 1).split(", ")
            var res = false
            mainLoop@ for (role in event.member!!.roles) {
                for (roleId in roleIds) {
                    when (roleId) {
                        role.id -> {
                            val user = event.getOption("пользователь")!!.asMember
                            var rankXP: Int = event.getOption("ранг")?.asInt ?: 0
                            var repXP: Int = event.getOption("репутация")?.asInt ?: 0
                            val useModificator = event.getOption("модификатор")!!.asBoolean
                            var modificator = 1.0f

                            if (useModificator) {
                                modificator = dbHandler.applyModificatr(user!!.idLong)
                            }

                            rankXP = (rankXP.times(modificator)).toInt()
                            repXP = (repXP.times(modificator)).toInt()

                            val answerRank: String? = dbHandler.addRankExp(user!!.idLong, rankXP)
                            val answerRep: String? = dbHandler.addRepExp(user.idLong, repXP)
                            event.reply("Опыт выдан пользователю ${user.asMention} в количестве: $rankXP Rank XP и $repXP Rep!").queue()

                            val list = user.roles
                            if (answerRank != null) {
                                event.hook.sendMessage("${user.asMention} получил новый ранг: \"$answerRank\"!")
                                    .queue()
                                for (rl in list) when (rl.name) {
                                    "Зеленый" -> {
                                        event.guild!!.removeRoleFromMember(
                                            user,
                                            event.guild!!.getRolesByName("Зеленый", true)[0]
                                        ).queue()
                                    }
                                    "Новичек" -> {
                                        event.guild!!.removeRoleFromMember(
                                            user,
                                            event.guild!!.getRolesByName("Новичек", true)[0]
                                        ).queue()
                                    }
                                    "Неопытный" -> {
                                        event.guild!!.removeRoleFromMember(
                                            user,
                                            event.guild!!.getRolesByName("Неопытный", true)[0]
                                        ).queue()
                                    }
                                    "Бывалый" -> {
                                        event.guild!!.removeRoleFromMember(
                                            user,
                                            event.guild!!.getRolesByName("Бывалый", true)[0]
                                        ).queue()
                                    }
                                    "Профессионал" -> {
                                        event.guild!!.removeRoleFromMember(
                                            user,
                                            event.guild!!.getRolesByName("Профессионал", true)[0]
                                        ).queue()
                                    }
                                    "Ветеран" -> {
                                        event.guild!!.removeRoleFromMember(
                                            user,
                                            event.guild!!.getRolesByName("Ветеран", true)[0]
                                        ).queue()
                                    }
                                    "Мастер" -> {
                                        event.guild!!.removeRoleFromMember(
                                            user,
                                            event.guild!!.getRolesByName("Мастер", true)[0]
                                        ).queue()
                                    }
                                    "Элита" -> {
                                        event.guild!!.removeRoleFromMember(
                                            user,
                                            event.guild!!.getRolesByName("Элита", true)[0]
                                        ).queue()
                                    }
                                }
                                event.guild!!.addRoleToMember(user, event.guild!!.getRolesByName(answerRank, true)[0])
                                    .queue()
                            }
                            if (answerRep != null) {
                                event.hook.sendMessage("${user.asMention} получил новый уровень репутации: \"$answerRep\"!")
                                    .queue()
                                for (rl in list) {
                                    when (rl.name) {
                                        "Ужасная" -> {
                                            event.guild!!.removeRoleFromMember(
                                                user,
                                                event.guild!!.getRolesByName("Ужасная", true)[0]
                                            ).queue()
                                        }
                                        "Очень плохая" -> {
                                            event.guild!!.removeRoleFromMember(
                                                user,
                                                event.guild!!.getRolesByName("Очень плохая", true)[0]
                                            ).queue()
                                        }
                                        "Плохая" -> {
                                            event.guild!!.removeRoleFromMember(
                                                user,
                                                event.guild!!.getRolesByName("Плохая", true)[0]
                                            ).queue()
                                        }
                                        "Нейтральная" -> {
                                            event.guild!!.removeRoleFromMember(
                                                user,
                                                event.guild!!.getRolesByName("Нейтральная", true)[0]
                                            ).queue()
                                        }
                                        "Хорошая" -> {
                                            event.guild!!.removeRoleFromMember(
                                                user,
                                                event.guild!!.getRolesByName("Хорошая", true)[0]
                                            ).queue()
                                        }
                                        "Очень хорошая" -> {
                                            event.guild!!.removeRoleFromMember(
                                                user,
                                                event.guild!!.getRolesByName("Очень хорошая", true)[0]
                                            ).queue()
                                        }
                                        "Отличная" -> {
                                            event.guild!!.removeRoleFromMember(
                                                user,
                                                event.guild!!.getRolesByName("Отличная", true)[0]
                                            ).queue()
                                        }
                                        "Великолепная" -> {
                                            event.guild!!.removeRoleFromMember(
                                                user,
                                                event.guild!!.getRolesByName("Великолепная", true)[0]
                                            ).queue()
                                        }
                                    }
                                }
                            }
                            res = true
                            break@mainLoop
                        }
                    }
                }
            }
            if (!res) {
                event.reply("У вас недостаточно прав")
            }
        }
    }
}
