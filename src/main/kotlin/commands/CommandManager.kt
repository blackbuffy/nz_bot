package commands

import RUBLE_SYMBOL
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.guild.GuildReadyEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.*

class CommandManager : ListenerAdapter() {
    private val commands: MutableMap<String, Command> = HashMap()
    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        val cmd = commands[event.name]
        cmd?.execute(event)
    }

    override fun onGuildReady(event: GuildReadyEvent) {
        commands["очистить"] = ClearCommand()
        commands["бан"] = BanCommand()
        commands["опыт"] = ExpCommand()
        commands["инвентарь"] = InventoryCommand()
        commands["предмет"] = ItemCommand()
        commands["кик"] = KickCommand()
        commands["мут"] = MuteCommand()
        commands["профиль"] = ProfileCommand()
        commands["время"] = TimeCommand()
        commands["разбан"] = UnbanCommand()
        commands["размут"] = UnmuteCommand()
        commands["режим"] = ModeCommand()
        commands["баланс"] = BalanceCommand()
        commands["бонус"] = BonusCommand()

        val guildCommandList: MutableList<CommandData> = ArrayList()

        guildCommandList.add(createClearCommand())
        guildCommandList.add(createMuteCommand())
        guildCommandList.add(createUnmuteCommand())
        guildCommandList.add(createTimeCommand())
        guildCommandList.add(createKickCommand())
        guildCommandList.add(createBanCommand())
        guildCommandList.add(createUnbanCommand())
        guildCommandList.add(createBonusCommand())
        // guildCommandList.add(Commands.slash("sendmsgtest", "test"));
        guildCommandList.add(createInventoryCommand())
        guildCommandList.add(createItemCommand())
        // guildCommandList.add(Commands.slash("authall", "adm").setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)));
        guildCommandList.add(createProfileCommand())
        guildCommandList.add(createExpCommand())
        guildCommandList.add(createModeCommand())
        guildCommandList.add(createBalanceCommand())
        event.guild.updateCommands().addCommands(guildCommandList).queue()
    }

    private fun createBonusCommand(): SlashCommandData {
        val createOptions = arrayOf(
            OptionData(OptionType.INTEGER, "день", "День бонуса", true),
            OptionData(OptionType.NUMBER, "модификатор", "Модификатор XP", false),
            OptionData(OptionType.INTEGER, "деньги", "$RUBLE_SYMBOL", false)
        )

        val subcommands = arrayOf(
            SubcommandData("забрать", "Забрать бонус"),
            SubcommandData("создать", "Создать бонус")
                .addOptions(*createOptions)
        )

        return Commands.slash("бонус", "Команды связанные с бонусами")
            .addSubcommands(*subcommands)
    }

    private fun createBalanceCommand(): SlashCommandData {
        val getOptions = arrayOf(
            OptionData(OptionType.USER, "пользователь", "Пользователь, чей баланс нужно посмотреть", false)
        )
        val changeOptions = arrayOf(
            OptionData(OptionType.USER, "пользователь", "Пользователь, чей баланс нужно изменить", true),
            OptionData(OptionType.INTEGER, "значение", "Значение на которое нужно изменить", true)
        )

        val subcommands = arrayOf(
            SubcommandData("посмотреть", "Посмотреть баланс пользователя")
                .addOptions(*getOptions),
            SubcommandData("изменить", "Изменить баланс пользователя")
                .addOptions(*changeOptions)
        )
        return Commands.slash("баланс", "Команды связанные с балансом")
            .addSubcommands(*subcommands)
    }

    private fun createModeCommand(): SlashCommandData {
        val setOptions = arrayOf(
            OptionData(OptionType.STRING, "режим", "Режим", true)
        )
        return Commands.slash("режим", "Режим использования команд")
            .addOptions(*setOptions)
    }

    private fun createClearCommand(): SlashCommandData {
        val clearOptions = arrayOf(
            OptionData(OptionType.INTEGER, "количество", "Количество сообщений", true)
        )
        return Commands.slash("очистить", "Очистить сообщения").addOptions(*clearOptions)
            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL))
    }

    private fun createTimeCommand(): SlashCommandData {
        val timeSetOptions = arrayOf(
            OptionData(OptionType.INTEGER, "день", "День который нужно установить", true),
            OptionData(OptionType.INTEGER, "месяц", "Месяц который нужно установить", true),
            OptionData(OptionType.INTEGER, "год", "Год который нужно установить", true),
            OptionData(OptionType.INTEGER, "час", "Час который нужно установить", true),
            OptionData(OptionType.INTEGER, "минут", "Сколько минут нужно установить", true),
            OptionData(OptionType.INTEGER, "секунд", "Сколько секунд нужно установить", true)
        )
        val timeSubcommands = arrayOf(
            SubcommandData("установить", "Установить время")
                .addOptions(*timeSetOptions),
            SubcommandData("узнать", "Вернуть время"),
            SubcommandData("пауза", "Временно остановить время"),
            SubcommandData("возобновить", "Запустить время")
        )
        return Commands.slash("время", "Команды связанные со временем")
            .addSubcommands(*timeSubcommands)
            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
    }

    private fun createKickCommand(): SlashCommandData {
        val kickOptions = arrayOf(
            OptionData(OptionType.USER, "пользователь", "Пользователь которого нужно выгнать", true),
            OptionData(OptionType.STRING, "причина", "Причина по которой пользователя выгнали", true)
        )
        return Commands.slash("кик", "Выгнать участника с сервера")
            .addOptions(*kickOptions)
            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.KICK_MEMBERS))
    }

    private fun createBanCommand(): SlashCommandData {
        val banOptions = arrayOf(
            OptionData(OptionType.USER, "пользователь", "Пользователь которого нужно заблокировать", true),
            OptionData(OptionType.STRING, "причина", "Причина по которой пользователя заблокировали", true),
            OptionData(OptionType.INTEGER, "время", "Время блокировки необязательно", false)
        )
        return Commands.slash("бан", "Заблокировать участника")
            .addOptions(*banOptions).setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.BAN_MEMBERS))
    }

    private fun createUnbanCommand(): SlashCommandData {
        val unbanOptions = arrayOf(
            OptionData(OptionType.USER, "пользователь", "Пользователь которого нужно разблокировать", true),
            OptionData(OptionType.STRING, "причина", "Причина по которой пользователя разблокировали", true)
        )
        return Commands.slash("разбан", "Разблокировать участника")
            .addOptions(*unbanOptions)
            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.BAN_MEMBERS))
    }

    private fun createUnmuteCommand(): SlashCommandData {
        val unmuteOptions = arrayOf(
            OptionData(OptionType.USER, "пользователь", "Пользователь которому нужно вернуть доступ", true),
            OptionData(OptionType.STRING, "причина", "Причина снятия наказания", true)
        )
        return Commands.slash("размут", "Вернуть участнику доступ к отправке сообщений и голосовым чатам")
            .addOptions(unmuteOptions[0], unmuteOptions[1])
            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS))
    }

    private fun createMuteCommand(): SlashCommandData {
        val muteOptions = arrayOf(
            OptionData(OptionType.USER, "пользователь", "Пользователь которому нужно ограничить доступ", true),
            OptionData(OptionType.INTEGER, "секунд", "Длительность наказания в секундах", true),
            OptionData(OptionType.STRING, "причина", "Причина наказания", true)
        )
        return Commands.slash("мут", "Запретить участнику отправлять сообщения и говорить на время")
            .addOptions(muteOptions[0], muteOptions[1], muteOptions[2])
            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS))
    }

    private fun createExpCommand(): SlashCommandData {
        val expGetSCOptions = arrayOf(
            OptionData(OptionType.USER, "пользователь", "Пользователь, EXP которого нужно посмотреть", false)
        )
        val expGiveSCOptions = arrayOf(
            OptionData(OptionType.USER, "пользователь", "Пользователь, кому выдать XP", true),
            OptionData(OptionType.BOOLEAN, "модификатор", "Использовать ли модификатор", true),
            OptionData(OptionType.INTEGER, "ранг", "Количество Rank XP", false),
            OptionData(OptionType.INTEGER, "репутация", "Количество Rep XP", false)
        )
        val expSubcommands = arrayOf(
            SubcommandData("посмотреть", "Посмотреть XP пользователя")
                .addOptions(*expGetSCOptions),
            SubcommandData("выдать", "Выдать XP пользователю")
                .addOptions(*expGiveSCOptions)
        )

        val modificatorGetSCOptions = arrayOf(
            OptionData(OptionType.BOOLEAN, "активные", "Какие модификаторы посмотреть", true)
        )
        val modificatorSubcommands = arrayOf(
            SubcommandData("посмотреть", "Посмотреть модификатор")
                .addOptions(*modificatorGetSCOptions),
            SubcommandData("использовать", "Использовать неактивные модификаторы")
        )
        val modificatorGroup = arrayOf(
            SubcommandGroupData("модификатор", "Команды модификаторов опыта")
                .addSubcommands(*modificatorSubcommands)
        )

        return Commands.slash("опыт", "Запретить участнику отправлять сообщения и говорить на время")
            .addSubcommands(*expSubcommands)
            .addSubcommandGroups(*modificatorGroup)
    }

    private fun createProfileCommand(): SlashCommandData {
        val profileGetSCOptions = arrayOf(
            OptionData(OptionType.USER, "пользователь", "Пользователь, чей профиль нужно посмотреть", false)
        )
        val profileCreateSCOptions = arrayOf(
            OptionData(OptionType.USER, "пользователь", "Пользователь, которому нужно создать профиль", true),
            OptionData(OptionType.STRING, "имя", "Имя персонажа", true),
            OptionData(OptionType.INTEGER, "возраст", "Возраст персонажа", true),
            OptionData(OptionType.STRING, "прозвище", "Кличка/позывной персонажа", true),
            OptionData(OptionType.STRING, "фракция", "Фракция в которой состоит персонаж", true)
        )
        val profileUpdateNeedsSCOptions = arrayOf(
            OptionData(OptionType.INTEGER, "выносливость", "Значение выносливости", false),
            OptionData(OptionType.INTEGER, "жажда", "Значение жажды", false),
            OptionData(OptionType.INTEGER, "голод", "Значение голода", false),
            OptionData(OptionType.INTEGER, "общее_здоровье", "Значение общего здоровья", false),
            OptionData(OptionType.INTEGER, "здоровье_л_руки", "Значение здоровья левой руки", false),
            OptionData(OptionType.INTEGER, "здоровье_п_руки", "Значение здоровья правой руки", false),
            OptionData(OptionType.INTEGER, "здоровье_л_ноги", "Значение здоровья левой ноги", false),
            OptionData(OptionType.INTEGER, "здоровье_п_ноги", "Значение здоровья правой ноги", false),
            OptionData(OptionType.INTEGER, "здоровье_торса", "Значение здоровья торса", false),
            OptionData(OptionType.INTEGER, "здоровье_головы", "Значение здоровья головы", false),
            OptionData(OptionType.INTEGER, "самочувствие", "Значение самочувствия", false)
        )
        val profileGetNeedsSCOptions = arrayOf(
            OptionData(OptionType.USER, "пользователь", "Пользователь, чьи нужды нужно просмотреть", false)
        )
        val profileGetSkillsSCOptions = arrayOf(
            OptionData(OptionType.USER, "пользователь", "Пользователь, чьи навыки нужно просмотреть", false)
        )
        val profileUpdateSkillsSCOptions = arrayOf(
            OptionData(OptionType.USER, "пользователь", "Пользователь, чьи навыки нужно обновить", true),
            OptionData(OptionType.INTEGER, "меткость", "Количетсво опыта, которое нужно выдать пользователю", false),
            OptionData(
                OptionType.INTEGER,
                "передвижение",
                "Количетсво опыта, которое нужно выдать пользователю",
                false
            ),
            OptionData(OptionType.INTEGER, "дипломатия", "Количетсво опыта, которое нужно выдать пользователю", false),
            OptionData(OptionType.INTEGER, "торговля", "Количетсво опыта, которое нужно выдать пользователю", false),
            OptionData(
                OptionType.INTEGER,
                "исследование",
                "Количетсво опыта, которое нужно выдать пользователю",
                false
            ),
            OptionData(OptionType.INTEGER, "создание", "Количетсво опыта, которое нужно выдать пользователю", false),
            OptionData(OptionType.INTEGER, "инженерия", "Количетсво опыта, которое нужно выдать пользователю", false),
            OptionData(
                OptionType.INTEGER,
                "первая_помощь",
                "Количетсво опыта, которое нужно выдать пользователю",
                false
            ),
            OptionData(OptionType.INTEGER, "выживание", "Количетсво опыта, которое нужно выдать пользователю", false)
        )
        val profileGetHpSCOptions = arrayOf(
            OptionData(OptionType.USER, "пользователь", "Пользователь, чье состояние здоровья нужно посмотреть", false)
        )
        val profileUpdateHpSCOptions = arrayOf(
            OptionData(OptionType.USER, "пользователь", "Пользователь, чье состояние здоровья нужно обновить", true),
            OptionData(OptionType.INTEGER, "левая_рука", "Часть здоровья которую нужно обновить", false),
            OptionData(OptionType.INTEGER, "правая_рука", "Часть здоровья которую нужно обновить", false),
            OptionData(OptionType.INTEGER, "туловище", "Часть здоровья которую нужно обновить", false),
            OptionData(OptionType.INTEGER, "левая_нога", "Часть здоровья которую нужно обновить", false),
            OptionData(OptionType.INTEGER, "правая_нога", "Часть здоровья которую нужно обновить", false),
            OptionData(OptionType.INTEGER, "голова", "Часть здоровья которую нужно обновить", false),
            OptionData(OptionType.INTEGER, "самочувствие", "Часть здоровья которую нужно обновить", false)
        )
        val profileSubcommands = arrayOf(
            SubcommandData("посмотреть", "Посмотреть профиль")
                .addOptions(*profileGetSCOptions),
            SubcommandData("создать", "Создать профиль игроку")
                .addOptions(*profileCreateSCOptions),
            SubcommandData("обновить_нужды", "Обновить нужды игрока")
                .addOptions(*profileUpdateNeedsSCOptions),
            SubcommandData("нужды", "Узнать нужды персонажа")
                .addOptions(*profileGetNeedsSCOptions),
            SubcommandData("навыки", "Узнать навыки персонажа")
                .addOptions(*profileGetSkillsSCOptions),
            SubcommandData("обновить_навыки", "Обновить навыки игрока")
                .addOptions(*profileUpdateSkillsSCOptions),
            SubcommandData("здоровье", "Посмотреть здоровье")
                .addOptions(*profileGetHpSCOptions),
            SubcommandData("обновить_здоровье", "Обновить состояние здоровья")
                .addOptions(*profileUpdateHpSCOptions)
        )
        return Commands.slash("профиль", "Команды связанные с профилями")
            .addSubcommands(*profileSubcommands)
    }

    private fun createItemCommand(): SlashCommandData {
        val itemGetInfoSCOptions = arrayOf(
            OptionData(OptionType.INTEGER, "айди", "Айди предмета о котором нужно получить информацию", true)
        )
        val itemGetAllSCOptions = arrayOf(
            OptionData(OptionType.STRING, "тип", "Тип предметов", true)
        )
        val itemAddConsumableSCOptions = arrayOf(
            OptionData(OptionType.STRING, "название", "Название предмета", true),
            OptionData(OptionType.INTEGER, "сытость", "Сколько предмет восполняет сытости", true),
            OptionData(OptionType.INTEGER, "жажда", "Сколько предмет восполняет жажды", true),
            OptionData(OptionType.STRING, "описание", "Описание предмета и его доп. свойств", true),
            OptionData(OptionType.INTEGER, "радиация", "Сколько предмет выводит радиации", true),
            OptionData(OptionType.INTEGER, "пси", "Сколько предмет выводит пси-излучения", true),
            OptionData(OptionType.INTEGER, "био", "Сколько предмет выводит био-заражения", true)
        )
        val itemRemoveConsumableSCOptions = arrayOf(
            OptionData(OptionType.INTEGER, "айди", "Айди предмета которого нужно удалить", true)
        )
        val itemAddWeaponSCOptions = arrayOf(
            OptionData(OptionType.STRING, "название", "Название оружие", true),
            OptionData(OptionType.INTEGER, "темп_огня", "Темп огня оружия", true),
            OptionData(OptionType.NUMBER, "точность", "Точность оружия", true),
            OptionData(OptionType.INTEGER, "дальность", "Дальность стрельбы оружия", true),
            OptionData(OptionType.INTEGER, "настильность", "Настильность стрельбы оружия", true),
            OptionData(OptionType.NUMBER, "отдача", "Отдача от стрельбы оружия", true),
            OptionData(OptionType.INTEGER, "боезапас", "Боезапас магазина", true),
            OptionData(OptionType.NUMBER, "вес", "Вес оружия", true),
            OptionData(OptionType.STRING, "тип_патрон", "Патрон, используемый в оружии", true),
            OptionData(OptionType.INTEGER, "цена", "Цена за оружие", true),
            OptionData(OptionType.STRING, "ранг", "Ранг, требуемый для оружия", true),
            OptionData(OptionType.STRING, "тип", "Тип оружия", true)
        )
        val itemRemoveWeaponSCOptions = arrayOf(
            OptionData(OptionType.INTEGER, "айди_предмета", "Айди предмета", true)
        )
        val itemAddArmorSCOptions = arrayOf(
            OptionData(OptionType.STRING, "название_предмета", "Название предмета", true),
            OptionData(OptionType.INTEGER, "цена", "Цена предмета", true),
            OptionData(OptionType.INTEGER, "термозащита", "Термозащита предмета", true),
            OptionData(OptionType.INTEGER, "электрозащита", "Электрозащита предмета", true),
            OptionData(OptionType.INTEGER, "химзащита", "Химическая защита предмета", true),
            OptionData(OptionType.INTEGER, "радиозащита", "Защита от радиации предмета", true),
            OptionData(OptionType.INTEGER, "псизащита", "Защита от пси излучения предмета", true),
            OptionData(OptionType.INTEGER, "гашение", "Гашение удара предмета", true),
            OptionData(OptionType.INTEGER, "броня", "Броня предмета", true),
            OptionData(OptionType.INTEGER, "контейнеры", "Кол-во контейнеров предмета", true),
            OptionData(OptionType.STRING, "ранг", "Ранг брони", true)
        )
        val itemRemoveArmorSCOptions = arrayOf(
            OptionData(OptionType.INTEGER, "айди_предмета", "Айди предмета", true)
        )
        val itemSubcommands = arrayOf(
            SubcommandData("добавить_броню", "Создать броню")
                .addOptions(*itemAddArmorSCOptions),
            SubcommandData("удалить_броню", "Удалить броню")
                .addOptions(*itemRemoveArmorSCOptions),
            SubcommandData("добавить_оружие", "Создать оружие")
                .addOptions(*itemAddWeaponSCOptions),
            SubcommandData("удалить_оружие", "Удалить оружие")
                .addOptions(*itemRemoveWeaponSCOptions),
            SubcommandData("добавить_провизию", "Создать съедобный предмет")
                .addOptions(*itemAddConsumableSCOptions),
            SubcommandData("удалить_провизию", "Удалить съедобный предмет")
                .addOptions(*itemRemoveConsumableSCOptions),
            SubcommandData("все", "Увидеть все предметы определенного типа")
                .addOptions(*itemGetAllSCOptions),
            SubcommandData("узнать_о", "Узнать информацию о предмете")
                .addOptions(*itemGetInfoSCOptions)
        )
        return Commands.slash("предмет", "Команды связанные с предметами")
            .addSubcommands(*itemSubcommands)
    }

    private fun createInventoryCommand(): SlashCommandData {
        val invGiveArmorSCOptions = arrayOf(
            OptionData(OptionType.USER, "пользователь", "Пользователь которому нужно выдать предмет", true),
            OptionData(OptionType.STRING, "название_предмета", "Название предмета которого нужно выдать", true)
        )
        val invTakeArmorSCOptions = arrayOf(
            OptionData(OptionType.USER, "пользователь", "Пользователь у которого нужно забрать предмет", true),
            OptionData(OptionType.STRING, "название_предмета", "Название предмета которого нужно выдать", true)
        )
        val invGetArmorSCOptions = arrayOf(
            OptionData(OptionType.USER, "пользователь", "Пользователь, у которого нужно просмотреть инвентарь", false)
        )
        val invGiveWeaponSCOptions = arrayOf(
            OptionData(OptionType.USER, "пользователь", "Пользователь которому нужно выдать оружие", true),
            OptionData(OptionType.STRING, "название_предмета", "Название предмета которого нужно выдать", true)
        )
        val invTakeWeaponSCOptions = arrayOf(
            OptionData(OptionType.USER, "пользователь", "Пользователь у которого нужно забрать оружие", true),
            OptionData(OptionType.STRING, "название", "Название оружия которого нужно забрать у участника", true)
        )
        val invGiveConsumableSCOptions = arrayOf(
            OptionData(OptionType.USER, "пользователь", "Пользователь которому нужно выдать предмет", true),
            OptionData(OptionType.STRING, "название", "Название предмета которого нужно выдать", true)
        )
        val invTakeConsumableSCOptions = arrayOf(
            OptionData(OptionType.USER, "пользователь", "Пользователь у которого нужно забрать предмет", true),
            OptionData(OptionType.STRING, "название", "Название предмета которого нужно забрать", true)
        )
        val invSubcommands = arrayOf(
            SubcommandData("выдать_броню", "Выдать броню участнику")
                .addOptions(*invGiveArmorSCOptions),
            SubcommandData("забрать_броню", "Забрать броню у участника")
                .addOptions(*invTakeArmorSCOptions),
            SubcommandData("посмотреть", "Вернуть список предметов в инвентаре игрока")
                .addOptions(*invGetArmorSCOptions),
            SubcommandData("выдать_оружие", "Выдать оружие участнику")
                .addOptions(*invGiveWeaponSCOptions),
            SubcommandData("забрать_оружие", "Забрать оружие у участника")
                .addOptions(*invTakeWeaponSCOptions),
            SubcommandData("выдать_провизию", "Выдать съедобный предмет участнику")
                .addOptions(*invGiveConsumableSCOptions),
            SubcommandData("забрать_провизию", "Забрать съедобный предмет у участника")
                .addOptions(*invTakeConsumableSCOptions)
        )
        return Commands.slash("инвентарь", "Команды связанные с инвентарем")
            .addSubcommands(*invSubcommands)
    } /* public static void authAll(SlashCommandInteractionEvent event) {
        DBHandler dbHandler = new DBHandler();
        List<Member> members = event.getGuild().getMembers();
        for (Member member : members) {
            long userid = member.getIdLong();
            dbHandler.signUpUser(userid);
        }
        event.reply("Z").queue();
    }

    public static void sendMsgTest(SlashCommandInteractionEvent event) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Тикет для создания персонажа");
        embedBuilder.setDescription("Будущий сталкер, тебе предстоит создать своего персонажа, который выживет в странном и жестком мире Зоны. Ты можешь стать смельчаком-бойцом, заядлым торговцем, опытным наемником или коварным бандитом. Твоя судьба — твой выбор.\nВ тикете вы можете задать **любые** вопросы администрации, приятной игры!");
        embedBuilder.setColor(new Color(0, 193, 241));

        Button createCharacterTicketButton = Button.success("create-character-button", "Создать тикет");

        MessageCreateData ticketMsg = new MessageCreateBuilder()
                .addEmbeds(embedBuilder.build())
                .setComponents(ActionRow.of(createCharacterTicketButton))
                .build();

        event.getGuild().getTextChannelById(1054428854904705124L).sendMessage(ticketMsg).queue();
    } */
}