package database.handlers.consumables

import database.DBHandler

class ConsumableGiveHandler {
    companion object {
        private val dbHandler = DBHandler()

        fun handleConsumableGive(userId: Long, name: String): Boolean {
            when(isConsumableExist(name)) {
                true -> {
                    giveConsumable(userId, name)
                    return true
                }
                else -> {
                    return false
                }
            }
        }

        private fun isConsumableExist(name: String): Boolean {
            val sql = "SELECT conid FROM consumables WHERE name=?"

            dbHandler.getConnection().prepareStatement(sql).also {
                it.setString(1, name)
                it.executeQuery().also { rs ->
                    return rs.next()
                }
            }
        }

        private fun giveConsumable(userId: Long, name: String) {
            val sql = "UPDATE users SET consumables=(JSON_ARRAY_APPEND(consumables, '$', ?)) WHERE userid=?"

            dbHandler.getConnection().prepareStatement(sql).also {
                it.setString(1, name)
                it.setLong(2, userId)
                it.executeUpdate()
            }
        }
    }
}