package db.map

import db.Database
import db.DatabaseTable
import org.example.db.map.MapTableDao
import org.example.db.map.PlayingField
import java.sql.ResultSet

class MapDatabaseTable(db: Database?) : DatabaseTable(db), MapTableDao {

    companion object {
        private const val TABLE_NAME = "MAP_PLAYING_FIELDS"

        private const val KEY_ID = "ID"
        private const val KEY_NAME = "NAME"
        private const val KEY_STREET = "STREET"
        private const val KEY_POSTAL_CODE = "POSTAL_CODE"
        private const val KEY_CITY = "CITY"
        private const val KEY_LAT = "GEO_LAT"
        private const val KEY_LNG = "GEO_LNG"
        private const val KEY_ERROR = "ERROR"
        private const val KEY_ORIGIN_LINE = "ORIGIN_LINE"
    }

    override fun create() {
        val SQL = """
            CREATE TABLE IF NOT EXISTS $TABLE_NAME (
                $KEY_ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                $KEY_NAME CHAR(50) NOT NULL, 
                $KEY_STREET CHAR(50),
                $KEY_POSTAL_CODE CHAR(50),
                $KEY_CITY CHAR(50), 
                $KEY_LAT REAL,
                $KEY_LNG REAL,
                $KEY_ERROR CHAR(50),
                $KEY_ORIGIN_LINE CHAR(200)
            )
        """.trimIndent()

        try {
            val stmt = db.connection.createStatement()
            stmt.executeUpdate(SQL)
            stmt.close()
            println("Table created successfully")
        } catch (e: Exception) {
            System.err.println(e.message)
            System.exit(0)
        }
    }

    override fun insertField(field: PlayingField) {
        val SQL = """
           INSERT INTO $TABLE_NAME ( 
                $KEY_NAME,  
                $KEY_STREET,  
                $KEY_POSTAL_CODE,  
                $KEY_CITY,  
                $KEY_LAT,  
                $KEY_LNG,
                $KEY_ERROR,
                $KEY_ORIGIN_LINE
           ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()

        try {
            val stmt = db.connection.prepareStatement(SQL)
            stmt.setString(1, field.name)
            stmt.setString(2, field.street)
            stmt.setString(3, field.postalCode)
            stmt.setString(4, field.city)
            stmt.setDouble(5, field.geoLat)
            stmt.setDouble(6, field.geoLng)
            stmt.setString(7, field.error)
            stmt.setString(8, field.originLine)
            stmt.execute()
            stmt.close()
            println("Inserted '${field}' map record.")
        } catch (e: Exception) {
            System.err.println(e.message)
            System.exit(0)
        }
    }

    fun fetchPlayingField(rs: ResultSet): PlayingField {
        val f = PlayingField(
            rs.getString(KEY_NAME),
            rs.getString(KEY_STREET),
            rs.getString(KEY_POSTAL_CODE),
            rs.getString(KEY_CITY),
            rs.getDouble(KEY_LAT),
            rs.getDouble(KEY_LNG),
            rs.getString(KEY_ERROR),
            rs.getString(KEY_ORIGIN_LINE),
        )
        f.id = rs.getInt(KEY_ID)
        return f
    }

    override fun getAll(): ArrayList<PlayingField> {
        val fields = ArrayList<PlayingField>()
        val SQL = """
           SELECT * FROM $TABLE_NAME
        """.trimIndent()

        try {
            val stmt = db.connection.createStatement()
            val rs  = stmt.executeQuery(SQL)
            while (rs.next()) {
                fields += fetchPlayingField(rs)
            }
            stmt.close()
        } catch (e: Exception) {
            System.err.println(e.message)
            System.exit(0)
        }
        return fields
    }

    override fun getAllErrored(): ArrayList<PlayingField> {
        val fields = ArrayList<PlayingField>()
        val SQL = """
           SELECT * FROM $TABLE_NAME WHERE $KEY_ERROR != ""
        """.trimIndent()

        try {
            val stmt = db.connection.createStatement()
            val rs  = stmt.executeQuery(SQL)
            while (rs.next()) {
                fields += fetchPlayingField(rs)
            }
            stmt.close()
        } catch (e: Exception) {
            System.err.println(e.message)
            System.exit(0)
        }
        return fields
    }
}