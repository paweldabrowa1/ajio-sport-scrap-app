package org.example.db.map

data class PlayingField (
    var name: String,
    var street: String?,
    var postalCode: String?,
    var city: String?,
    var geoLat: Double,
    var geoLng: Double,
    var error: String?,
    var originLine: String,
) {
    var id: Int = -1

    override fun toString(): String {
        val e = if (error != null && error?.length!! > 0) { "*" } else { "" }
        return "${e}ID($id):\t\"$name\" $city($postalCode) [$street]"
    }
}