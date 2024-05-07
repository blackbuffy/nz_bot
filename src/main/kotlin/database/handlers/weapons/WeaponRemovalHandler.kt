package database.handlers.weapons

import database.DBHandler

class WeaponRemovalHandler {
    companion object {
        private val dbHandler = DBHandler()

        fun handleWeaponRemoval(id: Int) {
            deleteFromTables(id)
            updateMissingIDs(id)
        }

        private fun deleteFromTables(id: Int) {
            val sql = """
                DELETE FROM weapons WHERE weaponid=?;
                DELETE FROM allitems WHERE id=?;
            """.trimIndent()

            dbHandler.getConnection().prepareStatement(sql).also {
                it.setInt(1, id)
                it.setInt(2, id)
                it.executeUpdate()
            }
        }

        private fun updateMissingIDs(id: Int) {
            val sql = "UPDATE itemids SET missingids=(JSON_ARRAY_APPEND(missingids, '$', ?))"

            dbHandler.getConnection().prepareStatement(sql).also {
                it.setString(1, id.toString())
                it.executeUpdate()
            }
        }
    }
}