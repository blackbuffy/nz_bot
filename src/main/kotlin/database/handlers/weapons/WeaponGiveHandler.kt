package database.handlers.weapons

import database.DBHandler

class WeaponGiveHandler {
    companion object {
        private val dbHandler = DBHandler()

        fun handleWeaponGive(userId: Long, name: String): Boolean {
            when(isWeaponExist(name)) {
                true -> {
                    giveWeapon(userId, name)
                    return true
                }
                else -> {
                    return false
                }
            }
        }

        private fun isWeaponExist(name: String): Boolean {
            val sql = "SELECT weaponid FROM weapons WHERE name=?"

            dbHandler.getConnection().prepareStatement(sql).also {
                it.setString(1, name)
                it.executeQuery().also { rs ->
                    return rs.next()
                }
            }
        }

        private fun giveWeapon(userId: Long, name: String) {
            val sql = "UPDATE users SET weapons=(JSON_ARRAY_APPEND(weapons, '$', ?)) WHERE userid=?"

            dbHandler.getConnection().prepareStatement(sql).also {
                it.setString(1, name)
                it.setLong(2, userId)
                it.executeUpdate()
            }
        }
    }
}