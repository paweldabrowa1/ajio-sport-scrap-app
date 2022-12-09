package org.example.db.map

interface MapTableDao {

    fun insertField(field: PlayingField)

    fun getAll(): ArrayList<PlayingField>

    fun getAllErrored(): ArrayList<PlayingField>
}