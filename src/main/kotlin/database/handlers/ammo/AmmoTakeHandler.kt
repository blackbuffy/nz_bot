package database.handlers.ammo

import database.DBHandler

class AmmoTakeHandler {
    companion object {
        private val dbHandler = DBHandler()

        private fun isIndexExist(name: String, userId: Long): String {
            val sql = "SELECT JSON_SEARCH(ammo, 'one', ?) AS index_found FROM users WHERE userid=?"
            dbHandler.getConnection().prepareStatement(sql).also {
                it.setString(1, name)
                it.setLong(2, userId)
                it.executeQuery().also { rs ->
                    rs.next()
                    return rs.getString(1)?.substring(1..rs.getString(1).length - 2)?: "null"
                }
            }
        }

        fun takeAmmo(name: String, userId: Long): Boolean {
            val sql = "UPDATE users SET ammo = JSON_REMOVE(ammo, ?) WHERE userid=?"
            when(val xyz = isIndexExist(name, userId)) {
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