package ch.epfl.cs311.wanderwave.model

import android.net.Uri
import ch.epfl.cs311.wanderwave.model.data.Profile
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.remote.ProfileConnection
import ch.epfl.cs311.wanderwave.model.remote.TrackConnection
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/*
data class Profile(
    var firstName: String,
    var lastName: String,
    var description: String,
    var numberOfLikes: Int,
    var isPublic: Boolean,
    var profilePictureUri: Uri? = null,
    var spotifyUid: String,
    var firebaseUid: String,
    var topSongs: List<Track> = emptyList(),
    var chosenSongs: List<Track> = emptyList(),
)
 */

public class ProfileConnectionTest {

  @get:Rule val mockkRule = MockKRule(this)

  @RelaxedMockK private lateinit var documentSnapshot: DocumentSnapshot
  private lateinit var profileConnection: ProfileConnection

  @RelaxedMockK private lateinit var firebaseFirestore: FirebaseFirestore
  @RelaxedMockK private lateinit var querySnapshot: QuerySnapshot

  @Before
  fun setup() {
    MockKAnnotations.init(this)
    val trackConnection = TrackConnection()
    profileConnection = ProfileConnection(firebaseFirestore, trackConnection = trackConnection)
  }

  @Test
  fun testDocumentToItem() {
    // Mock document snapshot data
    val firstName = "John"
    val lastName = "Doe"
    val description = "Test description"
    val numberOfLikes = 5
    val isPublic = true
    val spotifyUid = "spotifyUid"
    val firebaseUid = "firebaseUid"

    // Mock the behavior of Document
    every { documentSnapshot.exists() } returns true
    every { documentSnapshot.getString("firstName") } returns firstName
    every { documentSnapshot.getString("lastName") } returns lastName
    every { documentSnapshot.getString("description") } returns description
    every { documentSnapshot.getLong("numberOfLikes") } returns numberOfLikes.toLong()
    every { documentSnapshot.getBoolean("isPublic") } returns isPublic
    every { documentSnapshot.getString("spotifyUid") } returns spotifyUid
    every { documentSnapshot.getString("firebaseUid") } returns firebaseUid
    every { documentSnapshot.getString("profilePictureUri") } returns null

    // Call the function under test
    val result = profileConnection.documentToItem(documentSnapshot)

    // Assert the result
    val expectedProfile =
        Profile(
            firstName = firstName,
            lastName = lastName,
            description = description,
            numberOfLikes = numberOfLikes.toInt(),
            isPublic = isPublic,
            profilePictureUri = null,
            spotifyUid = spotifyUid,
            firebaseUid = firebaseUid)
    assertEquals(expectedProfile, result)
  }

  @Test
  fun itemToMapTest() {
    val profile =
        Profile(
            firstName = "John",
            lastName = "Doe",
            description = "Test description",
            numberOfLikes = 10,
            isPublic = true,
            profilePictureUri = Uri.parse("https://example.com/image.jpg"),
            spotifyUid = "spotify123",
            firebaseUid = "firebase123")

    val expectedMap: HashMap<String, Any> =
        hashMapOf(
            "firstName" to "John",
            "lastName" to "Doe",
            "description" to "Test description",
            "numberOfLikes" to 10,
            "spotifyUid" to "spotify123",
            "firebaseUid" to "firebase123",
            "isPublic" to true,
            "profilePictureUri" to "https://example.com/image.jpg",
            "topSongs" to listOf<DocumentReference>(),
            "chosenSongs" to listOf<DocumentReference>(),
        )

    assertEquals(expectedMap, profileConnection.itemToMap(profile))
  }

  @Test
  fun testAddProfilesIfNotExist() {
    val profiles =
        listOf(
            Profile(
                firstName = "New",
                lastName = "User",
                description = "No description",
                numberOfLikes = 0,
                isPublic = false,
                spotifyUid = "newspotifyUid",
                firebaseUid = "newfirebaseUid"))

    every { querySnapshot.isEmpty } returns true
    profileConnection.addProfilesIfNotExist(profiles)
  }

  @Test
  fun testAddItem() {
    val profile =
        Profile(
            firstName = "John",
            lastName = "Doe",
            description = "Test description",
            numberOfLikes = 10,
            isPublic = true,
            profilePictureUri = Uri.parse("https://example.com/image.jpg"),
            spotifyUid = "spotify123",
            firebaseUid = "firebase123")

    val trackConnection = mockk<TrackConnection>(relaxed = true)
    val profileConnection = ProfileConnection(firebaseFirestore, trackConnection = trackConnection)

    profileConnection.addItem(profile)

    verify { trackConnection.addItemsIfNotExist(profile.topSongs) }
    verify { trackConnection.addItemsIfNotExist(profile.chosenSongs) }
  }

  @Test
  fun testAddItemWithId() {
    val profile =
        Profile(
            firstName = "John",
            lastName = "Doe",
            description = "Test description",
            numberOfLikes = 10,
            isPublic = true,
            profilePictureUri = Uri.parse("https://example.com/image.jpg"),
            spotifyUid = "spotify123",
            firebaseUid = "firebase123")

    val trackConnection = mockk<TrackConnection>(relaxed = true)
    val profileConnection = ProfileConnection(firebaseFirestore, trackConnection = trackConnection)

    profileConnection.addItemWithId(profile)

    verify { trackConnection.addItemsIfNotExist(profile.topSongs) }
    verify { trackConnection.addItemsIfNotExist(profile.chosenSongs) }
  }

