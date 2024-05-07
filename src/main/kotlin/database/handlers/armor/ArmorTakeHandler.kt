package database.handlers.armor

import database.DBHandler

class ArmorTakeHandler {
    companion object {
        private val dbHandler = DBHandler()

        private fun isIndexExist(armorName: String, userId: Long): String {
            val sql = "SELECT JSON_SEARCH(armor, 'one', ?) AS index_found FROM users WHERE userid=?"
            dbHandler.getConnection().prepareStatement(sql).also {
                it.setString(1, armorName)
                it.setLong(2, userId)
                it.executeQuery().also { rs ->
                    rs.next()
                    return rs.getString(1)?: "null"
                }
            }
        }

        fun takeArmor(armorName: String, userId: Long): Boolean {
            val sql = "UPDATE users SET armor = JSON_REMOVE(armor, CONCAT(?)) WHERE userid=?"
            when(val xyz = isIndexExist(armorName, userId)) {
                "null" -> {
                    return false
                }
                else -> {
                    dbHandler.getConnection().prepareStatement(sql).also {
                        it.setString(1, xyz)
                        it.setLong(2, userId)
                        it.executeUpdate()
                    }
                    return true
                }
            }
        }
    }
}