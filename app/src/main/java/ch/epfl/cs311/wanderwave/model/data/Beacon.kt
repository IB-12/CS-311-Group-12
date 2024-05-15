package ch.epfl.cs311.wanderwave.model.data

import com.google.firebase.firestore.DocumentSnapshot

data class Beacon(

    /** GUID of the beacon */
    val id: String,

    /** Location of the beacon */
    val location: Location,

    /** List of tracks that are broadcast from the beacon */
    val profileAndTrack: List<ProfileTrackAssociation> = listOf<ProfileTrackAssociation>(),
) {

  companion object {
    fun from(document: DocumentSnapshot): Beacon? {
      return if (document.exists()) {
        val id = document.id
        val locationMap = document["location"] as? Map<String, Any> ?: null
        val latitude = locationMap?.get("latitude") as? Double ?: 0.0
        val longitude = locationMap?.get("longitude") as? Double ?: 0.0
        val name = locationMap?.get("name") as? String ?: ""
        val location = Location(latitude, longitude, name)

        val profileAndTrack = listOf<ProfileTrackAssociation>()

        Beacon(id = id, location = location, profileAndTrack = profileAndTrack)
      } else {
        null
      }
    }
  }
}
