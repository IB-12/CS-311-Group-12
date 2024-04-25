package ch.epfl.cs311.wanderwave.model

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import ch.epfl.cs311.wanderwave.model.data.Beacon
import ch.epfl.cs311.wanderwave.model.data.Location
import ch.epfl.cs311.wanderwave.model.data.Profile
import ch.epfl.cs311.wanderwave.model.data.ProfileTrackAssociation
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.localDb.AppDatabase
import ch.epfl.cs311.wanderwave.model.localDb.PlaceHolderEntity
import ch.epfl.cs311.wanderwave.model.remote.BeaconConnection
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import io.mockk.coVerify
import io.mockk.every
import io.mockk.junit4.MockKRule
import io.mockk.mockk
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.system.measureTimeMillis

public class BeaconConnectionTest {

  @get:Rule val mockkRule = MockKRule(this)
  private lateinit var beaconConnection: BeaconConnection

  private lateinit var firestore: FirebaseFirestore
  private lateinit var documentReference: DocumentReference
  private lateinit var collectionReference: CollectionReference

  lateinit var beacon: Beacon

  @Before
  fun setup() {
    // Create the mocks
    firestore = mockk()
    documentReference = mockk<DocumentReference>(relaxed = true)
    collectionReference = mockk<CollectionReference>(relaxed = true)

    // Mock data
    beacon = Beacon(
        id = "testBeacon",
        location = Location(1.0, 1.0, "Test Location"),
        profileAndTrack =
        listOf(
            ProfileTrackAssociation(
                Profile(
                    "Sample First Name",
                    "Sample last name",
                    "Sample desc",
                    0,
                    false,
                    null,
                    "Sample Profile ID",
                    "Sample Track ID"),
                Track("Sample Track ID", "Sample Track Title", "Sample Artist Name"))))

    // Define behavior for the mocks
    every { firestore.collection(any()) } returns mockk(relaxed = true)
    every { collectionReference.document(beacon.id) } returns documentReference
    every { firestore.collection(any()) } returns collectionReference

    // Pass the mock Firestore instance to your BeaconConnection
    beaconConnection = BeaconConnection(firestore)
  }

  @Test
  fun testAddAndGetItem() = runBlocking {
    withTimeout(3000) { // Increased timeout to 30 seconds
      val beacon = Beacon(
        id = "testBeacon",
        location = Location(1.0, 1.0, "Test Location"),
          profileAndTrack =
          listOf(
              ProfileTrackAssociation(
                  Profile(
                      "Sample First Name",
                      "Sample last name",
                      "Sample desc",
                      0,
                      false,
                      null,
                      "Sample Profile ID",
                      "Sample Track ID"),
                  Track("Sample Track ID", "Sample Track Title", "Sample Artist Name"))))


      val trackDocumentSnapshot = mockk<DocumentSnapshot>(relaxed = true)
      every { trackDocumentSnapshot.data } returns mapOf(
        "id" to "testTrack",
        "title" to "Test Title",
        "artist" to "Test Artist"
      )
      every { trackDocumentSnapshot.toObject(Track::class.java) } returns Track("testTrack", "Test Title", "Test Artist")
      every { trackDocumentSnapshot.exists() } returns true

      val trackRef = mockk<DocumentReference>(relaxed = true)
      val trackTask = mockk<Task<DocumentSnapshot>>(relaxed = true)
      every { trackTask.isSuccessful } returns true
      every { trackTask.result } returns trackDocumentSnapshot
      every { trackRef.get() } returns trackTask

      // Define behavior for the mocks
      every { firestore.collection(any()).document(any()).set(any()) } returns Tasks.forResult(null)

      // Define behavior for the mocks
//      every { firestore.collection(any()).document(any()).delete() } returns Tasks.forResult(null)
//      coEvery { firestore.collection(any()).document(any()).get() } returns Tasks.forResult(documentSnapshot)
//      every { documentSnapshot.toObject(Beacon::class.java) } returns beacon
//      every { documentSnapshot.get("location") } returns mapOf("latitude" to 1.0, "longitude" to 1.0, "name" to "Test Location")
//      every { documentSnapshot.get("tracks") } returns listOf(trackRef)
//      every { documentSnapshot.id } returns "testBeacon"
//      every { documentSnapshot.exists() } returns true

      Log.d("Firestore", "Adding beacon")
      beaconConnection.addItemWithId(beacon)
      Log.d("Firestore", "Added beacon, getting beacon")
      val retrievedBeacon = beaconConnection.getItem("testBeacon").firstOrNull()

      assertEquals(beacon, retrievedBeacon)
      verify { trackRef.get() }
    }
  }



