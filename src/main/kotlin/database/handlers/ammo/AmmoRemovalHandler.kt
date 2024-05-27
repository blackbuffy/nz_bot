package database.handlers.ammo

import database.DBHandler

class AmmoRemovalHandler {
    companion object {
        private val dbHandler = DBHandler()

        fun handleAmmoItemRemoval(id: Int) {
            appendMissingId(id)
            removeFromItems(id)
        }

        private fun appendMissingId(id: Int) {
            val sql = "UPDATE itemids SET missingids=(JSON_ARRAY_APPEND(missingids, '$', ?))"
            dbHandler.getConnection().prepareStatement(sql).also {
                it.setString(1, id.toString())
                it.executeUpdate()
            }
        }

        private fun removeFromItems(id: Int) {
            val sql = "DELETE FROM ammo WHERE id=?"
            val sql2 = "DELETE FROM allitems WHERE id=?"
            dbHandler.getConnection().prepareStatement(sql).also {
                it.setInt(1, id)
                it.executeUpdate()
            }
            dbHandler.getConnection().prepareStatement(sql2).also {
                it.setInt(1, id)
                it.executeUpdate()
            }
        }
    }
}