  @Test
  fun testGetItemCallsOtherGetItem() {
    val itemId = "testItemId"

    // Mock the ProfileConnection
    val profileConnection =
        spyk(ProfileConnection(trackConnection = TrackConnection()), recordPrivateCalls = true)

    // Define the behavior for the second getItem method
    every {
      profileConnection.getItem(
          itemId, any<(DocumentSnapshot, MutableStateFlow<Profile?>) -> Unit>())
    } returns flowOf<Profile>()

    // Call the method under test
    profileConnection.getItem(itemId)

    // Verify that the second getItem method was called
    verify {
      profileConnection.getItem(
          itemId, any<(DocumentSnapshot, MutableStateFlow<Profile?>) -> Unit>())
    }
  }

  @Test
  fun testGetItem() = runBlocking {
    withTimeout(3000) {
      // Pass the mock Firestore instance to your BeaconConnection

      val documentReference = mockk<DocumentReference>(relaxed = true)
      val collectionReference = mockk<CollectionReference>(relaxed = true)
      val trackDocumentReference = mockk<DocumentReference>(relaxed = true)
      // profileConnection = mockk<ProfileConnection>(relaxed = true)

      // make a real track
      val track =
          Track( // id, title, artist
              "Sample ID", "trackTitle", "trackArtist")

      val mockTask = mockk<Task<DocumentSnapshot>>()
      val mockDocumentSnapshot = mockk<DocumentSnapshot>()

      val getTestProfile =
          Profile(
              "Sample First Name",
              "Sample last name",
              "Sample desc",
              0,
              false,
              Uri.parse("https://example.com/image.jpg"),
              "Sample Profile ID",
              "Sample ID",
              listOf(track),
              listOf(track, track))

      val mapOfTestProfile =
          hashMapOf(
              "id" to getTestProfile.firebaseUid,
              "firstName" to getTestProfile.firstName,
              "lastName" to getTestProfile.lastName,
              "description" to getTestProfile.description,
              "topSongs" to getTestProfile.topSongs.map { it.id },
              "chosenSongs" to getTestProfile.chosenSongs.map { it.id })

      every { mockDocumentSnapshot.getData() } returns mapOfTestProfile
      every { mockDocumentSnapshot.exists() } returns true
      every { mockDocumentSnapshot.id } returns getTestProfile.firebaseUid
      every { mockDocumentSnapshot.getString("firstName") } returns getTestProfile.firstName
      every { mockDocumentSnapshot.getString("lastName") } returns getTestProfile.lastName
      every { mockDocumentSnapshot.getString("description") } returns getTestProfile.description
      every { mockDocumentSnapshot.getLong("numberOfLikes") } returns
          getTestProfile.numberOfLikes.toLong()
      every { mockDocumentSnapshot.getBoolean("isPublic") } returns getTestProfile.isPublic
      every { mockDocumentSnapshot.getString("profilePictureUri") } returns
          getTestProfile.profilePictureUri.toString()
      every { mockDocumentSnapshot.getString("spotifyUid") } returns getTestProfile.spotifyUid
      every { mockDocumentSnapshot.getString("firebaseUid") } returns getTestProfile.firebaseUid
      every { mockDocumentSnapshot.get("topSongs") } returns
          getTestProfile.topSongs.map { trackDocumentReference }
      every { mockDocumentSnapshot.get("chosenSongs") } returns
          getTestProfile.chosenSongs.map { trackDocumentReference }

      every { mockDocumentSnapshot.getString("title") } returns track.title
      every { mockDocumentSnapshot.getString("artist") } returns track.artist

      // Define behavior for the addOnSuccessListener method
      every { mockTask.addOnSuccessListener(any<OnSuccessListener<DocumentSnapshot>>()) } answers
          {
            val listener = arg<OnSuccessListener<DocumentSnapshot>>(0)

            // Define the behavior of the mock DocumentSnapshot here
            listener.onSuccess(mockDocumentSnapshot)
            mockTask
          }
      every { mockTask.addOnFailureListener(any()) } answers { mockTask }

      every { mockTask.isComplete } returns true
      every { mockTask.isSuccessful } returns true
      every { mockTask.exception } returns null
      every { mockTask.isCanceled } returns false
      // get result
      every { mockTask.result } returns mockDocumentSnapshot

      // Define behavior for the get() method on the DocumentReference to return the mock task
      every { documentReference.get() } returns mockTask
      every { trackDocumentReference.get() } returns mockTask

      every { firebaseFirestore.collection(profileConnection.collectionName) } returns
          collectionReference
      every { collectionReference.document("testProfile") } returns documentReference

      // Call the function under test
      val retrievedProfile =
          profileConnection
              .getItem("testProfile", { _, _ -> })
              .filter { !it.topSongs.isEmpty() }
              .firstOrNull()

      // Verify that the get function is called on the document with the correct id
      coVerify { documentReference.get() }

      assert(getTestProfile == retrievedProfile)
    }
  }
}
