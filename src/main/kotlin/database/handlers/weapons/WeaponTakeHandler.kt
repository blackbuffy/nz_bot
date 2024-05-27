package database.handlers.weapons

import database.DBHandler

class WeaponTakeHandler {
    companion object {
        private val dbHandler = DBHandler()

        private fun isIndexExist(name: String, userId: Long): String {
            val sql = "SELECT JSON_SEARCH(weapons, 'one', ?) AS index_found FROM users WHERE userid=?"
            dbHandler.getConnection().prepareStatement(sql).also {
                it.setString(1, name)
                it.setLong(2, userId)
                it.executeQuery().also { rs ->
                    rs.next()
                    return rs.getString(1)?.substring(1..rs.getString(1).length - 2)?: "null"
                }
            }
        }

        private fun takeWeapon(index: String, userId: Long) {
            val sql = "UPDATE users SET weapons = JSON_REMOVE(weapons, ?) WHERE userid=?"

            dbHandler.getConnection().prepareStatement(sql).also {
                it.setString(1, index)
                it.setLong(2, userId)
                it.executeUpdate()
            }
        }

        fun handleWeaponTake(name: String, userId: Long): Boolean {
            when (val index = isIndexExist(name, userId)) {
                "null" -> {
                    return false
                }
                else -> {
                    takeWeapon(index, userId)
                    return true
                }
            }
        }
    }
}