package database.handlers.ammo

import database.DBHandler

class AmmoListReturnHandler {
    companion object {
        private val dbHandler = DBHandler()

        fun returnList(userId: Long): Array<String> {
            return if (getListString(userId) == "[]") arrayOf("") else getListString(userId).replace("\"", "").replace("[", "").replace("]", "").split(", ".toRegex())
                .dropLastWhile { it.isEmpty() }
                .toTypedArray()
        }

        fun getListString(userId: Long): String {
            val sql = "SELECT ammo FROM users WHERE userid=?"

            dbHandler.getConnection().prepareStatement(sql).also {
                it.setLong(1, userId)
                it.executeQuery().also { rs ->
                    rs.next()
                    return rs.getString(1)
                }
            }
        }
    }
}