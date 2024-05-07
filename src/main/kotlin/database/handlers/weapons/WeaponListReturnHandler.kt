package database.handlers.weapons

import database.DBHandler

class WeaponListReturnHandler {
    companion object {
        private val dbHandler = DBHandler()

        private fun getListString(userId: Long): String {
            val sql = "SELECT weapons FROM users WHERE userid=?"
            dbHandler.getConnection().prepareStatement(sql).also {
                it.setLong(1, userId)
                it.executeQuery().also { rs ->
                    rs.next()
                    return rs.getString(1)
                }
            }
        }

        fun returnList(userId: Long): Array<String> {
            return getListString(userId).replace("\"", "").replace("[", "").replace("]", "").split(", ".toRegex())
                .dropLastWhile { it.isEmpty() }
                .toTypedArray()
        }
    }
}