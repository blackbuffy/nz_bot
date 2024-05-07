package database.handlers.consumables

import database.DBHandler

class ConsumableAdditionHandler {
    companion object {
        private val dbHandler = DBHandler()

        private fun isIdMissing(): Boolean {
            val sql = "SELECT JSON_LENGTH(missingids) FROM itemids"
            dbHandler.getConnection().prepareStatement(sql).executeQuery().also { rs ->
                rs.next()
                return rs.getInt(1) > 0
            }
        }

        fun handleConsumableAddition(
            name: String,
            rad: Int,
            psi: Int,
            bio: Int,
            food: Int,
            thirst: Int,
            description: String
        ) {
            val conId = when(isIdMissing()) {
                false -> {
                    updateItemIDs()
                }

                else -> {
                    updateMissingIDs()
                }
            }

            addToConsumables(conId, name, rad, psi, bio, food, thirst, description)
            addToAllItems(conId)
        }

        private fun addToAllItems(id: Int) {
            val sql = "INSERT INTO allitems (id, type) VALUES (?,?)"
            dbHandler.getConnection().prepareStatement(sql).also {
                it.setInt(1, id)
                it.setString(2, "расходник")
                it.executeUpdate()
            }
        }

        private fun addToConsumables(
            conId: Int,
            name: String,
            rad: Int,
            psi: Int,
            bio: Int,
            food: Int,
            thirst: Int,
            description: String
        ) {
            val sql = """
            INSERT INTO consumables(
                conid,
                name, 
                psi, 
                bio, 
                food, 
                thirst, 
                description
            ) VALUES(?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()
            dbHandler.getConnection().prepareStatement(sql).also {
                it.setInt(1, conId)
                it.setString(2, name)
                it.setInt(3, rad)
                it.setInt(4, psi)
                it.setInt(5, bio)
                it.setInt(6, food)
                it.setInt(7, thirst)
                it.setString(8, description)
                it.executeUpdate()
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