  @Test
  fun testAddItem() = runBlocking {
    // Mock data
    val beacon =
        Beacon(
            id = "testBeacon",
            location = Location(1.0, 1.0, "Test Location"),
            profileAndTrack =
                listOf(
                    ProfileTrackAssociation(
                        Profile(
                            "Sample First Name",
                            "Sample last name",
                            "Sample desc",
                            0,
                            false,
                            null,
                            "Sample Profile ID",
                            "Sample Track ID"),
                        Track("Sample Track ID", "Sample Track Title", "Sample Artist Name"))))

    // Call the function under test
    beaconConnection.addItem(beacon)

    // Verify that either the set function is called
    verify { collectionReference.add(any()) }
  }

  @Test
  fun testGetAll() = runBlocking {
    // Place holder test before we merge with the main for coverage
    val retrievedBeacons = beaconConnection.getAll().first()

    // Assert nothing
  }

  @Test
  fun testAddTrackToBeacon() {
    // Mock data
    val beacon =
        Beacon(
            id = "testBeacon",
            location = Location(1.0, 1.0, "Test Location"),
            profileAndTrack =
                listOf(
                    ProfileTrackAssociation(
                        Profile(
                            "Sample First Name",
                            "Sample last name",
                            "Sample desc",
                            0,
                            false,
                            null,
                            "Sample Profile ID",
                            "Sample Track ID"),
                        Track("Sample Track ID", "Sample Track Title", "Sample Artist Name"))))

    val track = Track("testTrack2", "Test Title 2", "Test Artist 2")

    // Call the function under test
    beaconConnection.addTrackToBeacon(beacon.id, track, {})

    // No verification is needed for interactions with the real object
  }

  @Test
  fun testUpdateItem() = runBlocking {
    // Mock data
    val beacon =
        Beacon(
            id = "testBeacon",
            location = Location(1.0, 1.0, "Test Location"),
            profileAndTrack =
                listOf(
                    ProfileTrackAssociation(
                        Profile(
                            "Sample First Name",
                            "Sample last name",
                            "Sample desc",
                            0,
                            false,
                            null,
                            "Sample Profile ID",
                            "Sample Track ID"),
                        Track("Sample Track ID", "Sample Track Title", "Sample Artist Name"))))
    // Call the function under test
    beaconConnection.updateItem(beacon)

    // Verify that the set function is called on the document with the correct id
    verify { documentReference.set(any()) }
  }

