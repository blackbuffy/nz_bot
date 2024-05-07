package database.handlers.weapons

import database.DBHandler

class WeaponAdditionHandler {
    companion object {
        private val dbHandler = DBHandler()

        private fun isIdMissing(): Boolean {
            val sql = "SELECT JSON_LENGTH(missingids) FROM itemids"
            dbHandler.getConnection().prepareStatement(sql).executeQuery().also { rs ->
                rs.next()
                return rs.getInt(1) > 0
            }
        }

        fun handleWeaponAddition(
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
            val id = when(isIdMissing()) {
                false -> {
                    updateItemIDs()
                }

                else -> {
                    updateMissingIDs()
                }
            }

            addToWeapons(type, rank, name, fire_rate, accuracy, range, flatness, recoil, ammo, weight, ammo_type, price, id)
            addToAllItems(id)
        }

        private fun addToAllItems(id: Int) {
            val sql = "INSERT INTO allitems (id, type) VALUES (?,?)"
            dbHandler.getConnection().prepareStatement(sql).also {
                it.setInt(1, id)
                it.setString(2, "оружие")
                it.executeUpdate()
            }
        }

        private fun addToWeapons(
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
            price: Int,
            id: Int
        ) {
            val sql = """
            INSERT INTO weapons(
                weapons.NAME,
                weapons.fire_rate,
                weapons.accuracy,
                weapons.`range`,
                weapons.flatness,
                weapons.recoil,
                weapons.ammo,
                weapons.weight,
                weapons.ammo_type,
                weapons.price,
                weapons.weaponid,
                weapons.rank,
                weapons.type
            ) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()
            dbHandler.getConnection().prepareStatement(sql).also { prSt ->
                prSt.setString(1, name)
                prSt.setInt(2, fire_rate)
                prSt.setDouble(3, accuracy)
                prSt.setInt(4, range)
                prSt.setInt(5, flatness)
                prSt.setDouble(6, recoil)
                prSt.setInt(7, ammo)
                prSt.setDouble(8, weight)
                prSt.setString(9, ammo_type)
                prSt.setInt(10, price)
                prSt.setInt(11, id)
                prSt.setString(12, rank)
                prSt.setString(13, type)
                prSt.executeUpdate()
            }
        }

        private fun updateItemIDs(): Int {
            val sql = """
                UPDATE itemids SET lastid=lastid+1;
                SELECT lastid FROM itemids
            """.trimIndent()
            dbHandler.getConnection().createStatement().also {
                it.execute(sql)
                it.resultSet.also { rs ->
                    rs.next()
                    return rs.getInt(1)
                }
            }
        }

        private fun updateMissingIDs(): Int {
            val sql = """
                SELECT MIN(value) AS id FROM JSON_TABLE((SELECT missingids FROM itemids), '${'$'}[*]' COLUMNS(value INT PATH '${'$'}'));
                WITH UpdatedIDs AS (
                    SELECT JSON_REMOVE((SELECT missingids FROM itemids), CONCAT('$[', (SELECT JSON_SEARCH(nz_db.itemids.missingids, 'one', (SELECT id FROM SelectedID))), ']')) AS newids
                );
                UPDATE itemids SET missingids = (SELECT newids FROM UpdatedIDs);
            """.trimIndent()
            dbHandler.getConnection().createStatement().also {
                it.execute(sql)
                it.resultSet.also { rs ->
                    rs.next()
                    return rs.getInt(1)
                }
            }
        }
    }
}