package database.handlers.armor

import database.DBHandler

class ArmorAdditionHandler {
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
                it.setString(2, "броня")
                it.executeUpdate()
            }
        }

        private fun insertArmorItem(
            id: Int,
            name: String,
            price: Int,
            termo: Int,
            electro: Int,
            chemical: Int,
            radio: Int,
            psi: Int,
            absorption: Int,
            armor: Int,
            containers: Int,
            rank: String
        ) {
            val sql = "INSERT INTO armor (itemid, name, price, termo, electro, chemical, radio, psi, absorption, armor, containers, `rank`) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)"
            dbHandler.getConnection().prepareStatement(sql).also {
                it.setInt(1, id)
                it.setString(2, name)
                it.setInt(3, price)
                it.setInt(4, termo)
                it.setInt(5, electro)
                it.setInt(6, chemical)
                it.setInt(7, radio)
                it.setInt(8, psi)
                it.setInt(9, absorption)
                it.setInt(10, armor)
                it.setInt(11, containers)
                it.setString(12, rank)
                it.executeUpdate()
            }
            insertIntoAllItems(id)
        }

        fun handleArmorItemAddition(
            name: String,
            price: Int,
            termo: Int,
            electro: Int,
            chemical: Int,
            radio: Int,
            psi: Int,
            absorption: Int,
            armor: Int,
            containers: Int,
            rank: String
        ) {
            val id = fetchAvailableId()
            insertArmorItem(id, name, price, termo, electro, chemical, radio, psi, absorption, armor, containers, rank)
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