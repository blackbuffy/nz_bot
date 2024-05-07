package commands

import database.DBHandler
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import java.awt.Color

class ProfileCommand : Command {
    override fun execute(event: SlashCommandInteractionEvent) {
        if (event.getSubcommandName() == "создать") {
            createProfile(event)
        } else if (event.getSubcommandName() == "посмотреть") {
            getProfile(event)
        } else if (event.getSubcommandName() == "обновить_нужды") {
            updateNeeds(event)
        } else if (event.getSubcommandName() == "нужды") {
            getNeeds(event)
        } else if (event.getSubcommandName() == "навыки") {
            getSkills(event)
        } else if (event.getSubcommandName() == "обновить_навыки") {
            updateSkills(event)
        } else if (event.getSubcommandName() == "здоровье") {
            getHP(event)
        } else if (event.getSubcommandName() == "обновить_здоровье") {
            updateHP(event)
        }
    }

    companion object {
        fun createProfile(event: SlashCommandInteractionEvent) {
            val dbHandler = DBHandler()
            val roleIds: List<String> = dbHandler.getRoleIds("verify", 1).split(", ")
            var res = false
            mainLoop@ for (role in event.getMember()!!.getRoles()) {
                for (roleId in roleIds) {
                    if (roleId == role.id) {
                        val user: User = event.getOption("пользователь")!!.getAsUser()
                        val userid = user.idLong
                        val name: String = event.getOption("имя")!!.getAsString()
                        val age: Int = event.getOption("возраст")!!.getAsInt()
                        val moniker: String = event.getOption("прозвище")!!.getAsString()
                        val fraction: String = event.getOption("фракция")!!.getAsString()
                        dbHandler.createProfile(userid, age, moniker, fraction)
                        event.reply("Профиль игрока " + user.globalName + " создан успешно").queue()
                        event.getGuild()!!.addRoleToMember(user, event.getGuild()!!.getRoleById(1054124676294660137L)!!)
                            .queue()
                        res = true
                        break@mainLoop
                    }
                }
            }
            if (!res) {
                event.reply("У вас недостаточно прав").queue()
            }
        }

        fun getProfile(event: SlashCommandInteractionEvent) {
            val dbHandler = DBHandler()
            val eb = EmbedBuilder()
            val user: User =
                if (event.getOption("пользователь") == null) event.getUser() else event.getOption("пользователь")!!
                    .getAsUser()
            val arr: Collection<String> = dbHandler.getProfile(user.idLong)
            val descSB = StringBuilder()
            for (e in arr) {
                descSB.append(e).append("\n")
            }
            val descStr = descSB.toString()
            if (descStr == "false\n") {
                event.reply("У пользователя нет профиля").queue()
            } else {
                val emsg: MessageEmbed = eb
                    .setTitle("Профиль игрока " + user.globalName)
                    .setDescription(descStr)
                    .setColor(Color(18, 125, 181))
                    .build()
                val msg: MessageCreateData = MessageCreateBuilder()
                    .addEmbeds(emsg)
                    .build()
                event.reply(msg).queue()
            }
        }

        fun updateNeeds(event: SlashCommandInteractionEvent) {
            val dbHandler = DBHandler()
            val user: User = event.getUser()
            val userid = user.idLong
            val roleIds: List<String> = dbHandler.getRoleIds("gm", 1).split(", ")
            var res = false
            mainLoop@ for (role in event.getMember()!!.getRoles()) {
                for (roleId in roleIds) {
                    if (role.id == roleId) {
                        val stamina = if (event.getOption("выносливость") != null) event.getOption("выносливость")!!
                            .getAsInt() else 0
                        val thirst = if (event.getOption("жажда") != null) event.getOption("жажда")!!.getAsInt() else 0
                        val hunger = if (event.getOption("голод") != null) event.getOption("голод")!!.getAsInt() else 0
                        val hp = if (event.getOption("общее_здоровье") != null) event.getOption("общее_здоровье")!!
                            .getAsInt() else 0
                        val larmhp = if (event.getOption("здоровье_л_руки") != null) event.getOption("здоровье_л_руки")!!
                            .getAsInt() else 0
                        val rarmhp = if (event.getOption("здоровье_п_руки") != null) event.getOption("здоровье_п_руки")!!
                            .getAsInt() else 0
                        val lleghp = if (event.getOption("здоровье_л_ноги") != null) event.getOption("здоровье_л_ноги")!!
                            .getAsInt() else 0
                        val rleghp = if (event.getOption("здоровье_п_ноги") != null) event.getOption("здоровье_п_ноги")!!
                            .getAsInt() else 0
                        val torsohp = if (event.getOption("здоровье_торса") != null) event.getOption("здоровье_торс")!!
                            .getAsInt() else 0
                        val headhp = if (event.getOption("здоровье_головы") != null) event.getOption("здоровье_голова")!!
                            .getAsInt() else 0
                        val overallhp = if (event.getOption("самочувствие") != null) event.getOption("самочувствие")!!
                            .getAsInt() else 0
                        dbHandler.updateCharacterNeeds(
                            userid,
                            stamina,
                            thirst,
                            hunger,
                            hp,
                            larmhp,
                            rarmhp,
                            lleghp,
                            rleghp,
                            torsohp,
                            headhp,
                            overallhp
                        )
                        event.reply("Потребности игрока " + user.globalName + " успешно обновлены").queue()
                        res = true
                        break@mainLoop
                    }
                }
            }
            if (!res) {
                event.reply("У вас недостаточно прав").queue()
            }
        }

        fun updateSkills(event: SlashCommandInteractionEvent) {
            val dbHandler = DBHandler()
            val user: User = event.getUser()
            val userid = user.idLong
            val roleIds: List<String> = dbHandler.getRoleIds("gm", 1).split(", ")
            var res = false
            mainLoop@ for (role in event.getMember()!!.getRoles()) {
                for (roleId in roleIds) {
                    if (role.id == roleId) {
                        val accuracy =
                            if (event.getOption("меткость") != null) event.getOption("меткость")!!.getAsInt() else 0
                        val movement = if (event.getOption("передвижение") != null) event.getOption("передвижение")!!
                            .getAsInt() else 0
                        val diplomacy =
                            if (event.getOption("дипломатия") != null) event.getOption("дипломатия")!!.getAsInt() else 0
                        val trade =
                            if (event.getOption("торговля") != null) event.getOption("торговля")!!.getAsInt() else 0
                        val research = if (event.getOption("исследование") != null) event.getOption("исследование")!!
                            .getAsInt() else 0
                        val crafting =
                            if (event.getOption("создание") != null) event.getOption("создание")!!.getAsInt() else 0
                        val engineering =
                            if (event.getOption("инженерия") != null) event.getOption("инженерия")!!.getAsInt() else 0
                        val firstaid = if (event.getOption("первая_помощь") != null) event.getOption("первая_помощь")!!
                            .getAsInt() else 0
                        val survival =
                            if (event.getOption("выживание") != null) event.getOption("выживание")!!.getAsInt() else 0
                        val map: HashMap<String, Int> = dbHandler.updateCharacterSkills(
                            userid,
                            accuracy,
                            movement,
                            diplomacy,
                            trade,
                            research,
                            crafting,
                            engineering,
                            firstaid,
                            survival
                        )!!
                        event.reply("Навыки игрока " + user.globalName + " успешно обновлены").queue()
                        if (map.isNotEmpty()) {
                            val textSB = StringBuilder("Внимание, " + user.asMention + ", рост навыков!\n")
                            for (e in map.keys) {
                                when (e) {
                                    "accuracy" -> {
                                        textSB.append("Меткость повышена до " + map[e] + " уровня!\n")
                                    }
                                    "movement" -> {
                                        textSB.append("Навык передвижения повышен до " + map[e] + " уровня!\n")
                                    }
                                    "diplomacy" -> {
                                        textSB.append("Навык дипломатии повышен до " + map[e] + " уровня!\n")
                                    }
                                    "trade" -> {
                                        textSB.append("Навык торговли повышен до " + map[e] + " уровня!\n")
                                    }
                                    "research" -> {
                                        textSB.append("Навык исследования повышен до " + map[e] + " уровня!\n")
                                    }
                                    "crafting" -> {
                                        textSB.append("Навык создания повышен до " + map[e] + " уровня!\n")
                                    }
                                    "engineering" -> {
                                        textSB.append("Навык инженерии повышен до " + map[e] + " уровня!\n")
                                    }
                                    "firstaid" -> {
                                        textSB.append("Навык первой помощи повышен до " + map[e] + " уровня!\n")
                                    }
                                    "survival" -> {
                                        textSB.append("Навык выживания повышен до " + map[e] + " уровня!\n")
                                    }
                                }
                            }
                            event.getChannel().sendMessage(textSB.toString()).queue()
                        }
                        res = true
                        break@mainLoop
                    }
                }
            }
            if (!res) {
                event.reply("У вас недостаточно прав").queue()
            }
        }

        fun getNeeds(event: SlashCommandInteractionEvent) {
            val dbHandler = DBHandler()
            val eb = EmbedBuilder()
            val user: User =
                if (event.getOption("пользователь") == null) event.getUser() else event.getOption("пользователь")!!
                    .getAsUser()
            val userid = user.idLong
            val arr: Collection<String> = dbHandler.getCharacterNeeds(userid)!!
            val descSB = StringBuilder()
            for (e in arr) {
                descSB.append(e).append("\n")
            }
            val descStr = descSB.toString()
            if (descStr == "false\n") {
                event.reply("У пользователя нет профиля").queue()
            } else {
                val emsg: MessageEmbed = eb
                    .setTitle("Характеристика игрока  ${user.globalName}")
                    .setDescription(descStr)
                    .setColor(Color(18, 125, 181))
                    .build()
                val msg: MessageCreateData = MessageCreateBuilder()
                    .addEmbeds(emsg)
                    .build()
                event.reply(msg).queue()
            }
        }

        fun getSkills(event: SlashCommandInteractionEvent) {
            val dbHandler = DBHandler()
            val user: User =
                if (event.getOption("пользователь") == null) event.getUser() else event.getOption("пользователь")!!
                    .getAsUser()
            val userid = user.idLong
            val mode: Int = dbHandler.getMode(userid)
            if (mode == 1) {
                event.reply("УРААА ПЕРЕМОГА БУДЕ!!").queue()
            } else {
                val eb = EmbedBuilder()
                val arr: Collection<String> = dbHandler.getCharacterSkills(userid)!!
                val descSB = StringBuilder()
                for (e in arr) {
                    descSB.append(e).append("\n")
                }
                val descStr = descSB.toString()
                if (descStr == "false\n") {
                    event.reply("У пользователя нет профиля").queue()
                } else {
                    val emsg: MessageEmbed = eb
                        .setTitle("Навыки игрока  " + user.globalName)
                        .setDescription(descStr)
                        .setColor(Color(18, 125, 181))
                        .build()
                    val msg: MessageCreateData = MessageCreateBuilder()
                        .addEmbeds(emsg)
                        .build()
                    event.reply(msg).queue()
                }
            }
        }

        fun getHP(event: SlashCommandInteractionEvent) {
            val dbHandler = DBHandler()
            val eb = EmbedBuilder()
            val user: User =
                if (event.getOption("пользователь") == null) event.getUser() else event.getOption("пользователь")!!
                    .getAsUser()
            val userid = user.idLong
            val lvl: Int = dbHandler.getFirstAidLVL(userid)
            val map: HashMap<String, String> = dbHandler.getHP(userid, lvl)!!
            if (map.isEmpty()) {
                event.reply("У пользователя нет профиля").queue()
            } else {
                val sb = StringBuilder()
                for (e in map.keys) {
                    sb.append(e + map[e] + "\n")
                }
                val str = sb.toString()
                val emsg: MessageEmbed = eb
                    .setTitle("Здоровье игрока  " + user.globalName)
                    .setDescription(str)
                    .setColor(Color(18, 125, 181))
                    .build()
                val msg: MessageCreateData = MessageCreateBuilder()
                    .addEmbeds(emsg)
                    .build()
                event.reply(msg).queue()
            }
        }

        fun updateHP(event: SlashCommandInteractionEvent) {
            val dbHandler = DBHandler()
            val user: User = event.getUser()
            val userid = user.idLong
            val roleIds: List<String> = dbHandler.getRoleIds("gm", 1).split(", ")
            var res = false
            mainLoop@ for (role in event.getMember()!!.getRoles()) {
                for (roleId in roleIds) {
                    if (role.id == roleId) {
                        val l_arm =
                            if (event.getOption("левая_рука") != null) event.getOption("левая_рука")!!.getAsInt() else 0
                        val r_arm =
                            if (event.getOption("правая_рука") != null) event.getOption("правая_рука")!!.getAsInt() else 0
                        val torso =
                            if (event.getOption("туловище") != null) event.getOption("туловище")!!.getAsInt() else 0
                        val l_leg =
                            if (event.getOption("левая_нога") != null) event.getOption("левая_нога")!!.getAsInt() else 0
                        val r_leg =
                            if (event.getOption("правая_нога") != null) event.getOption("правая_нога")!!.getAsInt() else 0
                        val head = if (event.getOption("голова") != null) event.getOption("голова")!!.getAsInt() else 0
                        val overall = if (event.getOption("самочувствие") != null) event.getOption("самочувствие")!!
                            .getAsInt() else 0
                        dbHandler.updateHP(userid, l_arm, r_arm, torso, l_leg, r_leg, head, overall)
                        event.reply("Состояние здоровья игрока " + user.globalName + " успешно обновлено").queue()
                        res = true
                        break@mainLoop
                    }
                }
            }
            if (!res) {
                event.reply("У вас недостаточно прав").queue()
            }
        }
    }
}
