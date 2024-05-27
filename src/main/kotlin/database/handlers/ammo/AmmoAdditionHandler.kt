package database.handlers.ammo

import database.DBHandler

class AmmoAdditionHandler {
    companion object {
        private val dbHandler = DBHandler()

        private fun fetchMinIdFromJson(jsonArray: String): Int {
            val sql = "SELECT MIN(value) FROM JSON_TABLE('$jsonArray', '$[*]' COLUMNS (value INT PATH '$')) minid;"
            dbHandler.getConnection().prepareStatement(sql).executeQuery().use { rs ->
                rs.next()
                return rs.getInt(1)
            }
        }

        private fun fetchAvailableId(): Int {
            when(checkForMissingIds()) {
                true -> {
                    val sql = "SELECT missingids FROM itemids"
                    dbHandler.getConnection().prepareStatement(sql).executeQuery().use { rs ->
                        rs.next()
                        val arr = rs.getString(1)
                        val id = fetchMinIdFromJson(arr)
                        updateMissingIds(id)
                        return id
                    }
                }
                else -> {
                    return getLastId()
                }
            }
        }

        private fun insertIntoAllItems(id: Int) {
            val sql = "INSERT INTO allitems (id, type) VALUES (?,?)"
            dbHandler.getConnection().prepareStatement(sql).also {
                it.setInt(1, id)
                it.setString(2, "патроны")
                it.executeUpdate()
            }
        }

        private fun insertAmmoItem(
            id: Int,
            name: String,
            price: Int,
            amount: Int
        ) {
            val sql = "INSERT INTO ammo (id, name, price, amount) VALUES(?,?,?,?)"
            dbHandler.getConnection().prepareStatement(sql).also {
                it.setInt(1, id)
                it.setString(2, name)
                it.setInt(3, price)
                it.setInt(4, amount)
                it.executeUpdate()
            }
            insertIntoAllItems(id)
        }

        fun handleAmmoItemAddition(
            name: String,
            price: Int,
            amount: Int
        ) {
            val id = fetchAvailableId()
            insertAmmoItem(id, name, price, amount)
        }

        private fun updateMissingIds(id: Int) {
            val index = findIndexInJson(id)
            val sql = "UPDATE itemids SET missingids = JSON_REMOVE(missingids, CONCAT(?))"
            dbHandler.getConnection().prepareStatement(sql).also {
                it.setString(1, index)
                it.executeUpdate()
            }
        }

        private fun updateLastId() {
            val sql = "UPDATE itemids SET lastid = lastid+1"
            dbHandler.getConnection().prepareStatement(sql).executeUpdate()
        }

        private fun findIndexInJson(id: Int): String {
            val sql = "SELECT JSON_SEARCH(missingids, 'one', $id) AS index_found FROM itemids"
            dbHandler.getConnection().prepareStatement(sql).executeQuery().use { rs ->
                rs.next()
                val index = rs.getString(1)
                return index.substring(1, index.length - 1)
            }
        }

        private fun checkForMissingIds(): Boolean {
            val sql = "SELECT JSON_LENGTH(missingids) FROM itemids"
            dbHandler.getConnection().prepareStatement(sql).executeQuery().use { rs ->
                rs.next()
                return rs.getInt(1) > 0
            }
        }

        private fun getLastId(): Int {
            updateLastId()
            val sql = "SELECT lastid FROM itemids"
            dbHandler.getConnection().prepareStatement(sql).executeQuery().use { rs ->
                rs.next()
                return rs.getInt(1)
            }
        }
    }
}