package database

import DB_HOST
import DB_NAME
import DB_PASS
import DB_PORT
import DB_USER
import RUBLE_SYMBOL
import RU_LOCALE_MAP
import database.handlers.armor.*
import database.handlers.consumables.*
import database.handlers.weapons.*
import net.dv8tion.jda.api.entities.channel.concrete.Category
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion
import org.json.JSONObject
import org.mariadb.jdbc.MariaDbDataSource
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import javax.sql.DataSource

class DBHandler {

    private var dataSource: DataSource

    init {
        val mariaDbDataSource = MariaDbDataSource()
        mariaDbDataSource.setUrl("jdbc:mariadb://$DB_HOST:$DB_PORT/$DB_NAME")
        mariaDbDataSource.user = DB_USER
        mariaDbDataSource.setPassword(DB_PASS)

        dataSource = mariaDbDataSource
    }

    internal fun getConnection(): Connection {
        return dataSource.connection
    }

    fun signUpUser(userId: Long) {
        val sql = "INSERT INTO users (userid) VALUES (?)"

        getConnection().prepareStatement(sql).also {
            it.setString(1, userId.toString())
            it.executeUpdate()
        }
    }

    fun getBalance(userId: Long): Int? {
        val sql = "SELECT money FROM profiles WHERE userid=?"

        getConnection().prepareStatement(sql).also {
            it.setLong(1, userId)
            it.executeQuery().also { rs ->
                return if (rs.next()) rs.getInt(1) else null
            }
        }
    }

    fun changeBalance(userId: Long, value: Int) {
        val sql = "UPDATE profiles SET money=? WHERE userid=?"

        getConnection().prepareStatement(sql).also {
            it.setInt(1, value)
            it.setLong(2, userId)
            it.executeUpdate()
        }
    }

    fun getDateTime(): LocalDateTime {
        val sql = "SELECT datetime FROM date_and_time"

        getConnection().prepareStatement(sql).executeQuery().also {
            it.next()
            return it.getTimestamp(1).toLocalDateTime()
        }
    }

    fun setDateTime(dateTime: LocalDateTime) {
        val timestamp = Timestamp.from(dateTime.atZone(ZoneId.systemDefault()).toInstant())
        val sql = "UPDATE date_and_time SET datetime=?"
        getConnection().prepareStatement(sql).also {
            it.setTimestamp(1, timestamp)
            it.executeUpdate()
        }
    }

    fun getAllItems(type: String): Collection<String> {
        val sql = when (type) {
            "броня" -> {
                "SELECT name FROM armoritems"
            }
            "оружие" -> {
                "SELECT name FROM weapons"
            }
            "провизия" -> {
                "SELECT name FROM consumables"
            }
            else -> {
                null
            }
        }

        val arr: MutableCollection<String> = TreeSet<String>()

        when (sql) {
            null -> {
                arr.add("false")
            }
            else -> {
                getConnection().prepareStatement(sql).executeQuery().also { rs ->
                    if (!rs.next()) {
                        arr.add("empty")
                    } else {
                        arr.add(rs.getString(1))
                        while (!rs.isLast) {
                            rs.next()
                            arr.add(rs.getString(1))
                        }
                    }
                }
            }
        }

        return arr
    }

    fun getItemId(type: String, name: String): Int? {
        val sql = when (type) {
            "броня" -> {
                "SELECT itemid FROM armoritems WHERE name=?"
            }
            "оружие" -> {
                "SELECT weaponid FROM weapons WHERE name=?"
            }
            "провизия" -> {
                "SELECT conid FROM consumables WHERE name=?"
            }
            else -> {
                null
            }
        }

        var i: Int?

        when (sql) {
            null -> {
                i = null
            }
            else -> {
                getConnection().prepareStatement(sql).also {
                    it.setString(1, name)
                    it.executeQuery().also { rs ->
                        rs.next()
                        i = rs.getInt(1)
                    }
                }
            }
        }

        return i
    }

