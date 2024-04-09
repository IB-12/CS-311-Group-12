package ch.epfl.cs311.wanderwave.model.remote

import ch.epfl.cs311.wanderwave.model.data.Track
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class TrackConnection : FirebaseConnection<Track, Track> {

  // THe goal is to have the Id of the firebase document to match the id of the spotify track

  override val collectionName: String = "tracks"

  override val getItemId = { track: Track -> track.id }

  private val db = FirebaseFirestore.getInstance()

  // Document to Track
  override fun documentToItem(document: DocumentSnapshot): Track? {
    return Track.from(document)
  }

  override fun itemToHash(track: Track): HashMap<String, Any> {
    return track.toHashMao()
  }

  fun addList(tracks: List<Track>) {
    tracks.forEach { track -> addItemWithId(track) }
  }

  fun addItemsIfNotExist(tracks: List<Track>) {
    // The goal of this function is to add only if the spotify id of the track is not already in the
    // database, for now I just check the normal ID
    tracks.forEach { track ->
      db.collection(collectionName).whereEqualTo("id", track.id).get().addOnSuccessListener {
          documentSnapshot ->
        if (documentSnapshot.isEmpty) {
          addItemWithId(track)
        }
      }
    }
  }
}