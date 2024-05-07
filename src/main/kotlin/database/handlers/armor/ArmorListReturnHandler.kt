package database.handlers.armor

import database.DBHandler

class ArmorListReturnHandler {
    companion object {
        private val dbHandler = DBHandler()

        fun returnList(userId: Long): Array<String> {
            return getListString(userId).replace("\"", "").replace("[", "").replace("]", "").split(", ".toRegex())
                .dropLastWhile { it.isEmpty() }
                .toTypedArray()
        }

        fun getListString(userId: Long): String {
            val sql = "SELECT armor FROM users WHERE userid=?"

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