    fun getItemInfo(id: Int): Collection<String> {
        val typeMap = mapOf(
            "броня" to Pair("SELECT * FROM armor WHERE itemid=?",
                listOf("name", "rank", "price", "termo", "electro", "chemical", "radio", "psi", "absorption", "armor", "containers")),
            "оружие" to Pair("SELECT * FROM weapons WHERE weaponid=?",
                listOf("name", "fire_rate", "accuracy", "range", "flatness", "recoil", "ammo", "weight", "ammo_type", "price", "rank", "type")),
            "провизия" to Pair("SELECT * FROM consumables WHERE conid=?",
                listOf("name", "rad", "psi", "bio", "food", "thirst", "description"))
        )

        // Получение типа предмета
        val sql1 = "SELECT type FROM allitems WHERE id=?"
        val preparedStatement1 = getConnection().prepareStatement(sql1).also { it.setInt(1, id) }
        val resultSet1 = preparedStatement1.executeQuery().also { it.next() }
        val type = resultSet1.getString(1)

        // Присвоение значения пары
        val (sql2, fields) = typeMap[type] ?: throw IllegalArgumentException("Неверный тип элемента")

        // Запрос на получение предмета
        val preparedStatement2 = getConnection().prepareStatement(sql2).also { it.setInt(1, id) }
        val resultSet2 = preparedStatement2.executeQuery()

        // Проверка на существование предмета и наполнение списка данными
        val finalReturnArray = mutableListOf<String>()
        if (resultSet2.next()) {
            fields.forEach { field ->
                val value = if (field == "price") {
                    "${resultSet2.getString(field)}$RUBLE_SYMBOL"
                } else {
                    resultSet2.getString(field)
                }
                finalReturnArray.add("${RU_LOCALE_MAP[field]}: $value")
            }
        }

        return finalReturnArray
    }

    fun addArmorItem(
        name: String,
        rank: String,
        price: Int,
        termo: Int,
        electro: Int,
        chemical: Int,
        radio: Int,
        psi: Int,
        absorption: Int,
        armor: Int,
        containers: Int
    ) {
        ArmorAdditionHandler.handleArmorItemAddition(name, price, termo, electro, chemical, radio, psi, absorption, armor, containers, rank)
    }

    fun removeArmorItem(id: Int) {
        ArmorRemovalHandler.handleArmorItemRemoval(id)
    }

    fun giveArmorItem(userid: Long, armorName: String): Boolean {
        return ArmorGiveHandler.giveArmor(armorName, userid)
    }

    fun takeArmorItem(userid: Long, armorName: String): Boolean {
        return ArmorTakeHandler.takeArmor(armorName, userid)
    }

    fun getUserArmor(userid: Long): Array<String> {
        return ArmorListReturnHandler.returnList(userid)
    }

    fun insertModalData(userid: Long, name: String, biography: String, personality: String, history: String) {
        val sql = "INSERT INTO profiles (userid, name, biography, personality, history) VALUES (?,?,?,?,?)"

        getConnection().prepareStatement(sql).also {
            it.setLong(1, userid)
            it.setString(2, name)
            it.setString(3, biography)
            it.setString(4, personality)
            it.setString(5, history)
            it.executeUpdate()
        }
    }

