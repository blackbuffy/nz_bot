package database.handlers.consumables

import database.DBHandler

class ConsumableTakeHandler {
    companion object {
        private val dbHandler = DBHandler()

        private fun isIndexExist(name: String, userId: Long): String {
            val sql = "SELECT JSON_SEARCH(consumables, 'one', ?) AS index_found FROM users WHERE userid=?"
            dbHandler.getConnection().prepareStatement(sql).also {
                it.setString(1, name)
                it.setLong(2, userId)
                it.executeQuery().also { rs ->
                    rs.next()
                    return rs.getString(1)?: "null"
                }
            }
        }

        private fun takeConsumable(index: String, userId: Long) {
            val sql = "UPDATE users SET consumables = JSON_REMOVE(consumables, CONCAT(?)) WHERE userid=?"

            dbHandler.getConnection().prepareStatement(sql).also {
                it.setString(1, index)
                it.setLong(2, userId)
                it.executeUpdate()
            }
        }

        fun handleConsumableTake(name: String, userId: Long): Boolean {
            when (val index = isIndexExist(name, userId)) {
                "null" -> {
                    return false
                }
                else -> {
                    takeConsumable(index, userId)
                    return true
                }
            }
        }
    }
}