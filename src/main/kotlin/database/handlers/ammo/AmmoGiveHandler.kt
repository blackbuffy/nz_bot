package database.handlers.ammo

import database.DBHandler

class AmmoGiveHandler {
    companion object {
        private val dbHandler = DBHandler()

        private fun isAmmoExist(armorName: String): Boolean {
            val sqlCheck = "SELECT id FROM ammo WHERE name=?"
            dbHandler.getConnection().prepareStatement(sqlCheck).also {
                it.setString(1, armorName)
                return it.executeQuery().next()
            }
        }

        fun giveAmmo(name: String, userId: Long): Boolean {
            when(isAmmoExist(name)) {
                true -> {
                    val sqlAppend = "UPDATE users SET ammo=(JSON_ARRAY_APPEND(ammo, '$', ?)) WHERE userid=?"
                    dbHandler.getConnection().prepareStatement(sqlAppend).also {
                        it.setString(1, name)
                        it.setLong(2, userId)
                        it.executeUpdate()
                        return true
                    }
                }
                else -> {
                    return false
                }
            }
        }
    }
}