    fun getRoleIds(type: String, limit: Int): String {
        val sql = "SELECT roleid FROM adminroles WHERE $type<=? AND NOT $type=0"
        var result = ""
        try {
            val prSt = getConnection().prepareStatement(sql)
            prSt.setInt(1, limit)
            val rs = prSt.executeQuery()
            rs.next()
            val sb = StringBuilder(rs.getString(1))
            while (rs.next()) {
                sb.append(", " + rs.getString(1))
            }
            result = sb.toString()
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return result
    }

    fun getRoleId(name: String?): Long {
        var rs: ResultSet? = null
        var res = 0L
        val sql = "SELECT roleid FROM adminroles WHERE rolename=?"
        try {
            val prSt = getConnection().prepareStatement(sql)
            prSt.setString(1, name)
            rs = prSt.executeQuery()
            rs.next()
            res = rs.getLong(1)
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return res
    }

    fun createTicket(name: String, creatorid: Long, panel: String, channelid: Long) {
        val sql = "INSERT INTO tickets (ticketname, creatorid, panel, channelid) VALUES (?, ?, ?, ?)"
        try {
            val prSt = getConnection().prepareStatement(sql)
            prSt.setString(1, name)
            prSt.setLong(2, creatorid)
            prSt.setString(3, panel)
            prSt.setLong(4, channelid)
            prSt.executeUpdate()
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    fun getTicketName(channelid: Long): String? {
        var rs: ResultSet? = null
        var res: String? = ""
        val sql = "SELECT ticketname FROM tickets WHERE channelid=?"
        try {
            val prSt = getConnection().prepareStatement(sql)
            prSt.setLong(1, channelid)
            rs = prSt.executeQuery()
            rs.next()
            res = rs.getString(1)
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return res
    }

    fun setAdminId(adminId: Long, name: String?) {
        val sql = "UPDATE tickets SET adminid=? WHERE ticketname=?"
        try {
            val prSt = getConnection().prepareStatement(sql)
            prSt.setLong(1, adminId)
            prSt.setString(2, name)
            prSt.executeUpdate()
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    fun getAdminId(name: String?): Long {
        var rs: ResultSet? = null
        val sql = "SELECT adminid FROM tickets WHERE ticketname=?"
        var res: Long = 0
        try {
            val prSt = getConnection().prepareStatement(sql)
            prSt.setString(1, name)
            rs = prSt.executeQuery()
            rs.next()
            res = rs.getLong(1)
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return res
    }

    fun closeTicket(name: String?, channel: MessageChannelUnion, category: Category?) {
        val sql = "UPDATE tickets SET closed=1 WHERE ticketname=?"
        try {
            val prSt = getConnection().prepareStatement(sql)
            prSt.setString(1, name)
            prSt.executeUpdate()
            channel.asGuildMessageChannel().manager.setName("closed-$name").queue()
            channel.asTextChannel().manager.setParent(category).queue()
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    fun openTicket(name: String?, channel: MessageChannelUnion, category: Category?) {
        val sql = "UPDATE tickets SET closed=0 WHERE ticketname=?"
        try {
            val prSt = getConnection().prepareStatement(sql)
            prSt.setString(1, name)
            prSt.executeUpdate()
            channel.asGuildMessageChannel().manager.setName("ticket-$name").queue()
            channel.asTextChannel().manager.setParent(category).queue()
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    fun deleteTicket(channel: MessageChannelUnion) {
        val sql = "DELETE FROM tickets WHERE channelid=?"
        try {
            val prSt = getConnection().prepareStatement(sql)
            prSt.setLong(1, channel.idLong)
            prSt.executeUpdate()
            channel.delete().queue()
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    fun getUserWeapon(userid: Long): Array<String> {
        return WeaponListReturnHandler.returnList(userid)
    }

    fun addWeapon(
        type: String,
        rank: String,
        name: String,
        fire_rate: Int,
        accuracy: Double,
        range: Int,
        flatness: Int,
        recoil: Double,
        ammo: Int,
        weight: Double,
        ammo_type: String,
        price: Int
    ) {
        WeaponAdditionHandler.handleWeaponAddition(type, rank, name, fire_rate, accuracy, range, flatness, recoil, ammo, weight, ammo_type, price)
    }

    fun removeWeapon(id: Int) {
        WeaponRemovalHandler.handleWeaponRemoval(id)
    }

    fun giveWeapon(userid: Long, name: String): Boolean {
        return WeaponGiveHandler.handleWeaponGive(userid, name)
    }

    fun takeWeapon(userid: Long, name: String): Boolean {
        return WeaponTakeHandler.handleWeaponTake(name, userid)
    }

    fun addConsumable(name: String, rad: Int, psi: Int, bio: Int, food: Int, thirst: Int, description: String) {
        ConsumableAdditionHandler.handleConsumableAddition(name, rad, psi, bio, food, thirst, description)
    }

    fun removeConsumable(id: Int) {
        ConsumableRemovalHandler.handleConsumableRemoval(id)
    }

    fun giveConsumable(userid: Long, name: String): Boolean {
        return ConsumableGiveHandler.handleConsumableGive(userid, name)
    }

    fun takeConsumable(userid: Long, name: String): Boolean {
        return ConsumableTakeHandler.handleConsumableTake(name, userid)
    }

    fun getUserConsumables(userid: Long): Array<String> {
        return ConsumableListReturnHandler.returnList(userid)
    }

    fun getCreatorId(name: String?): Long {
        var rs: ResultSet? = null
        val sql = "SELECT creatorid FROM tickets WHERE ticketname=?"
        var id: Long = 0
        try {
            val prSt = getConnection().prepareStatement(sql)
            prSt.setString(1, name)
            rs = prSt.executeQuery()
            rs.next()
            id = rs.getLong(1)
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return id
    }

    fun createProfile(userid: Long, age: Int, moniker: String, fraction: String) {
        val sql = "UPDATE profiles SET age=?, moniker=?, fraction=?, `rank`=?, reputation=? WHERE userid=?"
        getConnection().prepareStatement(sql).also {
            it.setInt(1, age)
            it.setString(2, moniker)
            it.setString(3, fraction)
            it.setString(4, "Зеленый")
            it.setString(5, "Нейтральная")
            it.setLong(6, userid)
            it.executeUpdate()
        }
    }

    fun getProfile(userid: Long): Collection<String> {
        val sql = "SELECT * FROM profiles WHERE userid=?"
        getConnection().prepareStatement(sql).use {
            it.setLong(1, userid)
            it.executeQuery().also { rs ->
                return if (!rs.next()) {
                    listOf("false")
                } else {
                    listOf("""
                        **Имя:** ${rs.getString(2)},
                        **Возраст:** ${rs.getInt(3)},
                        **Прозвище:** ${rs.getString(4)},
                        **Ранг:** ${rs.getString(9)},
                        **Репутация:** ${rs.getString(10)},
                        **Фракция:** ${rs.getString(5)},
                        **Биография:** ${rs.getString(6)},
                        **Характер:** ${rs.getString(7)},
                        **История:** ${rs.getString(8)}
                    """.trimIndent())
                }
            }
        }
    }

    fun addRankExp(userid: Long, xp: Int): String? {
        var rs: ResultSet? = null
        val sqlUpdateExp = "UPDATE profiles SET rank_xp=rank_xp+? WHERE userid=?"
        val sqlSelectExp = "SELECT rank_xp FROM profiles WHERE userid=?"
        val sqlUpdateRank = "UPDATE profiles SET profiles.rank=? WHERE userid=?"
        val sqlSelectRank = "SELECT profiles.rank FROM profiles WHERE userid=?"

        val rankThresholds = listOf(
            5 to "Зеленый",
            15 to "Новичек",
            50 to "Неопытный",
            85 to "Бывалый",
            120 to "Профессионал",
            200 to "Ветеран",
            500 to "Мастер"
        )

        var answer: String? = null
        val prSt = getConnection().prepareStatement(sqlUpdateExp)
        prSt.setInt(1, xp)
        prSt.setLong(2, userid)
        prSt.executeUpdate()

        val prSt2 = getConnection().prepareStatement(sqlSelectExp)
        prSt2.setLong(1, userid)
        rs = prSt2.executeQuery()
        rs.next()
        val x = rs.getInt(1)

        val prSt4 = getConnection().prepareStatement(sqlSelectRank)
        prSt4.setLong(1, userid)
        rs = prSt4.executeQuery()
        rs.next()
        val currentRank = rs.getString(1)

        val newRank = rankThresholds.lastOrNull { x >= it.first }?.second ?: "Элита"
        val prSt3 = getConnection().prepareStatement(sqlUpdateRank)
        prSt3.setString(1, newRank)
        prSt3.setLong(2, userid)
        if (currentRank != newRank) {
            prSt3.executeUpdate()
            answer = newRank
        }

        return answer
    }

    fun addRepExp(userid: Long, xp: Int): String? {
        val reputationThreshold = listOf(
            -700 to "Ужасная",
            -350 to "Очень плохая",
            -100 to "Плохая",
            101 to "Нейтральная",
            451 to "Хорошая",
            801 to "Очень хорошая",
            1401 to "Отличная"
        )

        var rs: ResultSet? = null
        val sqlUpdateReputationXP = "UPDATE profiles SET reputation_xp=reputation_xp+? WHERE userid=?"
        val sqlSelectReputationXP = "SELECT reputation_xp FROM profiles WHERE userid=?"
        val sqlUpdateReputation = "UPDATE profiles SET reputation=? WHERE userid=?"
        val sqlSelectReputation = "SELECT reputation FROM profiles WHERE userid=?"

        var answer: String? = null

        val prSt = getConnection().prepareStatement(sqlUpdateReputationXP)
        prSt.setInt(1, xp)
        prSt.setLong(2, userid)
        prSt.executeUpdate()

        val prSt2 = getConnection().prepareStatement(sqlSelectReputationXP)
        prSt2.setLong(1, userid)
        rs = prSt2.executeQuery()
        rs.next()
        val newXP = rs.getInt(1)

        val prSt4 = getConnection().prepareStatement(sqlSelectReputation)
        prSt4.setLong(1, userid)
        rs = prSt4.executeQuery()
        rs.next()
        val currentReputation = rs.getString(1)

        val prSt3 = getConnection().prepareStatement(sqlUpdateReputation)
        prSt3.setLong(2, userid)

        val newReputation = reputationThreshold.firstOrNull { newXP < it.first }?.second ?: "Великолепная"
        if (currentReputation != newReputation) {
            prSt3.setString(1, newReputation)
            prSt3.executeUpdate()
            answer = newReputation
        }

        return answer
    }

    fun getExp(userid: Long): Collection<String> {
        var rs: ResultSet? = null
        val sql = "SELECT rank_xp, reputation_xp FROM profiles WHERE userid=?"
        val arr: MutableCollection<String> = ArrayList()
        try {
            val prSt = getConnection().prepareStatement(sql)
            prSt.setLong(1, userid)
            rs = prSt.executeQuery()
            if (rs.next()) {
                arr.add("**Rank XP:** " + rs.getInt(1))
                arr.add("**Rep XP:** " + rs.getInt(2))
            } else {
                arr.add("false")
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return arr
    }

    fun updateCharacterNeeds(
        userid: Long,
        stamina: Int,
        thirst: Int,
        hunger: Int,
        hp: Int,
        leftArm: Int,
        rightArm: Int,
        leftLeg: Int,
        rightLeg: Int,
        torso: Int,
        head: Int,
        overall: Int
    ) {
        var rs: ResultSet? = null
        val sql1 = "SELECT stats FROM profiles WHERE userid=?"
        val sql2 = "UPDATE profiles SET stats=? WHERE userid=?"
        try {
            val prSt1 = getConnection().prepareStatement(sql1)
            val prSt2 = getConnection().prepareStatement(sql2)
            prSt1.setLong(1, userid)
            rs = prSt1.executeQuery()
            rs.next()
            val jsonString = rs.getString(1)
            val json = JSONObject(jsonString)
            if (stamina != 0) {
                var value: Int = json.getInt("stamina")
                value += stamina
                json.put("stamina", value)
            }
            if (thirst != 0) {
                var value: Int = json.getInt("thirst")
                value += thirst
                json.put("thirst", value)
            }
            if (hunger != 0) {
                var value: Int = json.getInt("hunger")
                value += hunger
                json.put("hunger", value)
            }
            if (hp != 0) {
                var value: Int = json.getInt("hp")
                value += hp
                json.put("hp", value)
            }
            if (leftArm != 0) {
                var value: Int = json.getInt("l_arm")
                value += leftArm
                json.put("l_arm", value)
            }
            if (rightArm != 0) {
                var value: Int = json.getInt("r_arm")
                value += rightArm
                json.put("r_arm", value)
            }
            if (torso != 0) {
                var value: Int = json.getInt("torso")
                value += torso
                json.put("torso", value)
            }
            if (leftLeg != 0) {
                var value: Int = json.getInt("l_leg")
                value += leftLeg
                json.put("l_leg", value)
            }
            if (rightLeg != 0) {
                var value: Int = json.getInt("r_leg")
                value += rightLeg
                json.put("r_leg", value)
            }
            if (head != 0) {
                var value: Int = json.getInt("head")
                value += head
                json.put("head", value)
            }
            if (overall != 0) {
                var value: Int = json.getInt("overall")
                value += overall
                json.put("overall", value)
            }
            val updatedStr: String = json.toString()
            prSt2.setString(1, updatedStr)
            prSt2.setLong(2, userid)
            prSt2.executeUpdate()
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    fun getCharacterNeeds(userid: Long): Collection<String>? {
        var rs: ResultSet? = null
        val needs: MutableCollection<String> = ArrayList()
        val sql = "SELECT stats FROM profiles WHERE userid=?"
        try {
            val prSt = getConnection().prepareStatement(sql)
            prSt.setLong(1, userid)
            rs = prSt.executeQuery()
            if (!rs.next()) {
                needs.add("false")
            } else {
                val jsonString = rs.getString(1)
                val json = JSONObject(jsonString)
                needs.add("**Голод**: " + json.get("hunger") + "\n")
                needs.add("**Жажда**: " + json.get("thirst") + "\n")
                needs.add("**Выносливость**: " + json.get("stamina") + "\n")
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return needs
    }

    fun setMode(userid: Long, mode: Int) {
        val sql = "UPDATE users SET command_mode=? WHERE userid=?"
        try {
            val prSt = getConnection().prepareStatement(sql)
            prSt.setInt(1, mode)
            prSt.setLong(2, userid)
            prSt.executeUpdate()
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    fun getMode(userid: Long): Int {
        var rs: ResultSet? = null
        val sql = "SELECT command_mode FROM users WHERE userid=?"
        var mode = 0
        try {
            val prSt = getConnection().prepareStatement(sql)
            prSt.setLong(1, userid)
            rs = prSt.executeQuery()
            rs.next()
            mode = rs.getInt(1)
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return mode
    }

    fun getCharacterSkills(userid: Long): Collection<String>? {
        var rs: ResultSet? = null
        val skills: MutableCollection<String> = ArrayList()
        val sql = "SELECT skills,skills_xp FROM profiles WHERE userid=?"
        try {
            val prSt = getConnection().prepareStatement(sql)
            prSt.setLong(1, userid)
            rs = prSt.executeQuery()
            if (!rs.next()) {
                skills.add("false")
            } else {
                val jsonString1 = rs.getString(1)
                val json1 = JSONObject(jsonString1)
                val jsonString2 = rs.getString(2)
                val json2 = JSONObject(jsonString2)
                skills.add("**Уровень навыка выживания**: " + json1.get("survival_lvl") + " (" + json2.get("survival") + " XP)\n")
                skills.add("**Уровень меткости**: " + json1.get("accuracy_lvl") + " (" + json2.get("accuracy") + " XP)\n")
                skills.add("**Уровень навыка передвижения**: " + json1.get("movement_lvl") + " (" + json2.get("movement") + " XP)\n")
                skills.add("**Уровень навыка дипломатии**: " + json1.get("diplomacy_lvl") + " (" + json2.get("diplomacy") + " XP)\n")
                skills.add("**Уровень навыка торговли**: " + json1.get("trade_lvl") + " (" + json2.get("trade") + " XP)\n")
                skills.add("**Уровень навыка исследования**: " + json1.get("research_lvl") + " (" + json2.get("research") + " XP)\n")
                skills.add("**Уровень навыка создания**: " + json1.get("crafting_lvl") + " (" + json2.get("crafting") + " XP)\n")
                skills.add("**Уровень навыка инженерии**: " + json1.get("engineering_lvl") + " (" + json2.get("engineering") + " XP)\n")
                skills.add("**Уровень навыка первой помощи**: " + json1.get("firstaid_lvl") + " (" + json2.get("firstaid") + " XP)\n")
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return skills
    }

    private fun updateSkillLevel(jsonXP: JSONObject, jsonLVL: JSONObject, skill: String, increment: Int, thresholds: List<Pair<Int, Int>>): Int {
        var level = jsonLVL.getInt("${skill}_lvl")
        var xp = jsonXP.getInt(skill) + increment
        jsonXP.put(skill, xp)

        val newLevel = thresholds.lastOrNull { xp >= it.first }?.second ?: level
        if (newLevel != level) {
            jsonLVL.put("${skill}_lvl", newLevel)
        }
        return newLevel
    }

    fun updateCharacterSkills(
        userid: Long,
        accuracy: Int,
        movement: Int,
        diplomacy: Int,
        trade: Int,
        research: Int,
        crafting: Int,
        engineering: Int,
        firstaid: Int,
        survival: Int
    ): HashMap<String, Int>? {
        val map = HashMap<String, Int>()
        val thresholds = listOf(
            25 to 1,
            50 to 2,
            100 to 3,
            250 to 4,
            450 to 5
        )

        val sql = "SELECT skills_xp, skills FROM profiles WHERE userid=?"
        val sql2 = "UPDATE profiles SET skills_xp=?, skills=? WHERE userid=?"
        getConnection().prepareStatement(sql).use { prSt ->
            prSt.setLong(1, userid)
            prSt.executeQuery().use { rs ->
                rs.next()
                val jsonXP = JSONObject(rs.getString(1))
                val jsonLVL = JSONObject(rs.getString(2))

                val skills = listOf("accuracy", "movement", "diplomacy", "trade", "research", "crafting", "engineering", "firstaid", "survival")
                val increments = listOf(accuracy, movement, diplomacy, trade, research, crafting, engineering, firstaid, survival)

                skills.zip(increments).forEach { (skill, increment) ->
                    val newLevel = updateSkillLevel(jsonXP, jsonLVL, skill, increment, thresholds)
                    if (jsonLVL.getInt("${skill}_lvl") != newLevel) {
                        map[skill] = newLevel
                    }
                }

                val prSt2 = getConnection().prepareStatement(sql2)
                prSt2.setString(1, jsonXP.toString())
                prSt2.setString(2, jsonLVL.toString())
                prSt2.setLong(3, userid)
                prSt2.executeUpdate()
            }
        }
        return map
    }

    fun getFirstAidLVL(userid: Long): Int {
        var rs: ResultSet? = null
        var firstaid = 0
        val sql = "SELECT skills FROM profiles WHERE userid=?"
        try {
            val prSt = getConnection().prepareStatement(sql)
            prSt.setLong(1, userid)
            rs = prSt.executeQuery()
            rs.next()
            val jsonString = rs.getString(1)
            val json = JSONObject(jsonString)
            firstaid = json.getInt("firstaid_lvl")
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return firstaid
    }

    private fun getStateDescription(value: Int, thresholds: List<IntRange>): String {
        return when {
            value == 0 -> "Катастрофическое"
            value in thresholds[0] -> "Ужасное"
            value in thresholds[1] -> "Плохое"
            value in thresholds[2] -> "Нормальное"
            else -> "Хорошее"
        }
    }

    private fun getPartName(part: String): String {
        return when (part) {
            "l_arm" -> "левой руки"
            "r_arm" -> "правой руки"
            "torso" -> "туловища"
            "l_leg" -> "левой ноги"
            "r_leg" -> "правой ноги"
            "head" -> "головы"
            else -> ""
        }
    }

    fun getHP(userid: Long, lvl: Int): HashMap<String, String> {
        val map = HashMap<String, String>()
        val sql = "SELECT stats FROM profiles WHERE userid=?"
        getConnection().prepareStatement(sql).use { prSt ->
            prSt.setLong(1, userid)
            prSt.executeQuery().use { rs ->
                rs.next()
                val json = JSONObject(rs.getString(1))
                map["Общее состояние: "] = json.getInt("hp").toString()
                map["Самочувствие: "] = json.getInt("overall").toString()

                if (lvl > 0) {
                    val thresholds = if (lvl <= 2) listOf(1..3, 4..7) else listOf(1..2, 3..5, 6..8)
                    listOf("l_arm", "r_arm", "torso", "l_leg", "r_leg", "head").forEach { part ->
                        val value = json.getInt(part)
                        map["Состояние ${getPartName(part)}: "] = getStateDescription(value, thresholds)
                    }
                }
            }
        }
        return map
    }

    fun updateHP(userid: Long, l_arm: Int, r_arm: Int, torso: Int, l_leg: Int, r_leg: Int, head: Int, overall: Int) {
        val rs: ResultSet
        val sql = "SELECT stats FROM profiles WHERE userid=?"
        val sql2 = "UPDATE profiles SET stats=? WHERE userid=?"
        val prSt = getConnection().prepareStatement(sql)
        prSt.setLong(1, userid)
        rs = prSt.executeQuery()
        rs.next()
        val jsonString = rs.getString(1)
        val json = JSONObject(jsonString)

        val updates = mapOf(
            "l_arm" to l_arm,
            "r_arm" to r_arm,
            "torso" to torso,
            "l_leg" to l_leg,
            "r_leg" to r_leg,
            "head" to head,
            "overall" to overall
        )

        updates.forEach { (key, value) ->
            if (json.has(key) && value != 0) {
                json.put(key, json.getInt(key) + value)
            }
        }

        val prSt2 = getConnection().prepareStatement(sql2)
        prSt2.setString(1, json.toString())
        prSt2.setLong(2, userid)
        prSt2.executeUpdate()
    }
}