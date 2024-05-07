package database.handlers.armor

import database.DBHandler

class ArmorGiveHandler {
    companion object {
        private val dbHandler = DBHandler()

        private fun isArmorExist(armorName: String): Boolean {
            val sqlCheck = "SELECT itemid FROM armor WHERE name=?"
            dbHandler.getConnection().prepareStatement(sqlCheck).also {
                it.setString(1, armorName)
                return it.executeQuery().next()
            }
        }

        fun giveArmor(armorName: String, userId: Long): Boolean {
            when(isArmorExist(armorName)) {
                true -> {
                    val sqlAppend = "UPDATE users SET armor=(JSON_ARRAY_APPEND(armor, '$', ?)) WHERE userid=?"
                    dbHandler.getConnection().prepareStatement(sqlAppend).also {
                        it.setString(1, armorName)
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