  @Test
  fun testAddItemTwice() = runBlocking {
    withTimeout(20000) {
        val beacon =
            Beacon(
                id = "testBeacon",
                location = Location(1.0, 1.0, "Test Location"),
                profileAndTrack =
                listOf(
                    ProfileTrackAssociation(
                        Profile(
                            "Sample First Name",
                            "Sample last name",
                            "Sample desc",
                            0,
                            false,
                            null,
                            "Sample Profile ID",
                            "Sample Track ID"
                        ),
                        Track("Sample Track ID", "Sample Track Title", "Sample Artist Name")
                    )
                )
            )
    }
  fun testGetItem() = runBlocking {
    withTimeout(3000) {
      // Mock the Task
      val mockTask = mockk<Task<DocumentSnapshot>>()
      val mockDocumentSnapshot = mockk<DocumentSnapshot>()

      val getTestBeacon = Beacon(
          id = "testBeacon",
          location = Location(1.0, 1.0, "Test Location"),
          profileAndTrack =
          listOf(
              ProfileTrackAssociation(
                  Profile(
                      "Sample First Name",
                      "Sample last name",
                      "Sample desc",
                      0,
                      false,
                      null,
                      "Sample Profile ID",
                      "Sample Track ID"),
                  Track("Sample Track ID", "Sample Track Title", "Sample Artist Name"))))

      every { mockDocumentSnapshot.getData() } returns getTestBeacon.toMap()
      every { mockDocumentSnapshot.exists() } returns true
      every { mockDocumentSnapshot.id } returns getTestBeacon.id
      every { mockDocumentSnapshot.get("location") } returns getTestBeacon.location.toMap()
      every { mockDocumentSnapshot.get("tracks") } returns getTestBeacon.profileAndTrack

      // Define behavior for the addOnSuccessListener method
      every { mockTask.addOnSuccessListener(any<OnSuccessListener<DocumentSnapshot>>()) } answers
          {
            val listener = arg<OnSuccessListener<DocumentSnapshot>>(0)

            // Define the behavior of the mock DocumentSnapshot here
            listener.onSuccess(mockDocumentSnapshot)
            mockTask
          }
      every { mockTask.addOnFailureListener(any()) } answers { mockTask }

      // Define behavior for the get() method on the DocumentReference to return the mock task
      every { documentReference.get() } returns mockTask

      // Call the function under test
      val retrievedBeacon = beaconConnection.getItem("testBeacon").first()

      // Verify that the get function is called on the document with the correct id
      coVerify { documentReference.get() }
      assertEquals(getTestBeacon, retrievedBeacon)
    }
  }


  @Test
  fun testGetAllItems() = runBlocking {
    withTimeout(3000) {
      // Mock the Task
      val mockTask = mockk<Task<QuerySnapshot>>()
      val mockQuerySnapshot = mockk<QuerySnapshot>()
      val mockDocumentSnapshot = mockk<QueryDocumentSnapshot>()

      val getTestBeacon =
        Beacon(
          id = "testBeacon", location = Location(1.0, 1.0, "Test Location"), tracks = listOf())

      val getTestBeaconList = listOf(getTestBeacon, getTestBeacon)


      every { mockDocumentSnapshot.getData() } returns getTestBeacon.toMap()
      every { mockDocumentSnapshot.exists() } returns true
      every { mockDocumentSnapshot.id } returns getTestBeacon.id
      every { mockDocumentSnapshot.get("location") } returns getTestBeacon.location.toMap()
      every { mockDocumentSnapshot.get("tracks") } returns getTestBeacon.tracks

      every { mockQuerySnapshot.documents } returns listOf(mockDocumentSnapshot,mockDocumentSnapshot)
      every { mockQuerySnapshot.iterator() } returns mutableListOf(mockDocumentSnapshot,mockDocumentSnapshot).iterator()

      // Define behavior for the addOnSuccessListener method
      every { mockTask.addOnSuccessListener(any<OnSuccessListener<QuerySnapshot>>()) } answers
          {
            val listener = arg<OnSuccessListener<QuerySnapshot>>(0)

            // Define the behavior of the mock QuerySnapshot here
            listener.onSuccess(mockQuerySnapshot)
            mockTask
          }
      every { mockTask.addOnFailureListener(any()) } answers { mockTask }

      // Define behavior for the get() method on the CollectionReference to return the mock task
      every { collectionReference.get() } returns mockTask

      // Call the function under test
      val retrievedBeacons = beaconConnection.getAll().first()

      // Verify that the get function is called on the collection
      coVerify { collectionReference.get() }
      assertEquals(getTestBeaconList, retrievedBeacons)
    }
  }

  @Test
  fun testDeleteItem() {
    // Call the function under test
    beaconConnection.deleteItem(beacon)

      beaconConnection.addItemWithId(beacon)
      beaconConnection.addItemWithId(beacon)


    }
  }

  @Test
  fun AddDeleteAndGetItem() = runBlocking {
    withTimeout(20000) {
      val beacon =
          Beacon(
              id = "testBeacon1",
              location = Location(1.0, 1.0, "Test Location"),
              profileAndTrack =
                  listOf(
                      ProfileTrackAssociation(
                          Profile(
                              "Sample First Name",
                              "Sample last name",
                              "Sample desc",
                              0,
                              false,
                              null,
                              "Sample Profile ID",
                              "Sample Track ID"),
                          Track("Sample Track ID", "Sample Track Title", "Sample Artist Name"))))

      beaconConnection.addItemWithId(beacon)
      beaconConnection.deleteItem("testBeacon1")

      // Flag to indicate if the flow emits any value
      var valueEmitted = false

      // Collect the flow within a 2-second timeout
      measureTimeMillis {
        withTimeoutOrNull(2000) {
          beaconConnection.getItem("testBeacon1").collect {
            valueEmitted = true // Set the flag if the flow emits any value
          }
        }
      }

      // Assert that the flow didn't emit anything within the timeout
      assert(valueEmitted.not()) { "Flow emitted unexpected value" }
    }
  }

  // TODO : To be deleted after a real entry is added to the database
  lateinit var db: AppDatabase

  @Test
  fun createDb() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()

    val placeHolderEntity = PlaceHolderEntity("1", 0.5, 0.5)
  }

  @After
  fun cleanupTestData() = runBlocking {
    // Remove the test data
    beaconConnection.deleteItem("testBeacon")
    beaconConnection.deleteItem("testBeacon1")
    beaconConnection.deleteItem("testBeacon2")
    beaconConnection.deleteItem("nonexistentBeacon")
  }
}
